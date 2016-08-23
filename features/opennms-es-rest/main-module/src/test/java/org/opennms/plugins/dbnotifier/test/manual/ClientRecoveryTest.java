package org.opennms.plugins.dbnotifier.test.manual;

import static org.junit.Assert.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.net.InetAddress;
import java.util.Date;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.plugins.elasticsearch.rest.EventToIndex;
import org.opennms.plugins.elasticsearch.rest.IndexNameFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRecoveryTest {
	private static final Logger LOG = LoggerFactory.getLogger(ClientRecoveryTest .class);

	@Test
	public void test() {
		LOG.debug("***************** start of test ClientRecoveryTest");

		try {

			IndexNameFunction indexNameFunction = new IndexNameFunction();
			String rootIndexName = EventToIndex.ALARM_INDEX_NAME;
			String indexName = indexNameFunction.apply(rootIndexName , new Date());
			
			// Get Jest client
			HttpClientConfig clientConfig = new HttpClientConfig.Builder(
					"http://localhost:9200").multiThreaded(true).build();
			JestClientFactory factory = new JestClientFactory();
			factory.setHttpClientConfig(clientConfig);
			JestClient jestClient = factory.getObject();

			try {

				String query = "{\n" 
				        +"\n       \"query\": {"
						+ "\n         \"match\": {"
					    + "\n         \"alarmid\": \"1359\""
					    + "\n          }"
					    + "\n        }"
					    + "\n     }";

				Search search = new Search.Builder(query)
				// multiple index or types can be added.
				.addIndex(indexName)
				.build();

				SearchResult sresult = jestClient.execute(search);

				LOG.debug("received search result: "+sresult.getJsonString()
						+ "\n   response code:" +sresult.getResponseCode() 
						+ "\n   error message: "+sresult.getErrorMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				// shutdown client
				jestClient.shutdownClient();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		LOG.debug("***************** end of test ClientRecoveryTest");
	}
	


}
