package es;
/**
 * ElasticSearch静态内部类形式的单例方法
 * @author wsz
 * @date 2018/8/13
 */

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class EsClient {
	
	private EsClient() {}
	
	private static class InitCliet {
		private final static RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")).build());
	}
	
	public static RestHighLevelClient getEsClient() {
		return InitCliet.client;
	}
}
