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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.number.IsCloseTo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.elasticsearch.plugin.DriftPlugin;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockTransactionManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.opennms.plugins.elasticsearch.rest.template.IndexSettings;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
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
                    .withPlugins(DriftPlugin.class)
    );

    private ElasticFlowRepository flowRepository;

    @Before
    public void setUp() throws MalformedURLException, FlowException, ExecutionException, InterruptedException {
        MockLogAppender.setupLogging(true, "DEBUG");
        final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
        final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
        final ClassificationEngine classificationEngine = mockDocumentEnricherFactory.getClassificationEngine();

        final MetricRegistry metricRegistry = new MetricRegistry();
        final RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT, null, null);
        final JestClient client = restClientFactory.createClient();
        final MockTransactionTemplate mockTransactionTemplate = new MockTransactionTemplate();
        mockTransactionTemplate.setTransactionManager(new MockTransactionManager());
        flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY, documentEnricher,
                classificationEngine, mockTransactionTemplate, new MockNodeDao(), new MockSnmpInterfaceDao(), 3, 12000);
        final IndexSettings settings = new IndexSettings();
        final ElasticFlowRepositoryInitializer initializer = new ElasticFlowRepositoryInitializer(client, settings);

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        initializer.initialize();

        // The repository should be empty
        assertThat(flowRepository.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, 0))).get(), equalTo(0L));

        // Load the default set of flows
        loadDefaultFlows();
    }

    @Test
    public void canGetTopNApplications() throws ExecutionException, InterruptedException {
        // Retrieve the Top N applications over the entire time range
        List<TrafficSummary<String>> appTrafficSummary = flowRepository.getTopNApplications(10, true, getFilters()).get();

        // Expect all of the applications, with the sum of all the bytes from all the flows
        assertThat(appTrafficSummary, hasSize(4));
        TrafficSummary<String> https = appTrafficSummary.get(0);
        assertThat(https.getEntity(), equalTo("https"));
        assertThat(https.getBytesIn(), equalTo(210L));
        assertThat(https.getBytesOut(), equalTo(2100L));

        // Unclassified applications should show up too
        TrafficSummary<String> unknown = appTrafficSummary.get(1);
        assertThat(unknown.getEntity(), equalTo("Unknown"));
        assertThat(unknown.getBytesIn(), equalTo(200L));
        assertThat(unknown.getBytesOut(), equalTo(100L));

        TrafficSummary<String> http = appTrafficSummary.get(2);
        assertThat(http.getEntity(), equalTo("http"));
        assertThat(http.getBytesIn(), equalTo(10L));
        assertThat(http.getBytesOut(), equalTo(100L));

        TrafficSummary<String> other = appTrafficSummary.get(3);
        assertThat(other.getEntity(), equalTo("Other"));
        assertThat(other.getBytesIn(), equalTo(0L));
        assertThat(other.getBytesOut(), equalTo(0L));

        // Now decrease N, expect all of the counts to pool up in "Other"
        appTrafficSummary = flowRepository.getTopNApplications(1, true, getFilters()).get();

        // Expect all of the applications, with the sum of all the bytes from all the flows
        assertThat(appTrafficSummary, hasSize(2));
        https = appTrafficSummary.get(0);
        assertThat(https.getEntity(), equalTo("https"));
        assertThat(https.getBytesIn(), equalTo(210L));
        assertThat(https.getBytesOut(), equalTo(2100L));

        other = appTrafficSummary.get(1);
        assertThat(other.getEntity(), equalTo("Other"));
        assertThat(other.getBytesIn(), equalTo(210L));
        assertThat(other.getBytesOut(), equalTo(200L));

        // Now set N to zero
        appTrafficSummary = flowRepository.getTopNApplications(0, false, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(0));

        // N=0, but include other
        appTrafficSummary = flowRepository.getTopNApplications(0, true, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(1));

        other = appTrafficSummary.get(0);
        assertThat(other.getEntity(), equalTo("Other"));
        assertThat(other.getBytesIn(), equalTo(420L));
        assertThat(other.getBytesOut(), equalTo(2300L));
    }

    @Test
    public void canGetTopNApplicationsWithPartialSums() throws ExecutionException, InterruptedException {
        // Retrieve the Top N applications over a subset of the range
        final List<TrafficSummary<String>> appTrafficSummary = flowRepository.getTopNApplications(1, false,
                Lists.newArrayList(new TimeRangeFilter(10,  20))).get();

        // Expect the top application with a partial sum of all the bytes
        assertThat(appTrafficSummary, hasSize(1));
        final TrafficSummary<String> HTTPS = appTrafficSummary.get(0);
        assertThat(HTTPS.getEntity(), equalTo("https"));
        assertThat(HTTPS.getBytesIn(), equalTo(75L));
        assertThat(HTTPS.getBytesOut(), equalTo(751L));
    }

    @Test
    public void canGetTopNConversations() throws ExecutionException, InterruptedException {
        // Retrieve the Top N conversation over the entire time range
        final List<TrafficSummary<Conversation>> convoTrafficSummary = flowRepository.getTopNConversations(2, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(2));

        // Expect the conversations, with the sum of all the bytes from all the flows
        TrafficSummary<Conversation> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getKey().getSrcIp(), equalTo("192.168.1.101"));
        assertThat(convo.getEntity().getKey().getDstIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(110L));
        assertThat(convo.getBytesOut(), equalTo(1100L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity().getKey().getSrcIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getKey().getDstIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(100L));
        assertThat(convo.getBytesOut(), equalTo(1000L));
    }

    @Test
    public void canGetTopNAppsSeries() throws ExecutionException, InterruptedException {
        // Top 10
        Table<Directional<String>, Long, Double> appTraffic = flowRepository.getTopNApplicationsSeries(10, 10, false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 2 with others
        appTraffic = flowRepository.getTopNApplicationsSeries(2, 10, true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 1
        appTraffic = flowRepository.getTopNApplicationsSeries(1, 10, false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        assertThat(appTraffic.rowKeySet(), containsInAnyOrder(new Directional<>("https", true),
                new Directional<>("https", false)));

        // Pull the values from the table into arrays for easy comparision and validate
        List<Long> timestamps = getTimestampsFrom(appTraffic);
        List<Double> httpsIngressValues = getValuesFor(new Directional<>("https", true), appTraffic);
        List<Double> httpsEgressValues = getValuesFor(new Directional<>("https", false), appTraffic);

        // In the range t=[10,20) there are 2 active flows with dstport=443:
        //   100 bytes from [13,26]
        //      = rate of 100/(26-13)
        //      = 7.6923 b/ms
        //   7 ms was spent in the range, so 7 * 7.6923 = 53.8461 bytes
        //
        //   110 bytes from [14,45]
        //      = rate of 110/(45-14)
        //      = 3.5484 b/ms
        //   6 ms was spent in the range, so 6 * 3.5484 = 21.2904 bytes
        //
        //   53.8461 + 21.2904 = 75.1365
        final double error = 1E-8;
        assertThat(timestamps, contains(10L, 20L, 30L, 40L));
        assertThat(httpsIngressValues, containsDoubles(error, 75.136476426799, 81.63771712158808, 35.483870967741936, 17.741935483870968));
        assertThat(httpsEgressValues, containsDoubles(error, 751.36476426799, 816.3771712158809, 354.83870967741933, 177.41935483870967));
    }

    private static Matcher<Iterable<Double>> containsDoubles(double error, Double... items) {
        final List<Matcher<Double>> matchers = new ArrayList<>();
        for (Double item : items) {
            matchers.add(IsCloseTo.closeTo(item, error));
        }
        return new IsIterableContainingInOrder(matchers);
    }

    private static List<Long> getTimestampsFrom(Table<?, Long, Double> table) {
        return table.columnKeySet().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    private static List<Double> getValuesFor(Object rowKey, Table<?, Long, Double> table) {
        final List<Long> timestamps = getTimestampsFrom(table);
        final List<Double> column = new ArrayList<>(timestamps.size());
        for (Long ts : timestamps) {
            Double val = table.get(rowKey, ts);
            if (val == null) {
                val = Double.NaN;
            }
            column.add(val);
        }
        return column;
    }

    @Test
    public void canRetrieveTopNConversationsSeries() throws ExecutionException, InterruptedException {
        final Table<Directional<Conversation>, Long, Double> convoTraffic = flowRepository.getTopNConversationsSeries(10, 10, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(8));
    }

    private void loadDefaultFlows() throws FlowException {
        final List<FlowDocument> flows = new FlowBuilder()
                .withExporter("SomeFs", "SomeFid", 99)
                .withSnmpInterfaceId(98)
                // 192.168.1.100:43444 <-> 10.1.1.11:80 (110 bytes in [3,15])
                .withDirection(Direction.INGRESS)
                .withFlow(new Date(3), new Date(15), "192.168.1.100", 43444, "10.1.1.11", 80, 10)
                .withDirection(Direction.EGRESS)
                .withFlow(new Date(3), new Date(15), "10.1.1.11", 80, "192.168.1.100", 43444, 100)
                // 192.168.1.100:43445 <-> 10.1.1.12:443 (1100 bytes in [13,26])
                .withDirection(Direction.INGRESS)
                .withFlow(new Date(13), new Date(26), "192.168.1.100", 43445, "10.1.1.12", 443, 100)
                .withDirection(Direction.EGRESS)
                .withFlow(new Date(13), new Date(26), "10.1.1.12", 443, "192.168.1.100", 43445, 1000)
                // 192.168.1.101:43442 <-> 10.1.1.12:443 (1210 bytes in [14, 45])
                .withDirection(Direction.INGRESS)
                .withFlow(new Date(14), new Date(45), "192.168.1.101", 43442, "10.1.1.12", 443, 110)
                .withDirection(Direction.EGRESS)
                .withFlow(new Date(14), new Date(45), "10.1.1.12", 443, "192.168.1.101", 43442, 1100)
                // 192.168.1.102:50000 <-> 10.1.1.13:50001 (200 bytes in [50, 52])
                .withDirection(Direction.INGRESS)
                .withFlow(new Date(50), new Date(52), "192.168.1.102", 50000, "10.1.1.13", 50001, 200)
                .withDirection(Direction.EGRESS)
                .withFlow(new Date(50), new Date(52), "10.1.1.13", 50001, "192.168.1.102", 50000, 100)
                .build();
        flowRepository.enrichAndPersistFlows(flows, new FlowSource("test", "127.0.0.1"));

        // Retrieve all the flows we just persisted
        await().atMost(30, TimeUnit.SECONDS).until(() -> flowRepository.getFlowCount(Collections.singletonList(
                new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo(Long.valueOf(flows.size())));
    }

    private List<Filter> getFilters(Filter... filters) {
        final List<Filter> filterList = Lists.newArrayList(filters);
        filterList.add(new TimeRangeFilter(0, System.currentTimeMillis()));
        return filterList;
    }
}
