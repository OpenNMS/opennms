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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class BulkingIT {
    private static Logger LOG = LoggerFactory.getLogger(BulkingIT.class);

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withStartDelay(0)
            .withManualStartup()
    );

    private List<EnrichedFlow> createMockedFlows(final int count) {
        final List<EnrichedFlow> flows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final EnrichedFlow flow = new EnrichedFlow();
            flow.setNetflowVersion(Flow.NetflowVersion.V5);
            flow.setIpProtocolVersion(4);
            flow.setSrcAddr("192.168." + i / 256 + "." + i % 256);
            flow.setDstAddr("192.168." + i / 256 + "." + i % 256);
            flow.setSrcAddrHostname(null);
            flow.setDstAddrHostname(null);
            flow.setNextHopHostname(null);
            flow.setVlan(null);
            flows.add(flow);
        }
        return flows;
    }

    private FlowRepository createFlowRepository(final JestClientWithCircuitBreaker jestClient, int bulkSize, int bulkFlushMs) {
        final ElasticFlowRepository elasticFlowRepository = new ElasticFlowRepository(new MetricRegistry(), jestClient,
                IndexStrategy.MONTHLY, new MockIdentity(), new MockTracerRegistry(), new IndexSettings());
        elasticFlowRepository.setBulkSize(bulkSize);
        elasticFlowRepository.setBulkFlushMs(bulkFlushMs);
        return new InitializingFlowRepository(elasticFlowRepository, jestClient);
    }

    /**
     * Tests that small bulk will be persisted after the given timeout of 1000ms (bulkFlushMs).
     */
    @Test
    public void testFlushTimeout() throws Exception {
        elasticSearchRule.startServer();

        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();

        try (final JestClientWithCircuitBreaker jestClient = restClientFactory.createClientWithCircuitBreaker(
                CircuitBreakerRegistry.of(CircuitBreakerConfig.custom().build()).circuitBreaker(BulkingIT.class.getName()), eventForwarder)) {

            final FlowRepository flowRepository = createFlowRepository(jestClient, 1000, 5000);

            final long[] persists = new long[2];

            // send full bulk in order to estimate last persist
            flowRepository.persist(createMockedFlows(1000));

            with().pollInterval(25, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                // record the initial persist
                persists[0] = System.currentTimeMillis();
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            // send small bulks
            flowRepository.persist(createMockedFlows(30));
            flowRepository.persist(createMockedFlows(30));
            flowRepository.persist(createMockedFlows(30));

            // these 90 flows should not be visible yet
            with().pollInterval(2, SECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            // now wait for the flows to appear
            with().pollInterval(25, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                // record the final persist
                persists[1] = System.currentTimeMillis();
                return SearchResultUtils.getTotal(searchResult) == 1090L;
            });

            long timePassed = (persists[1] - persists[0]);
            LOG.info("Time between persists is {}ms", timePassed);
            assertTrue(timePassed >= 5000);
        }

        // stop ES
        elasticSearchRule.stopServer();
    }

    /**
     * Tests that large bulk will be persisted immediately.
     */
    @Test
    public void testSingleLargeBulk() throws Exception {
        elasticSearchRule.startServer();

        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());

        try (final JestClientWithCircuitBreaker jestClient = createJestClient()) {
            final FlowRepository flowRepository = createFlowRepository(jestClient, 1000, 300000);
            flowRepository.persist(createMockedFlows(1000));

            // these results should appear immediately since the bulk size of 1000 was reached
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });
        }

        // stop ES
        elasticSearchRule.stopServer();
    }

    private JestClientWithCircuitBreaker createJestClient() throws Exception {
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        return restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(BulkingIT.class.getName()), eventForwarder);
    }

    /**
     * Tests that small bulks sum up and will be persisted when bulkSize is reached.
     */
    @Test
    public void testSmallBulks() throws Exception {
        elasticSearchRule.startServer();

        try (final JestClientWithCircuitBreaker jestClient = createJestClient()) {
            final FlowRepository flowRepository = createFlowRepository(jestClient, 1000, 300000);

            flowRepository.persist(createMockedFlows(1000));

            // these results should appear immediately since the bulk size of 1000 was reached
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(createMockedFlows(500));

            // these results should not appear immediately since the bulk size is only 500
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(createMockedFlows(400));

            // these results should not appear immediately since the bulk size is only 900
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(createMockedFlows(100));

            // these results should now appear immediately since the bulk size of 1000 was reached
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 2000L;
            });
        }

        // stop ES
        elasticSearchRule.stopServer();
    }
}
