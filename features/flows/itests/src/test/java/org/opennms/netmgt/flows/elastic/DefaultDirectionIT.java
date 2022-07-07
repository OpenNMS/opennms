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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.flows.processing.persisting.FlowRepository;
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

        try (final JestClient jestClient = restClientFactory.createClient()) {
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
