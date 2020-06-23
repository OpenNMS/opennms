/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class DefaultDirectionIT {
    private static Logger LOG = LoggerFactory.getLogger(DefaultDirectionIT.class);

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withStartDelay(0)
            .withManualStartup()
    );

    private Flow getMockFlowWithoutDirection() {
        final Flow flow = mock(Flow.class);
        when(flow.getNetflowVersion()).thenReturn(Flow.NetflowVersion.V5);
        when(flow.getIpProtocolVersion()).thenReturn(4);
        when(flow.getSrcAddr()).thenReturn("192.168.1.2");
        when(flow.getDstAddr()).thenReturn("192.168.2.2");
        when(flow.getSrcAddrHostname()).thenReturn(Optional.empty());
        when(flow.getDstAddrHostname()).thenReturn(Optional.empty());
        when(flow.getNextHopHostname()).thenReturn(Optional.empty());
        when(flow.getVlan()).thenReturn(null);
        return flow;
    }

    @Test
    public void testDefaultDirection() throws Exception {
        // start ES
        elasticSearchRule.startServer();

        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());

        try (final JestClient jestClient = restClientFactory.createClient()) {
            final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
            final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
            final ClassificationEngine classificationEngine = mockDocumentEnricherFactory.getClassificationEngine();
            final FlowRepository elasticFlowRepository = new InitializingFlowRepository(
                    new ElasticFlowRepository(new MetricRegistry(), jestClient, IndexStrategy.MONTHLY, documentEnricher,
                            classificationEngine, new MockSessionUtils(), new MockNodeDao(), new MockSnmpInterfaceDao(),
                            new MockIdentity(), new MockTracerRegistry(), new IndexSettings(),
                            3, 12000), jestClient);
            // persist data
            elasticFlowRepository.persist(Lists.newArrayList(getMockFlowWithoutDirection()),
                    FlowDocumentTest.getMockFlowSource());

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
            assertEquals("ingress", sourceJsonObject.get("netflow.direction"));
        }

        // stop ES
        elasticSearchRule.stopServer();
    }
}
