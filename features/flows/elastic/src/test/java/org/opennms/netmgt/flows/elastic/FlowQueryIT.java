/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.elastic.template.IndexSettings;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Table;

import io.searchbox.client.JestClient;

public class FlowQueryIT {

    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule(
            new ElasticSearchServerConfig()
                    .withDefaults()
                    .withSetting("http.enabled", true)
                    .withSetting("http.port", HTTP_PORT)
                    .withSetting("http.type", "netty4")
                    .withSetting("transport.type", "netty4")
                    .withSetting("transport.tcp.port", HTTP_TRANSPORT_PORT)
    );

    private ElasticFlowRepository flowRepository;

    @Before
    public void setUp() throws MalformedURLException, FlowException, ExecutionException, InterruptedException {
        MockLogAppender.setupLogging(true, "DEBUG");
        final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
        final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();

        final MetricRegistry metricRegistry = new MetricRegistry();
        final RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT, null, null);
        final JestClient client = restClientFactory.createClient();
        flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY, documentEnricher);
        final IndexSettings settings = new IndexSettings();
        final ElasticFlowRepositoryInitializer initializer = new ElasticFlowRepositoryInitializer(client, settings);

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        initializer.initialize();

        // The repository should be empty
        assertThat(flowRepository.getFlowCount(0, 0).get(), equalTo(0L));

        // Load the default set of flows
        loadDefaultFlows();
    }

    @Test
    public void canRetrieveTopNApps() throws ExecutionException, InterruptedException {
        final List<TrafficSummary<String>> appTrafficSummary = flowRepository.getTopNApplications(10, 0, 100).get();
        assertThat(appTrafficSummary, hasSize(2));
        final TrafficSummary<String> HTTPS = appTrafficSummary.get(0);
        assertThat(HTTPS.getEntity(), equalTo("https"));
        assertThat(HTTPS.getBytesIn(), equalTo(210L));
        assertThat(HTTPS.getBytesOut(), equalTo(2100L));

        final TrafficSummary<String> HTTP = appTrafficSummary.get(1);
        assertThat(HTTP.getEntity(), equalTo("http"));
        assertThat(HTTP.getBytesIn(), equalTo(10L));
        assertThat(HTTP.getBytesOut(), equalTo(100L));
    }

    @Test
    public void canRetrieveTopNAppsSeries() throws ExecutionException, InterruptedException {
        // Top 10
        Table<Directional<String>, Long, Double> appTraffic = flowRepository.getTopNApplicationsSeries(10, 0, 100, 10).get();
        assertThat(appTraffic.rowKeySet(), hasSize(4));

        // Top 1
        appTraffic = flowRepository.getTopNApplicationsSeries(1, 0, 100, 10).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        assertThat(appTraffic.rowKeySet(), containsInAnyOrder(new Directional<>("https", true),
                new Directional<>("https", false)));
    }

    @Test
    public void canRetrieveTopNConversations() throws ExecutionException, InterruptedException {
        final List<TrafficSummary<ConversationKey>> convoTrafficSummary = flowRepository.getTopNConversations(2, 0, 100).get();
        assertThat(convoTrafficSummary, hasSize(2));

        TrafficSummary<ConversationKey> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getSrcIp(), equalTo("192.168.1.101"));
        assertThat(convo.getEntity().getDstIp(), equalTo("10.1.1.12"));
        assertThat(convo.getBytesIn(), equalTo(110L));
        assertThat(convo.getBytesOut(), equalTo(1100L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity().getSrcIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getDstIp(), equalTo("10.1.1.12"));
        assertThat(convo.getBytesIn(), equalTo(100L));
        assertThat(convo.getBytesOut(), equalTo(1000L));
    }

    @Test
    public void canRetrieveTopNConversationsSeries() throws ExecutionException, InterruptedException {
        final Table<Directional<ConversationKey>, Long, Double> convoTraffic = flowRepository.getTopNConversationsSeries(10, 0, 100, 10).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(6));
    }

    private void loadDefaultFlows() throws FlowException {
        final List<FlowDocument> flows = new FlowBuilder()
                // 192.168.1.100:43444 <-> 10.1.1.11:80
                .withFlow(new Date(0), "192.168.1.100", 43444, "10.1.1.11", 80, 10)
                .withFlow(new Date(0), "10.1.1.11", 80, "192.168.1.100", 43444, 100)
                // 192.168.1.100:43445 <-> 10.1.1.12:443
                .withFlow(new Date(10), "192.168.1.100", 43445, "10.1.1.12", 443, 100)
                .withFlow(new Date(10), "10.1.1.12", 443, "192.168.1.100", 43445, 1000)
                // 192.168.1.101:43442 <-> 10.1.1.12:443
                .withFlow(new Date(10), "192.168.1.101", 43442, "10.1.1.12", 443, 110)
                .withFlow(new Date(10), "10.1.1.12", 443, "192.168.1.101", 43442, 1100)
                .build();
        flowRepository.enrichAndPersistFlows(flows.stream(), new FlowSource("test", "127.0.0.1"));

        // Retrieve all the flows we just persisted
        await().atMost(30, TimeUnit.SECONDS).until(() -> flowRepository.getFlowCount(0, System.currentTimeMillis()).get(), equalTo(Long.valueOf(flows.size())));
    }

}
