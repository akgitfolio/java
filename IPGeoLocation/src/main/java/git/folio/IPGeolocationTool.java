package git.folio;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.record.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class IPGeolocationTool {
    private static final String CITY_DB_URL = "https://git.io/GeoLite2-City.mmdb";
    private static final String ASN_DB_URL = "https://git.io/GeoLite2-ASN.mmdb";
    private static final String COUNTRY_DB_URL = "https://git.io/GeoLite2-Country.mmdb";

    private static final String CITY_DB_PATH = "./GeoLite2-City.mmdb";
    private static final String ASN_DB_PATH = "./GeoLite2-ASN.mmdb";
    private static final String COUNTRY_DB_PATH = "./GeoLite2-Country.mmdb";

    private DatabaseReader cityReader;
    private DatabaseReader asnReader;
    private DatabaseReader countryReader;

    public IPGeolocationTool() throws IOException {
        downloadDatabaseIfNeeded(CITY_DB_URL, CITY_DB_PATH);
        downloadDatabaseIfNeeded(ASN_DB_URL, ASN_DB_PATH);
        downloadDatabaseIfNeeded(COUNTRY_DB_URL, COUNTRY_DB_PATH);

        cityReader = new DatabaseReader.Builder(new File(CITY_DB_PATH)).build();
        asnReader = new DatabaseReader.Builder(new File(ASN_DB_PATH)).build();
        countryReader = new DatabaseReader.Builder(new File(COUNTRY_DB_PATH)).build();
    }

    private void downloadDatabaseIfNeeded(String dbUrl, String dbPath) throws IOException {
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            System.out.println("Downloading database: " + dbPath);
            URL url = new URL(dbUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, Paths.get(dbPath), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Download complete: " + dbPath);
        }
    }

    public void lookupIP(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            // City lookup
            CityResponse cityResponse = cityReader.city(ipAddress);
            Country country = cityResponse.getCountry();
            City city = cityResponse.getCity();
            Postal postal = cityResponse.getPostal();
            Location location = cityResponse.getLocation();

            // ASN lookup
            AsnResponse asnResponse = asnReader.asn(ipAddress);

            // Country lookup
            CountryResponse countryResponse = countryReader.country(ipAddress);

            System.out.println("IP Address: " + ip);
            System.out.println("Country: " + country.getName());
            System.out.println("City: " + city.getName());
            System.out.println("Postal Code: " + postal.getCode());
            System.out.println("Latitude: " + location.getLatitude());
            System.out.println("Longitude: " + location.getLongitude());
            System.out.println("ASN: " + asnResponse.getAutonomousSystemNumber());
            System.out.println("ASN Organization: " + asnResponse.getAutonomousSystemOrganization());
            System.out.println("Country ISO: " + countryResponse.getCountry().getIsoCode());

        } catch (IOException | GeoIp2Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            IPGeolocationTool tool = new IPGeolocationTool();
            tool.lookupIP("8.8.8.8");
        } catch (IOException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }
}