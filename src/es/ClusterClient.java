package es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import util.ComConvert;
/**
 * 集群模式下的客户端连接方法
 * @author wsz
 * @date 2018年8月20日
 */
public class ClusterClient {

	private static final String NODES ="127.0.0.1:9200";
	private static final String USERNAME ="abc";
	private static final String PASSWORD ="qwe";
	
    private RestClient s_LowClient = null;
    private RestHighLevelClient s_HighClient = null;
    
    public static void main(String[] args) throws IOException {
    	ClusterClient c = new ClusterClient();
    	
    	BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.termQuery("JLZT", "1"));//0无效1有效
		//模糊查询,不匹配则为空
		BoolQueryBuilder wildQuery = QueryBuilders.boolQuery();
		String searchStr = "*消火栓*";
		wildQuery.must(QueryBuilders.wildcardQuery("XHSMC", searchStr));
		wildQuery.must(QueryBuilders.wildcardQuery("XHSDZ", searchStr));
		boolQuery.must(wildQuery);
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .timeout(new TimeValue(60, TimeUnit.SECONDS))//查询超时时间
                .from(0)           //起始
                .size(3)          //数据量
                .query(boolQuery); //查询
        SearchRequest searchRequest = new SearchRequest()
                .indices("fire_xhs_read")	//索引名
                .types("xhs")				//类型
                .source(searchSourceBuilder);
        SearchResponse response = c.getHighClient().search(searchRequest);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
			System.out.println(hit.getSource());
		}
	}
    
    public RestHighLevelClient getHighClient() {
    	if(s_LowClient == null || s_HighClient == null) {
    		createClient();
    	}
    	return s_HighClient;
    }
    
	public void createClient() {
		try{
            List<HttpHost> hostArray = new ArrayList<>();
            String[] nodes = NODES.split(";");
            for(String item : nodes) {
                String[] host = item.split(":");
                if (host.length == 2 && !host[0].equals("") && !host[1].equals("")) {
                    hostArray.add(new HttpHost(host[0], ComConvert.toInteger(host[1], 9200), "http"));
                }
            }
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USERNAME, PASSWORD));
            s_LowClient = RestClient.builder(hostArray.toArray(new HttpHost[0])).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback()
            {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
                {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            }).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                @Override
                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                    return builder.setConnectTimeout(5000)
                            .setSocketTimeout(50000)
                            .setConnectionRequestTimeout(3000);
                }
            }).setMaxRetryTimeoutMillis(5 * 60 * 1000).build();
            s_HighClient = new RestHighLevelClient(s_LowClient);
        }catch (Exception e){
            e.printStackTrace();
        }
	}
}
