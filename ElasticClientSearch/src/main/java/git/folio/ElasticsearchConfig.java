package git.folio;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticsearchConfig {
    private static final String HOST = "localhost";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    private static final String USERNAME = "elastic";
    private static final String PASSWORD = "password";

    private static RestHighLevelClient client = null;

    public static synchronized RestHighLevelClient getClient() {
        if (client == null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(USERNAME, PASSWORD));

            RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT, SCHEME))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider));

            client = new RestHighLevelClient(builder);
        }
        return client;
    }

    public static void closeClient() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}