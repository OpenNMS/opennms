package org.opennms.plugins.elasticsearch.rest;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

public class RestClientFactory {
	
	private JestClientFactory factory=null;
	
	
	
	/**
	 * create a Jest Client Factory using url in form http://localhost:9200
	 * @param elasticSearchURL
	 */
	public RestClientFactory(String elasticSearchURL, String esusername, String espassword ){
		
	
		HttpClientConfig clientConfig = new HttpClientConfig.Builder(
				elasticSearchURL).multiThreaded(true)
				.defaultCredentials(esusername, espassword)
				.build();
		
		factory = new JestClientFactory();
		
		factory.setHttpClientConfig(clientConfig);
		
	}
	
	public JestClient getJestClient(){
		JestClient jestClient=factory.getObject();
		return jestClient;
	}

}
