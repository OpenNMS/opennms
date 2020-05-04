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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.values.TimestampedValue;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.number.IsCloseTo;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.elasticsearch.plugin.DriftPlugin;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.nephron.NephronOptions;
import org.opennms.nephron.Pipeline;
import org.opennms.nephron.coders.FlowDocumentProtobufCoder;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.NodeInfo;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.persistence.FlowDocumentBuilder;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import io.searchbox.client.JestClient;

/**
 * Similar to {@link FlowQueryIT}, but adapted for aggregated queries.
 */
public class AggregatedFlowQueryIT {

    @Rule
    public TestPipeline p = TestPipeline.create();

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
                    .withPlugins(DriftPlugin.class));

    private ElasticFlowRepository flowRepository;
    private MockDocumentForwarder documentForwarder = new MockDocumentForwarder();
    private RawFlowQueryService rawFlowQueryService;
    private AggregatedFlowQueryService aggFlowQueryService;

    @Before
    public void setUp() throws MalformedURLException, FlowException, ExecutionException, InterruptedException {
        MockLogAppender.setupLogging(true, "DEBUG");
        final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
        final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();

        final ClassificationEngine classificationEngine = mockDocumentEnricherFactory.getClassificationEngine();

        final MetricRegistry metricRegistry = new MetricRegistry();
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final JestClient client = restClientFactory.createClient();
        final IndexSettings rawIndexSettings = new IndexSettings();
        rawIndexSettings.setIndexPrefix("flows");
        final IndexSettings aggIndexSettings = new IndexSettings();
        aggIndexSettings.setIndexPrefix("aggflows");

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        final RawIndexInitializer initializer = new RawIndexInitializer(client, rawIndexSettings);
        initializer.initialize();

        final IndexSelector rawIndexSelector = new IndexSelector(rawIndexSettings, RawFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        rawFlowQueryService = new RawFlowQueryService(client, rawIndexSelector);

        final AggregateIndexInitializer aggIndexInitializer = new AggregateIndexInitializer(client, aggIndexSettings);
        aggIndexInitializer.initialize();

        final IndexSelector aggIndexSelector = new IndexSelector(aggIndexSettings, AggregatedFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        aggFlowQueryService = new AggregatedFlowQueryService(client, aggIndexSelector);

        SmartQueryService smartQueryService = new SmartQueryService(metricRegistry, rawFlowQueryService, aggFlowQueryService);
        // Prefer aggregated queries, but fallback to raw when unsupported by agg.
        smartQueryService.setAlwaysUseAggForQueries(false);
        smartQueryService.setAlwaysUseRawForQueries(false);
        smartQueryService.setTimeRangeDurationAggregateThresholdMs(1);

        flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY, documentEnricher,
            new MockSessionUtils(), new MockNodeDao(), new MockSnmpInterfaceDao(),
            new MockIdentity(), new MockTracerRegistry(), documentForwarder, rawIndexSettings,
                smartQueryService);
        flowRepository.setEnableFlowForwarding(true);

        // The repository should be empty
        assertThat(flowRepository.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, 0))).get(), equalTo(0L));
    }

    @Test
    public void canGetApplications() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get only the first application
        List<String> applications = flowRepository.getApplications("", 1, getFilters()).get();
        assertThat(applications, equalTo(Collections.singletonList("http")));

        // Get both applications
        applications = flowRepository.getApplications("", 10, getFilters()).get();
        assertThat(applications, equalTo(Arrays.asList("http", "https")));

        // Get the first N applications with a prefix
        applications = flowRepository.getApplications("h", 10, getFilters()).get();
        assertThat(applications, equalTo(Arrays.asList("http", "https")));

        // Test the fuzzy matching
        applications = flowRepository.getApplications("httz", 10, getFilters()).get();
        assertThat(applications, equalTo(Collections.singletonList("http")));
        applications = flowRepository.getApplications("hyyps", 10, getFilters()).get();
        assertThat(applications, Matchers.empty());
    }

    @Test
    public void canGetTopNApplicationSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N applications over the entire time range
        List<TrafficSummary<String>> appTrafficSummary = flowRepository.getTopNApplicationSummaries(10, true, getFilters()).get();

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
        appTrafficSummary = flowRepository.getTopNApplicationSummaries(1, true, getFilters()).get();

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
        appTrafficSummary = flowRepository.getTopNApplicationSummaries(0, false, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(0));

        // N=0, but include other
        appTrafficSummary = flowRepository.getTopNApplicationSummaries(0, true, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(1));

        other = appTrafficSummary.get(0);
        assertThat(other.getEntity(), equalTo("Other"));
        assertThat(other.getBytesIn(), equalTo(420L));
        assertThat(other.getBytesOut(), equalTo(2300L));
    }

    @Test
    public void canGetAppSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        List<TrafficSummary<String>> appTrafficSummary =
                flowRepository.getApplicationSummaries(Collections.singleton("https"), false, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(1));

        appTrafficSummary =
                flowRepository.getApplicationSummaries(ImmutableSet.of("https", "http"), false, getFilters()).get();

        assertThat(appTrafficSummary, hasSize(2));
        TrafficSummary<String> https = appTrafficSummary.get(0);
        assertThat(https.getEntity(), equalTo("https"));
        assertThat(https.getBytesIn(), equalTo(210L));
        assertThat(https.getBytesOut(), equalTo(2100L));

        TrafficSummary<String> http = appTrafficSummary.get(1);
        assertThat(http.getEntity(), equalTo("http"));
        assertThat(http.getBytesIn(), equalTo(10L));
        assertThat(http.getBytesOut(), equalTo(100L));
    }

    @Test
    public void canGetTopNAppsSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Top 10
        long step = TimeUnit.MINUTES.toMillis(1);
        Table<Directional<String>, Long, Double> appTraffic = flowRepository.getTopNApplicationSeries(10, step, false,
                getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 2 with others
        appTraffic = flowRepository.getTopNApplicationSeries(2, step, true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 1
        appTraffic = flowRepository.getTopNApplicationSeries(1, step, false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        assertThat(appTraffic.rowKeySet(), containsInAnyOrder(new Directional<>("https", true),
                new Directional<>("https", false)));

        verifyHttpsSeriesAggregated(appTraffic, "https");
    }

    @Test
    public void canGetAppSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get just https
        Table<Directional<String>, Long, Double> appTraffic =
                flowRepository.getApplicationSeries(Collections.singleton("https"), 10,
                        false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(appTraffic, "https");

        // Get just https and include others
        appTraffic = flowRepository.getApplicationSeries(Collections.singleton("https"), 10,
                true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(4));

        // Get https and http
        appTraffic = flowRepository.getApplicationSeries(ImmutableSet.of("http", "https"), 10,
                false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(4));

        // Get https and http and include others
        appTraffic = flowRepository.getApplicationSeries(ImmutableSet.of("http", "https"), 10,
                true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));
    }

    @Test
    public void canGetHosts() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get only the first host
        List<String> hosts = flowRepository.getHosts(".*", 1, getFilters()).get();
        assertThat(hosts, equalTo(Collections.singletonList("10.1.1.11")));

        // Get first 10 hosts
        hosts = flowRepository.getHosts(".*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13", "192.168.1.100",
                "192.168.1.101", "192.168.1.102")));

        // Get the first 10 hosts with a prefix
        hosts = flowRepository.getHosts("10.1.1.*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13")));

        // Find all the hosts using a regex
        hosts = flowRepository.getHosts("10.1.*|192.168.*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13", "192.168.1.100",
                "192.168.1.101", "192.168.1.102")));
    }

    @Test
    public void canGetTopNHostSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N applications over the entire time range
        List<TrafficSummary<Host>> hostTrafficSummary = flowRepository.getTopNHostSummaries(10, false, getFilters()).get();

        // Expect all of the hosts, with the sum of all the bytes from all the flows
        assertThat(hostTrafficSummary, hasSize(6));
        TrafficSummary<Host> top = hostTrafficSummary.get(0);
        assertThat(top.getEntity(), equalTo(new Host("10.1.1.12")));
        assertThat(top.getBytesIn(), equalTo(210L));
        assertThat(top.getBytesOut(), equalTo(2100L));

        TrafficSummary<Host> bottom = hostTrafficSummary.get(5);
        assertThat(bottom.getEntity(), equalTo(new Host("10.1.1.11")));
        assertThat(bottom.getBytesIn(), equalTo(10L));
        assertThat(bottom.getBytesOut(), equalTo(100L));

        // Now decrease N, expect all of the counts to pool up in "Other"
        hostTrafficSummary = flowRepository.getTopNHostSummaries(1, true, getFilters()).get();

        // Expect two summaries
        assertThat(hostTrafficSummary, hasSize(2));
        top = hostTrafficSummary.get(0);
        assertThat(top.getEntity(), equalTo(new Host("10.1.1.12")));
        assertThat(top.getBytesIn(), equalTo(210L));
        assertThat(top.getBytesOut(), equalTo(2100L));

        TrafficSummary<Host> other = hostTrafficSummary.get(1);
        assertThat(other.getEntity(), equalTo(new Host("Other")));
        assertThat(other.getBytesIn(), equalTo(210L));
        assertThat(other.getBytesOut(), equalTo(200L));

        // Now set N to zero
        hostTrafficSummary = flowRepository.getTopNHostSummaries(0, false, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(0));

        // N=0, but include other
        hostTrafficSummary = flowRepository.getTopNHostSummaries(0, true, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(1));
        other = hostTrafficSummary.get(0);
        assertThat(other.getEntity(), equalTo(new Host("Other")));
        assertThat(other.getBytesIn(), equalTo(420L));
        assertThat(other.getBytesOut(), equalTo(2300L));
    }

    @Test
    public void canGetHostSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get one specific host and no others
        List<TrafficSummary<Host>> hostTrafficSummary =
                flowRepository.getHostSummaries(Collections.singleton("10.1.1.12"), false, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(1));

        // Get summaries for two specific hosts
        hostTrafficSummary =
                flowRepository.getHostSummaries(ImmutableSet.of("10.1.1.11", "10.1.1.12"), false, getFilters()).get();

        assertThat(hostTrafficSummary, hasSize(2));
        TrafficSummary<Host> first = hostTrafficSummary.get(0);
        assertThat(first.getEntity().getIp(), equalTo("10.1.1.11"));
        assertThat(first.getBytesIn(), equalTo(10L));
        assertThat(first.getBytesOut(), equalTo(100L));

        TrafficSummary<Host> second = hostTrafficSummary.get(1);
        assertThat(second.getEntity().getIp(), equalTo("10.1.1.12"));
        assertThat(second.getBytesIn(), equalTo(210L));
        assertThat(second.getBytesOut(), equalTo(2100L));

        // Try with only one host to let Others accumulate the rest
        hostTrafficSummary =
                flowRepository.getHostSummaries(ImmutableSet.of("10.1.1.11"), true, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(2));
        first = hostTrafficSummary.get(0);
        assertThat(first.getEntity().getIp(), equalTo("10.1.1.11"));
        assertThat(first.getBytesIn(), equalTo(10L));
        assertThat(first.getBytesOut(), equalTo(100L));

        second = hostTrafficSummary.get(1);
        assertThat(second.getEntity().getIp(), equalTo("Other"));
        assertThat(second.getBytesIn(), equalTo(410L));
        assertThat(second.getBytesOut(), equalTo(2200L));
    }

    @Test
    public void canGetTopNHostSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Top 10
        long step = TimeUnit.MINUTES.toMillis(1);
        Table<Directional<Host>, Long, Double> hostTraffic = flowRepository.getTopNHostSeries(10, step, false,
                getFilters()).get();
        // 6 hosts in two directions should yield 12 rows
        assertThat(hostTraffic.rowKeySet(), hasSize(12));

        // Top 2 with others
        hostTraffic = flowRepository.getTopNHostSeries(2, step, true, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(6));
        // Top 1
        hostTraffic = flowRepository.getTopNHostSeries(1, step, false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(2));
        assertThat(hostTraffic.rowKeySet(), containsInAnyOrder(new Directional<>(new Host("10.1.1.12"), true),
                new Directional<>(new Host("10.1.1.12"), false)));
        verifyHttpsSeriesAggregated(hostTraffic, new Host("10.1.1.12"));
    }

    @Test
    public void canGetHostSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get just https
        Table<Directional<Host>, Long, Double> hostTraffic =
                flowRepository.getHostSeries(Collections.singleton("10.1.1.12"), 10,
                        false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(hostTraffic, new Host("10.1.1.12"));

        // Get just 10.1.1.12 and include others
        hostTraffic = flowRepository.getHostSeries(Collections.singleton("10.1.1.12"), 10,
                true, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(4));

        // Get 10.1.1.12 and 192.168.1.100
        hostTraffic = flowRepository.getHostSeries(ImmutableSet.of("10.1.1.12", "192.168.1.100"), 10,
                false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(4));

        // Get 10.1.1.12 and 192.168.1.100 and include others
        hostTraffic = flowRepository.getHostSeries(ImmutableSet.of("10.1.1.12", "192.168.1.100"), 10,
                true, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(6));
    }

    @Ignore
    @Test
    public void canGetConversations() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get all the conversations
        List<String> conversations =
                flowRepository.getConversations(".*", ".*", ".*", ".*", ".*", 10, getFilters()).get();
        assertThat(conversations, hasSize(4));

        // Find all the conversations with a null application
        conversations =
                flowRepository.getConversations(".*", ".*", ".*", ".*", "null", 10, getFilters()).get();
        assertThat(conversations, hasSize(1));

        // Find all the conversations involving 10.1.1.12 as the lower IP
        conversations =
                flowRepository.getConversations(".*", ".*", "10.1.1.12", ".*", ".*", 10, getFilters()).get();
        assertThat(conversations, hasSize(2));

        // Find a specific conversation
        conversations =
                flowRepository.getConversations("test", "6", "10.1.1.11", "192.168.1.100", "http", 10, getFilters()).get();
        assertThat(conversations, hasSize(1));
        assertThat(conversations.iterator().next(), equalTo("[\"test\",6,\"10.1.1.11\",\"192.168.1.100\",\"http\"]"));

    }

    @Test
    public void canGetTopNConversationSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N conversation over the entire time range
        List<TrafficSummary<Conversation>> convoTrafficSummary = flowRepository.getTopNConversationSummaries(2, false, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(2));

        // Expect the conversations, with the sum of all the bytes from all the flows
        TrafficSummary<Conversation> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.101"));
        // Disabled for now - see https://issues.opennms.org/browse/NMS-12692
        // assertThat(convo.getEntity().getLowerHostname(), equalTo(Optional.of("la.le.lu")));
        // assertThat(convo.getEntity().getUpperHostname(), equalTo(Optional.of("ingress.only")));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(110L));
        assertThat(convo.getBytesOut(), equalTo(1100L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.100"));
        // Disabled for now - see https://issues.opennms.org/browse/NMS-12692
        //assertThat(convo.getEntity().getLowerHostname(), equalTo(Optional.of("la.le.lu")));
        //assertThat(convo.getEntity().getUpperHostname(), equalTo(Optional.empty()));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(100L));
        assertThat(convo.getBytesOut(), equalTo(1000L));

        // Get the top 1 plus others
        convoTrafficSummary = flowRepository.getTopNConversationSummaries(1, true, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(2));

        convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.101"));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(110L));
        assertThat(convo.getBytesOut(), equalTo(1100L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity(), equalTo(Conversation.forOther().build()));
        assertThat(convo.getBytesIn(), equalTo(310L));
        assertThat(convo.getBytesOut(), equalTo(1200L));
    }

    @Test
    public void canGetConversationSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get a specific conversation
        List<TrafficSummary<Conversation>> convoTrafficSummary =
                flowRepository.getConversationSummaries(ImmutableSet.of("[\"test\",6,\"10.1.1.11\",\"192.168.1.100\",\"http\"]"),
                        false, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(1));
        TrafficSummary<Conversation> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.11"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getApplication(), equalTo("http"));
        assertThat(convo.getBytesIn(), equalTo(10L));
        assertThat(convo.getBytesOut(), equalTo(100L));

        // Get a specific conversation plus others
        convoTrafficSummary = flowRepository.getConversationSummaries(
                ImmutableSet.of("[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]"), true,
                getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(2));
        convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(100L));
        assertThat(convo.getBytesOut(), equalTo(1000L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity().getLowerIp(), equalTo("Other"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("Other"));
        assertThat(convo.getEntity().getApplication(), equalTo("Other"));
        assertThat(convo.getBytesIn(), equalTo(320L));
        assertThat(convo.getBytesOut(), equalTo(1300L));

    }

    @Test
    public void canGetConversationSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get series for specific host
        Table<Directional<Conversation>, Long, Double> convoTraffic = flowRepository.getConversationSeries(ImmutableSet.of("[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]"), 10, false, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(convoTraffic, Conversation.builder()
                .withLocation("test")
                .withProtocol(6)
                .withLowerIp("10.1.1.12")
                .withLowerHostname("la.le.lu")
                .withUpperIp("192.168.1.100")
                .withApplication("https").build());

        // Get series for same host and include others
        convoTraffic = flowRepository.getConversationSeries(ImmutableSet.of("[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]"), 10, true, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(4));
    }

    @Test
    public void canRetrieveTopNConversationsSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Top 10
        long step = TimeUnit.MINUTES.toMillis(1);
        Table<Directional<Conversation>, Long, Double> convoTraffic = flowRepository.getTopNConversationSeries(10, step, false, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(8));

        // Top 2 with others
        convoTraffic = flowRepository.getTopNConversationSeries(2, step, true, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(6));
    }

    private <L> void verifyHttpsSeriesAggregated(Table<Directional<L>, Long, Double> appTraffic, L label) {
        // Pull the values from the table into arrays for easy comparision and validate
        List<Long> timestamps = getTimestampsFrom(appTraffic);
        List<Double> httpsIngressValues = getValuesFor(new Directional<>(label, true), appTraffic);
        List<Double> httpsEgressValues = getValuesFor(new Directional<>(label, false), appTraffic);

        // In the range t=[10,20) there are 2 active flows with dstport=443:
        //   100 bytes from [13,26]
        //
        //   110 bytes from [14,45]
        //
        final double error = 1E-8;
        assertThat(timestamps, contains(0L, 60000L));
        assertThat(httpsIngressValues, containsDoubles(error, 210.0, 0.0));
        assertThat(httpsEgressValues, containsDoubles(error, 2100.0, 0.0));
    }

    private <L> void verifyHttpsSeries(Table<Directional<L>, Long, Double> appTraffic, L label) {
        // Pull the values from the table into arrays for easy comparision and validate
        List<Long> timestamps = getTimestampsFrom(appTraffic);
        List<Double> httpsIngressValues = getValuesFor(new Directional<>(label, true), appTraffic);
        List<Double> httpsEgressValues = getValuesFor(new Directional<>(label, false), appTraffic);

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
        assertThat(httpsIngressValues, containsDoubles(error, 75.136476426799, 81.63771712158808, 35.483870967741936,
                17.741935483870968));
        assertThat(httpsEgressValues, containsDoubles(error, 751.36476426799, 816.3771712158809, 354.83870967741933,
                177.41935483870967));
    }

    private void verifyHttpsSeries(Table<Directional<Conversation>, Long, Double> convoTraffic, Conversation label) {
        // Pull the values from the table into arrays for easy comparision and validate
        List<Long> timestamps = getTimestampsFrom(convoTraffic);
        List<Double> httpsIngressValues = getValuesFor(new Directional<>(label, true), convoTraffic);
        List<Double> httpsEgressValues = getValuesFor(new Directional<>(label, false), convoTraffic);

        // In the range t=[10,20) for this conversation between 10.1.1.12 and 192.168.1.100 https:
        //   100 bytes from [13,26]
        //      = rate of 100/(26-13)
        //      = 7.6923 b/ms
        //   7 ms was spent in the range, so 7 * 7.6923 = 53.8461 bytes
        final double error = 1E-8;
        assertThat(timestamps, contains(10L, 20L));
        assertThat(httpsIngressValues, containsDoubles(error, 53.84615384615385, 46.15384615384615));
        assertThat(httpsEgressValues, containsDoubles(error, 538.4615384615385, 461.53846153846155));
    }

    private static Matcher<Iterable<Double>> containsDoubles(double error, Double... items) {
        final List<Matcher<Double>> matchers = new ArrayList<>();
        for (Double item : items) {
            matchers.add(IsCloseTo.closeTo(item, error));
        }
        return new IsIterableContainingInOrder(matchers);
    }

    private static <R> List<Long> getTimestampsFrom(Table<R, Long, Double> table) {
        return table.columnKeySet().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    private static <R> List<Double> getValuesFor(R rowKey, Table<R, Long, Double> table) {
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
                .withHostnames(null, "la.le.lu")
                .withFlow(new Date(13), new Date(26), "192.168.1.100", 43445, "10.1.1.12", 443, 100)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withFlow(new Date(13), new Date(26), "10.1.1.12", 443, "192.168.1.100", 43445, 1000)
                // 192.168.1.101:43442 <-> 10.1.1.12:443 (1210 bytes in [14, 45])
                .withDirection(Direction.INGRESS)
                .withHostnames("ingress.only", "la.le.lu")
                .withFlow(new Date(14), new Date(45), "192.168.1.101", 43442, "10.1.1.12", 443, 110)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withFlow(new Date(14), new Date(45), "10.1.1.12", 443, "192.168.1.101", 43442, 1100)
                // 192.168.1.102:50000 <-> 10.1.1.13:50001 (200 bytes in [50, 52])
                .withDirection(Direction.INGRESS)
                .withFlow(new Date(50), new Date(52), "192.168.1.102", 50000, "10.1.1.13", 50001, 200)
                .withDirection(Direction.EGRESS)
                .withFlow(new Date(50), new Date(52), "10.1.1.13", 50001, "192.168.1.102", 50000, 100)
                .build();

        this.loadFlows(flows, 17);
    }

    private void loadFlows(final List<FlowDocument> flowDocuments, long expectedNumFlowSummaries) throws FlowException {
        final List<Flow> flows = new ArrayList<>();
        for (FlowDocument flow : flowDocuments) {
            TestFlow testFlow = new TestFlow(flow);
            flow.setFlow(testFlow);
            flows.add(testFlow);
        }
        flowRepository.persist(flows, new FlowSource("test", "127.0.0.1", null));

        // Retrieve all the flows we just persisted
        await().atMost(60, TimeUnit.SECONDS).until(() -> rawFlowQueryService.getFlowCount(Collections.singletonList(
                new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo(Long.valueOf(flows.size())));

        // Pass those same flows through the pipeline and persist the aggregations
        NephronOptions options = PipelineOptionsFactory.as(NephronOptions.class);
        doPipeline(documentForwarder.getFlows().stream()
                .map( flow -> {
                    flow.setExporterNodeInfo(new NodeInfo() {
                        @Override
                        public Integer getNodeId() {
                            return 1;
                        }

                        @Override
                        public String getForeignId() {
                            return "SomeFID";
                        }

                        @Override
                        public String getForeignSource() {
                            return "SomeFS";
                        }

                        @Override
                        public List<String> getCategories() {
                            return Collections.emptyList();
                        }
                    });
                    return FlowDocumentBuilder.buildFlowDocument(flow);
                })
                .collect(Collectors.toList()), options);

        // Count the number aggregated flows we persisted
        // Wait for these to be present to ensure the tests have a consistent view of the data
        // This value will need to be updated if/when the flow aggregation logic changes
        await().atMost(60, TimeUnit.SECONDS).until(() -> aggFlowQueryService.getFlowCount(Collections.singletonList(
                new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo(expectedNumFlowSummaries));
    }

    private void doPipeline(List<org.opennms.netmgt.flows.persistence.model.FlowDocument> flows, NephronOptions options) {
        Pipeline.registerCoders(p);

        // Build a stream from the given set of flows
        long timestampOffsetMillis = TimeUnit.MINUTES.toMillis(1);
        TestStream.Builder<org.opennms.netmgt.flows.persistence.model.FlowDocument> flowStreamBuilder = TestStream.create(new FlowDocumentProtobufCoder());
        for (org.opennms.netmgt.flows.persistence.model.FlowDocument flow : flows) {
            flowStreamBuilder = flowStreamBuilder.addElements(TimestampedValue.of(flow,
                    new Instant(flow.getLastSwitched().getValue() + timestampOffsetMillis)));
        }
        TestStream<org.opennms.netmgt.flows.persistence.model.FlowDocument> flowStream = flowStreamBuilder.advanceWatermarkToInfinity();

        // Build the pipeline
        options.setElasticUrl(elasticSearchRule.getUrl());
        // Must match!
        options.setElasticIndexStrategy(org.opennms.nephron.elastic.IndexStrategy.MONTHLY);
        options.setElasticFlowIndex("aggflowsnetflow_agg");

        p.apply(flowStream)
                .apply(new Pipeline.CalculateFlowStatistics(options))
                .apply(new Pipeline.WriteToElasticsearch(options));

        // Run the pipeline until completion
        p.run().waitUntilFinish();
    }

    private List<Filter> getFilters(Filter... filters) {
        final List<Filter> filterList = Lists.newArrayList(filters);
        filterList.add(new TimeRangeFilter(0, System.currentTimeMillis()));
        // Match the SNMP interface id in the flows
        filterList.add(new SnmpInterfaceIdFilter(98));
        return filterList;
    }
}
