/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.plugins.elasticsearch.rest;

import java.time.Instant;

import org.junit.Test;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class ClientRecoveryTest {
	private static final Logger LOG = LoggerFactory.getLogger(ClientRecoveryTest .class);

	@Test
	public void test() {
		LOG.debug("***************** start of test ClientRecoveryTest");

		try {

			String rootIndexName = EventToIndex.Indices.ALARMS.getIndexPrefix();
			String indexName = IndexStrategy.MONTHLY.getIndex(rootIndexName , Instant.now());
			
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
