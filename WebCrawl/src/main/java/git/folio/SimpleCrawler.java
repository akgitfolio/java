package git.folio;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SimpleCrawler {
    private static final String USER_AGENT = "SimpleCrawler";
    private static final int MAX_PAGES_TO_CRAWL = 100;
    private static final int CRAWL_DELAY = 1000; // 1 second delay between requests
    private static final Logger LOGGER = Logger.getLogger(SimpleCrawler.class.getName());
    private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))$");

    private Set<String> visitedUrls = new HashSet<>();
    private Queue<String> urlsToVisit = new LinkedList<>();
    private SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();

    public void crawl(String startUrl) {
        urlsToVisit.add(startUrl);

        while (!urlsToVisit.isEmpty() && visitedUrls.size() < MAX_PAGES_TO_CRAWL) {
            String url = urlsToVisit.poll();
            if (visitedUrls.contains(url) || FILTERS.matcher(url).matches()) {
                continue;
            }

            if (isAllowedByRobotsTxt(url)) {
                try {
                    Thread.sleep(CRAWL_DELAY); // Politeness delay
                    Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
                    processPage(url, doc);
                    visitedUrls.add(url);

                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href");
                        if (!visitedUrls.contains(nextUrl) && !FILTERS.matcher(nextUrl).matches()) {
                            urlsToVisit.add(nextUrl);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error crawling " + url, e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Crawl delay interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private boolean isAllowedByRobotsTxt(String url) {
        try {
            URL urlObj = new URL(url);
            String robotsTxtUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt";
            BaseRobotRules rules = robotParser.parseContent(robotsTxtUrl,
                    Jsoup.connect(robotsTxtUrl).userAgent(USER_AGENT).execute().bodyAsBytes(),
                    "text/plain", USER_AGENT);
            return rules.isAllowed(url);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Malformed URL: " + url, e);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error fetching robots.txt for " + url, e);
            return true; // Assume allowed if we can't fetch robots.txt
        }
    }

    private void processPage(String url, Document doc) {
        LOGGER.info("Crawled: " + url);
        // Add your processing logic here
    }

    public static void main(String[] args) {
        SimpleCrawler crawler = new SimpleCrawler();
        crawler.crawl("https://example.com");
    }
}
