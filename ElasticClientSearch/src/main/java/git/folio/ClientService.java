package git.folio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientService {
    private static final String INDEX_NAME = "clients";
    private final RestHighLevelClient esClient;
    private final ObjectMapper objectMapper;

    public ClientService() {
        this.esClient = ElasticsearchConfig.getClient();
        this.objectMapper = new ObjectMapper();
    }

    public String saveClient(Client client) throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                .source(objectMapper.writeValueAsString(client), XContentType.JSON);
        IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.getId();
    }

    public List<Client> searchClients(String searchTerm) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "name", "email", "accountType"));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        return processSearchResponse(searchResponse);
    }

    public List<Client> findByAccountType(String accountType) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("accountType", accountType));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        return processSearchResponse(searchResponse);
    }

    public List<Client> findByInvestmentAmountGreaterThan(double amount) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery("investmentAmount").gt(amount));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        return processSearchResponse(searchResponse);
    }

    private List<Client> processSearchResponse(SearchResponse searchResponse) {
        List<Client> clients = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Client client = objectMapper.convertValue(sourceAsMap, Client.class);
            client.setId(hit.getId());
            clients.add(client);
        }
        return clients;
    }
}