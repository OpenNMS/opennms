/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkingIT {
    private static Logger LOG = LoggerFactory.getLogger(BulkingIT.class);

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withStartDelay(0)
            .withManualStartup()
    );

    private List<Flow> createMockedFlows(final int count) {
        final List<Flow> flows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final Flow flow = mock(Flow.class);
            when(flow.getNetflowVersion()).thenReturn(Flow.NetflowVersion.V5);
            when(flow.getIpProtocolVersion()).thenReturn(4);
            when(flow.getSrcAddr()).thenReturn("192.168." + i / 256 + "." + i % 256);
            when(flow.getDstAddr()).thenReturn("192.168." + i / 256 + "." + i % 256);
            when(flow.getSrcAddrHostname()).thenReturn(Optional.empty());
            when(flow.getDstAddrHostname()).thenReturn(Optional.empty());
            when(flow.getNextHopHostname()).thenReturn(Optional.empty());
            when(flow.getVlan()).thenReturn(null);
            flows.add(flow);
        }
        return flows;
    }

    private FlowRepository createFlowRepository(final JestClient jestClient, final DocumentEnricher documentEnricher, int bulkSize, int bulkFlushMs) {
        final ElasticFlowRepository elasticFlowRepository = new ElasticFlowRepository(new MetricRegistry(), jestClient,
                IndexStrategy.MONTHLY, documentEnricher, new MockSessionUtils(), new MockNodeDao(), new MockSnmpInterfaceDao(),
                new MockIdentity(), new MockTracerRegistry(), new MockDocumentForwarder(), new IndexSettings());
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

        try (final JestClient jestClient = restClientFactory.createClient()) {
            final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
            final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
            final FlowRepository flowRepository = createFlowRepository(jestClient, documentEnricher, 1000, 5000);

            final long[] persists = new long[2];

            // send full bulk in order to estimate last persist
            flowRepository.persist(Lists.newArrayList(createMockedFlows(1000)), FlowDocumentTest.getMockFlowSource());

            with().pollInterval(25, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                // record the initial persist
                persists[0] = System.currentTimeMillis();
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            // send small bulks
            flowRepository.persist(Lists.newArrayList(createMockedFlows(30)), FlowDocumentTest.getMockFlowSource());
            flowRepository.persist(Lists.newArrayList(createMockedFlows(30)), FlowDocumentTest.getMockFlowSource());
            flowRepository.persist(Lists.newArrayList(createMockedFlows(30)), FlowDocumentTest.getMockFlowSource());

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

        try (final JestClient jestClient = restClientFactory.createClient()) {
            final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
            final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
            final FlowRepository flowRepository = createFlowRepository(jestClient, documentEnricher, 1000, 300000);
            flowRepository.persist(Lists.newArrayList(createMockedFlows(1000)), FlowDocumentTest.getMockFlowSource());

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

    /**
     * Tests that small bulks sum up and will be persisted when bulkSize is reached.
     */
    @Test
    public void testSmallBulks() throws Exception {
        elasticSearchRule.startServer();

        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());

        try (final JestClient jestClient = restClientFactory.createClient()) {
            final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
            final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
            final FlowRepository flowRepository = createFlowRepository(jestClient, documentEnricher, 1000, 300000);

            flowRepository.persist(Lists.newArrayList(createMockedFlows(1000)), FlowDocumentTest.getMockFlowSource());

            // these results should appear immediately since the bulk size of 1000 was reached
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(Lists.newArrayList(createMockedFlows(500)), FlowDocumentTest.getMockFlowSource());

            // these results should not appear immediately since the bulk size is only 500
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(Lists.newArrayList(createMockedFlows(400)), FlowDocumentTest.getMockFlowSource());

            // these results should not appear immediately since the bulk size is only 900
            with().pollInterval(250, MILLISECONDS).await().atMost(10, SECONDS).until(() -> {
                final SearchResult searchResult = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", searchResult.isSucceeded() ? "Success" : "Failure", SearchResultUtils.getTotal(searchResult));
                return SearchResultUtils.getTotal(searchResult) == 1000L;
            });

            flowRepository.persist(Lists.newArrayList(createMockedFlows(100)), FlowDocumentTest.getMockFlowSource());

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
