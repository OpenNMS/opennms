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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

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

        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        try (JestClientWithCircuitBreaker jestClient = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(ElasticFlowRepositoryRetryIT.class.getName()), eventForwarder)) {
            final FlowRepository elasticFlowRepository = new InitializingFlowRepository(
                    new ElasticFlowRepository(new MetricRegistry(), jestClient, IndexStrategy.MONTHLY,
                            new MockIdentity(), new MockTracerRegistry(), new IndexSettings()), jestClient);
            // persist data
            elasticFlowRepository.persist(Lists.newArrayList(getMockFlowWithoutDirection()));

            // wait for entries to show up
            with().pollInterval(5, SECONDS).await().atMost(1, MINUTES).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) > 0;
            });

            final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
            assertNotEquals(0L, SearchResultUtils.getTotal(searchResult));

            // check whether the default value is applied
            final JSONParser parser = new JSONParser();
            final JSONObject responseJsonObject = (JSONObject) parser.parse(searchResult.getJsonString());
            final JSONArray hitsJsonArray = (JSONArray) ((JSONObject) responseJsonObject.get("hits")).get("hits");
            final JSONObject sourceJsonObject = (JSONObject) ((JSONObject) hitsJsonArray.get(0)).get("_source");

            LOG.info("Direction value is: " + sourceJsonObject.get("netflow.direction"));
            assertEquals("unknown", sourceJsonObject.get("netflow.direction"));
        }

        // stop ES
        elasticSearchRule.stopServer();
    }
}
