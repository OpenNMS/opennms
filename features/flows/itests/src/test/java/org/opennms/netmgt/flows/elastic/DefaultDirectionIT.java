/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.flows.elastic;

import static org.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClientFactory;
import org.opennms.features.elastic.client.model.SearchRequest;
import org.opennms.features.elastic.client.model.SearchResponse;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class DefaultDirectionIT {
    private static Logger LOG = LoggerFactory.getLogger(DefaultDirectionIT.class);

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withStartDelay(0)
            .withManualStartup()
    );

    private EnrichedFlow getMockFlowWithoutDirection() {
        final EnrichedFlow flow = new EnrichedFlow();
        flow.setNetflowVersion(Flow.NetflowVersion.V5);
        flow.setIpProtocolVersion(4);
        flow.setSrcAddr("192.168.1.2");
        flow.setDstAddr("192.168.2.2");
        flow.setSrcAddrHostname(null);
        flow.setDstAddrHostname(null);
        flow.setNextHopHostname(null);
        flow.setVlan(null);
        return flow;
    }

    @Test
    public void testDefaultDirection() throws Exception {
        // start ES
        elasticSearchRule.startServer();

        final ElasticRestClientFactory elasticRestClientFactory = new ElasticRestClientFactory(elasticSearchRule.getUrl(), null, null);
        final ElasticRestClient elasticRestClient = elasticRestClientFactory.createClient();
        try {
            final FlowRepository elasticFlowRepository = new InitializingFlowRepository(
                    new ElasticFlowRepository(new MetricRegistry(), elasticRestClient, IndexStrategy.MONTHLY,
                            new MockIdentity(), new MockTracerRegistry(), new IndexSettings()), elasticRestClient);
            // persist data
            elasticFlowRepository.persist(Lists.newArrayList(getMockFlowWithoutDirection()));

            // wait for entries to show up
            with().pollInterval(5, SECONDS).await().atMost(1, MINUTES).until(() -> {
                final SearchResponse searchResponse = elasticRestClient.search(SearchRequest.forIndices(List.of("netflow-*"), "{\"query\": {\"match_all\": {}}}"));
                LOG.info("Response: {} documents", searchResponse.getHits().getTotalHits());
                return searchResponse.getHits().getTotalHits() > 0;
            });

            final SearchResponse searchResponse = elasticRestClient.search(SearchRequest.forIndices(List.of("netflow-*"), "{\"query\": {\"match_all\": {}}}"));
            assertNotEquals(0L, searchResponse.getHits().getTotalHits());

            // check whether the default value is applied
            final SearchResponse.SearchHit firstHit = searchResponse.getHits().getHits().get(0);
            final JSONParser parser = new JSONParser();
            final JSONObject sourceJsonObject = (JSONObject) parser.parse(firstHit.getSource().toString());

            LOG.info("Direction value is: " + sourceJsonObject.get("netflow.direction"));
            assertEquals("unknown", sourceJsonObject.get("netflow.direction"));
        } finally {
            elasticRestClient.close();
        }

        // stop ES
        elasticSearchRule.stopServer();
    }
}
