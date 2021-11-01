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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.painless.PainlessPlugin;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.number.IsCloseTo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.elasticsearch.plugin.DriftPlugin;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.FilterVisitor;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import io.searchbox.client.JestClient;

public class FlowQueryIT {

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(DriftPlugin.class, PainlessPlugin.class));

    private ElasticFlowRepository flowRepository;

    private SmartQueryService smartQueryService;

    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException {
        final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
        final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();

        final MetricRegistry metricRegistry = new MetricRegistry();
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final JestClient client = restClientFactory.createClient();
        final IndexSettings settings = new IndexSettings();
        settings.setIndexPrefix("flows");
        final IndexSelector rawIndexSelector = new IndexSelector(settings, RawFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        final RawFlowQueryService rawFlowRepository = new RawFlowQueryService(client, rawIndexSelector);
        final AggregatedFlowQueryService aggFlowRepository = mock(AggregatedFlowQueryService.class);
        smartQueryService = new SmartQueryService(metricRegistry, rawFlowRepository, aggFlowRepository);
        smartQueryService.setAlwaysUseRawForQueries(true); // Always use RAW values for these tests
        flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY, documentEnricher,
                new MockSessionUtils(), new MockNodeDao(), new MockSnmpInterfaceDao(),
                new MockIdentity(), new MockTracerRegistry(), new MockDocumentForwarder(), settings, 0, 0);

        final RawIndexInitializer initializer = new RawIndexInitializer(client, settings);

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        initializer.initialize();

        // The repository should be empty
        assertThat(smartQueryService.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, 0))).get(), equalTo(0L));
    }

    @Test
    public void canGetApplications() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get only the first application
        List<String> applications = smartQueryService.getApplications("", 1, getFilters()).get();
        assertThat(applications, equalTo(Collections.singletonList("http")));

        // Get both applications
        applications = smartQueryService.getApplications("", 10, getFilters()).get();
        assertThat(applications, equalTo(Arrays.asList("http", "https")));

        // Get the first N applications with a prefix
        applications = smartQueryService.getApplications("h", 10, getFilters()).get();
        assertThat(applications, equalTo(Arrays.asList("http", "https")));

        // Test the fuzzy matching
        applications = smartQueryService.getApplications("httz", 10, getFilters()).get();
        assertThat(applications, equalTo(Collections.singletonList("http")));
        applications = smartQueryService.getApplications("hyyps", 10, getFilters()).get();
        assertThat(applications, Matchers.empty());
    }

    @Test
    public void canGetTopNApplicationSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N applications over the entire time range
        List<TrafficSummary<String>> appTrafficSummary = smartQueryService.getTopNApplicationSummaries(10, true, getFilters()).get();

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
        appTrafficSummary = smartQueryService.getTopNApplicationSummaries(1, true, getFilters()).get();

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
        appTrafficSummary = smartQueryService.getTopNApplicationSummaries(0, false, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(0));

        // N=0, but include other
        appTrafficSummary = smartQueryService.getTopNApplicationSummaries(0, true, getFilters()).get();
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
                smartQueryService.getApplicationSummaries(Collections.singleton("https"), false, getFilters()).get();
        assertThat(appTrafficSummary, hasSize(1));

        appTrafficSummary =
                smartQueryService.getApplicationSummaries(ImmutableSet.of("https", "http"), false, getFilters()).get();

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
        Table<Directional<String>, Long, Double> appTraffic = smartQueryService.getTopNApplicationSeries(10, 10, false,
                getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 2 with others
        appTraffic = smartQueryService.getTopNApplicationSeries(2, 10, true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));

        // Top 1
        appTraffic = smartQueryService.getTopNApplicationSeries(1, 10, false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        assertThat(appTraffic.rowKeySet(), containsInAnyOrder(new Directional<>("https", true),
                new Directional<>("https", false)));

        verifyHttpsSeries(appTraffic, "https");
    }

    @Test
    public void canGetAppSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get just https
        Table<Directional<String>, Long, Double> appTraffic =
                smartQueryService.getApplicationSeries(Collections.singleton("https"), 10,
                        false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(appTraffic, "https");

        // Get just https and include others
        appTraffic = smartQueryService.getApplicationSeries(Collections.singleton("https"), 10,
                true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(4));

        // Get https and http
        appTraffic = smartQueryService.getApplicationSeries(ImmutableSet.of("http", "https"), 10,
                false, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(4));

        // Get https and http and include others
        appTraffic = smartQueryService.getApplicationSeries(ImmutableSet.of("http", "https"), 10,
                true, getFilters()).get();
        assertThat(appTraffic.rowKeySet(), hasSize(6));
    }

    @Test
    public void canGetTopNApplicationsWithPartialSums() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N applications over a subset of the range
        final List<TrafficSummary<String>> appTrafficSummary = smartQueryService.getTopNApplicationSummaries(1, false,
                Lists.newArrayList(new TimeRangeFilter(10, 20))).get();

        // Expect the top application with a partial sum of all the bytes
        assertThat(appTrafficSummary, hasSize(1));
        final TrafficSummary<String> HTTPS = appTrafficSummary.get(0);
        assertThat(HTTPS.getEntity(), equalTo("https"));
        assertThat(HTTPS.getBytesIn(), equalTo(75L));
        assertThat(HTTPS.getBytesOut(), equalTo(751L));
    }

    @Test
    public void canGetHosts() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get only the first host
        List<String> hosts = smartQueryService.getHosts(".*", 1, getFilters()).get();
        assertThat(hosts, equalTo(Collections.singletonList("10.1.1.11")));

        // Get first 10 hosts
        hosts = smartQueryService.getHosts(".*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13", "192.168.1.100",
                "192.168.1.101", "192.168.1.102")));

        // Get the first 10 hosts with a prefix
        hosts = smartQueryService.getHosts("10.1.1.*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13")));

        // Find all the hosts using a regex
        hosts = smartQueryService.getHosts("10.1.*|192.168.*", 10, getFilters()).get();
        assertThat(hosts, equalTo(Arrays.asList("10.1.1.11", "10.1.1.12", "10.1.1.13", "192.168.1.100",
                "192.168.1.101", "192.168.1.102")));
    }

    @Test
    public void canGetTopNHostSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N applications over the entire time range
        List<TrafficSummary<Host>> hostTrafficSummary = smartQueryService.getTopNHostSummaries(10, false, getFilters()).get();

        // Expect all of the hosts, with the sum of all the bytes from all the flows
        assertThat(hostTrafficSummary, hasSize(6));
        TrafficSummary<Host> top = hostTrafficSummary.get(0);
        assertThat(top.getEntity(), equalTo(new Host("10.1.1.12", "la.le.lu")));
        assertThat(top.getBytesIn(), equalTo(210L));
        assertThat(top.getBytesOut(), equalTo(2100L));

        TrafficSummary<Host> bottom = hostTrafficSummary.get(5);
        assertThat(bottom.getEntity(), equalTo(new Host("10.1.1.11")));
        assertThat(bottom.getBytesIn(), equalTo(10L));
        assertThat(bottom.getBytesOut(), equalTo(100L));

        // Now decrease N, expect all of the counts to pool up in "Other"
        hostTrafficSummary = smartQueryService.getTopNHostSummaries(1, true, getFilters()).get();

        // Expect two summaries
        assertThat(hostTrafficSummary, hasSize(2));
        top = hostTrafficSummary.get(0);
        assertThat(top.getEntity(), equalTo(new Host("10.1.1.12", "la.le.lu")));
        assertThat(top.getBytesIn(), equalTo(210L));
        assertThat(top.getBytesOut(), equalTo(2100L));

        TrafficSummary<Host> other = hostTrafficSummary.get(1);
        assertThat(other.getEntity(), equalTo(new Host("Other")));
        assertThat(other.getBytesIn(), equalTo(210L));
        assertThat(other.getBytesOut(), equalTo(200L));

        // Now set N to zero
        hostTrafficSummary = smartQueryService.getTopNHostSummaries(0, false, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(0));

        // N=0, but include other
        hostTrafficSummary = smartQueryService.getTopNHostSummaries(0, true, getFilters()).get();
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
                smartQueryService.getHostSummaries(Collections.singleton("10.1.1.12"), false, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(1));

        // Get summaries for two specific hosts
        hostTrafficSummary =
                smartQueryService.getHostSummaries(ImmutableSet.of("10.1.1.11", "10.1.1.12"), false, getFilters()).get();

        assertThat(hostTrafficSummary, hasSize(2));
        TrafficSummary<Host> first = hostTrafficSummary.get(0);
        assertThat(first.getEntity(), equalTo(new Host("10.1.1.11")));
        assertThat(first.getBytesIn(), equalTo(10L));
        assertThat(first.getBytesOut(), equalTo(100L));

        TrafficSummary<Host> second = hostTrafficSummary.get(1);
        assertThat(second.getEntity(), equalTo(new Host("10.1.1.12", "la.le.lu")));
        assertThat(second.getBytesIn(), equalTo(210L));
        assertThat(second.getBytesOut(), equalTo(2100L));

        // Try with only one host to let Others accumulate the rest
        hostTrafficSummary =
                smartQueryService.getHostSummaries(ImmutableSet.of("10.1.1.11"), true, getFilters()).get();
        assertThat(hostTrafficSummary, hasSize(2));
        first = hostTrafficSummary.get(0);
        assertThat(first.getEntity(), equalTo(new Host("10.1.1.11")));
        assertThat(first.getBytesIn(), equalTo(10L));
        assertThat(first.getBytesOut(), equalTo(100L));

        second = hostTrafficSummary.get(1);
        assertThat(second.getEntity(), equalTo(new Host("Other")));
        assertThat(second.getBytesIn(), equalTo(410L));
        assertThat(second.getBytesOut(), equalTo(2200L));
    }

    @Test
    public void canGetTopNHostSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Top 10
        Table<Directional<Host>, Long, Double> hostTraffic = smartQueryService.getTopNHostSeries(10, 10, false,
                getFilters()).get();
        // 6 hosts in two directions should yield 12 rows
        assertThat(hostTraffic.rowKeySet(), hasSize(12));

        // Top 2 with others
        hostTraffic = smartQueryService.getTopNHostSeries(2, 10, true, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(6));

        // Top 1
        hostTraffic = smartQueryService.getTopNHostSeries(1, 10, false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(2));
        assertThat(hostTraffic.rowKeySet(), containsInAnyOrder(new Directional<>(new Host("10.1.1.12", "la.le.lu"), true),
                new Directional<>(new Host("10.1.1.12", "la.le.lu"), false)));
        verifyHttpsSeries(hostTraffic, new Host("10.1.1.12", "la.le.lu"));
    }

    @Test
    public void canGetHostSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Get just https
        Table<Directional<Host>, Long, Double> hostTraffic =
                smartQueryService.getHostSeries(Collections.singleton("10.1.1.12"), 10,
                        false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(hostTraffic, new Host("10.1.1.12", "la.le.lu"));

        // Get just 10.1.1.12 and include others
        hostTraffic = smartQueryService.getHostSeries(Collections.singleton("10.1.1.12"), 10,
                true, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(4));

        // Get 10.1.1.12 and 192.168.1.100
        hostTraffic = smartQueryService.getHostSeries(ImmutableSet.of("10.1.1.12", "192.168.1.100"), 10,
                false, getFilters()).get();
        assertThat(hostTraffic.rowKeySet(), hasSize(4));

        // Get 10.1.1.12 and 192.168.1.100 and include others
        hostTraffic = smartQueryService.getHostSeries(ImmutableSet.of("10.1.1.12", "192.168.1.100"), 10,
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
                smartQueryService.getConversations(".*", ".*", ".*", ".*", ".*", 10, getFilters()).get();
        assertThat(conversations, hasSize(4));

        // Find all the conversations with a null application
        conversations =
                smartQueryService.getConversations(".*", ".*", ".*", ".*", "null", 10, getFilters()).get();
        assertThat(conversations, hasSize(1));

        // Find all the conversations involving 10.1.1.12 as the lower IP
        conversations =
                smartQueryService.getConversations(".*", ".*", "10.1.1.12", ".*", ".*", 10, getFilters()).get();
        assertThat(conversations, hasSize(2));

        // Find a specific conversation
        conversations =
                smartQueryService.getConversations("test", "6", "10.1.1.11", "192.168.1.100", "http", 10, getFilters()).get();
        assertThat(conversations, hasSize(1));
        assertThat(conversations.iterator().next(), equalTo("[\"test\",6,\"10.1.1.11\",\"192.168.1.100\",\"http\"]"));

    }

    @Test
    public void canGetTopNConversationSummaries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Retrieve the Top N conversation over the entire time range
        List<TrafficSummary<Conversation>> convoTrafficSummary = smartQueryService.getTopNConversationSummaries(2, false, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(2));

        // Expect the conversations, with the sum of all the bytes from all the flows
        TrafficSummary<Conversation> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.101"));
        assertThat(convo.getEntity().getLowerHostname(), equalTo(Optional.of("la.le.lu")));
        assertThat(convo.getEntity().getUpperHostname(), equalTo(Optional.of("ingress.only")));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(110L));
        assertThat(convo.getBytesOut(), equalTo(1100L));

        convo = convoTrafficSummary.get(1);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.12"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getLowerHostname(), equalTo(Optional.of("la.le.lu")));
        assertThat(convo.getEntity().getUpperHostname(), equalTo(Optional.empty()));
        assertThat(convo.getEntity().getApplication(), equalTo("https"));
        assertThat(convo.getBytesIn(), equalTo(100L));
        assertThat(convo.getBytesOut(), equalTo(1000L));

        // Get the top 1 plus others
        convoTrafficSummary = smartQueryService.getTopNConversationSummaries(1, true, getFilters()).get();
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
                smartQueryService.getConversationSummaries(ImmutableSet.of("[\"test\",6,\"10.1.1.11\",\"192.168.1.100\",\"http\"]"),
                        false, getFilters()).get();
        assertThat(convoTrafficSummary, hasSize(1));
        TrafficSummary<Conversation> convo = convoTrafficSummary.get(0);
        assertThat(convo.getEntity().getLowerIp(), equalTo("10.1.1.11"));
        assertThat(convo.getEntity().getUpperIp(), equalTo("192.168.1.100"));
        assertThat(convo.getEntity().getApplication(), equalTo("http"));
        assertThat(convo.getBytesIn(), equalTo(10L));
        assertThat(convo.getBytesOut(), equalTo(100L));

        // Get a specific conversation plus others
        convoTrafficSummary = smartQueryService.getConversationSummaries(
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
        Table<Directional<Conversation>, Long, Double> convoTraffic = smartQueryService.getConversationSeries(ImmutableSet.of("[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]"), 10, false, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(2));
        verifyHttpsSeries(convoTraffic, Conversation.builder()
                .withLocation("test")
                .withProtocol(6)
                .withLowerIp("10.1.1.12")
                .withLowerHostname("la.le.lu")
                .withUpperIp("192.168.1.100")
                .withApplication("https").build());

        // Get series for same host and include others
        convoTraffic = smartQueryService.getConversationSeries(ImmutableSet.of("[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]"), 10, true, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(4));
    }

    @Test
    public void canRetrieveTopNConversationsSeries() throws Exception {
        // Load the default set of flows
        loadDefaultFlows();

        // Top 10
        Table<Directional<Conversation>, Long, Double> convoTraffic = smartQueryService.getTopNConversationSeries(10, 10, false, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(8));

        // Top 2 with others
        convoTraffic = smartQueryService.getTopNConversationSeries(2, 10, true, getFilters()).get();
        assertThat(convoTraffic.rowKeySet(), hasSize(6));
    }

    @Test
    public void hasCorrectOrdering() throws Exception {
        this.loadFlows(new FlowBuilder()
                .withExporter("SomeFs", "SomeFid", 99)
                .withSnmpInterfaceId(98)
                .withDirection(Direction.INGRESS)

                // More documents - less data
                .withFlow(new Date(0), new Date(10), "192.168.0.1", 1234, "192.168.1.1", 1234, 100)
                .withFlow(new Date(0), new Date(10), "192.168.0.1", 1234, "192.168.1.1", 1234, 100)
                .withFlow(new Date(0), new Date(10), "192.168.0.1", 1234, "192.168.1.1", 1234, 100)
                .withFlow(new Date(0), new Date(10), "192.168.0.1", 1234, "192.168.1.1", 1234, 100)
                .withFlow(new Date(0), new Date(10), "192.168.0.1", 1234, "192.168.1.1", 1234, 100)


                .withFlow(new Date(0), new Date(10), "192.168.0.2", 1234, "192.168.1.2", 1234, 1000)
                .withFlow(new Date(0), new Date(10), "192.168.0.2", 1234, "192.168.1.2", 1234, 1000)
                .withFlow(new Date(0), new Date(10), "192.168.0.2", 1234, "192.168.1.2", 1234, 1000)

                // Less documents - more data
                .withFlow(new Date(0), new Date(10), "192.168.0.3", 1234, "192.168.1.3", 1234, 10000)

                .build());

        final List<TrafficSummary<Host>> summary = smartQueryService.getTopNHostSummaries(10, false, getFilters()).get();
        assertThat(summary, contains(
                TrafficSummary.from(Host.from("192.168.0.3").build()).withBytes(10000, 0).build(),
                TrafficSummary.from(Host.from("192.168.1.3").build()).withBytes(10000, 0).build(),
                TrafficSummary.from(Host.from("192.168.0.2").build()).withBytes(3000, 0).build(),
                TrafficSummary.from(Host.from("192.168.1.2").build()).withBytes(3000, 0).build(),
                TrafficSummary.from(Host.from("192.168.0.1").build()).withBytes(500, 0).build(),
                TrafficSummary.from(Host.from("192.168.1.1").build()).withBytes(500, 0).build()
        ));

        final Table<Directional<Host>, Long, Double> series = smartQueryService.getTopNHostSeries(10, 10, false, getFilters()).get();
        assertThat(series.rowKeySet(), contains(
                new Directional<>(Host.from("192.168.0.3").build(), true),
                new Directional<>(Host.from("192.168.1.3").build(), true),
                new Directional<>(Host.from("192.168.0.2").build(), true),
                new Directional<>(Host.from("192.168.1.2").build(), true),
                new Directional<>(Host.from("192.168.0.1").build(), true),
                new Directional<>(Host.from("192.168.1.1").build(), true)
        ));
    }

    private Object[] defaultFlowsFieldValues(Function<FlowDocument, Integer> fieldAccess) {
        return DEFAULT_FLOWS
                .stream()
                .map(fieldAccess)
                .distinct()
                .sorted()
                .map(i -> i.toString())
                .toArray()
                ;
    }

    private void canGetFieldValues(LimitedCardinalityField field, Function<FlowDocument, Integer> fieldAccess) throws Exception {
        loadDefaultFlows();

        Object[] memoryResult = defaultFlowsFieldValues(fieldAccess);

        List<String> elasticResult = smartQueryService.getFieldValues(field, getFilters()).get();

        assertThat(elasticResult, containsInAnyOrder(memoryResult));
    }

    private static Predicate<FlowDocument> filterPredicate(Filter filter) {
        return filter.visit(new FilterVisitor<Predicate<FlowDocument>>() {
            @Override
            public Predicate<FlowDocument> visit(ExporterNodeFilter exporterNodeFilter) {
                throw new RuntimeException("not yet implemented");
            }

            @Override
            public Predicate<FlowDocument> visit(TimeRangeFilter timeRangeFilter) {
                return fd -> fd.getLastSwitched() >= timeRangeFilter.getStart() && fd.getDeltaSwitched() <= timeRangeFilter.getEnd();
            }

            @Override
            public Predicate<FlowDocument> visit(SnmpInterfaceIdFilter snmpInterfaceIdFilter) {
                return fd -> snmpInterfaceIdFilter.getSnmpInterfaceId() == (fd.getDirection() == Direction.INGRESS ? fd.getInputSnmp() : fd.getOutputSnmp());
            }

            @Override
            public Predicate<FlowDocument> visit(DscpFilter dscpFilter) {
                return fd -> dscpFilter.getDscp().isEmpty() || dscpFilter.getDscp().contains(fd.getDscp());
            }

        });
    }

    private void canGetFieldSummaries(LimitedCardinalityField field, Function<FlowDocument, Integer> aggregateBy) throws Exception {
        loadDefaultFlows();
        canGetFieldSeriesOfLoadedFlows(field, aggregateBy, null);
    }

    private void canGetFieldSeries(LimitedCardinalityField field, Function<FlowDocument, Integer> aggregateBy) throws Exception {
        loadDefaultFlows();
        canGetFieldSeriesOfLoadedFlows(field, aggregateBy, null);
    }

    private void canGetFieldSummariesOfLoadedFlows(LimitedCardinalityField field, Function<FlowDocument, Integer> aggregateBy, Filter filter) throws Exception {

        List<Filter> filters = filter != null ? getFilters(filter) : getFilters();

        Predicate<FlowDocument> predicate = filters
                .stream()
                .map(FlowQueryIT::filterPredicate)
                .reduce(fd -> true, (p1, p2) -> fd -> p1.test(fd) && p2.test(fd));

        Object[] memoryResult = DEFAULT_FLOWS
                .stream()
                .filter(predicate)
                .map(fd -> FlowQueryIT.flowDoc2TrafficSummary(fd, aggregateBy.apply(fd).toString()))
                // collect the traffic summaries into a map
                // -> the map key is the traffic summary key and the map value is the merged traffic summary for that key
                .collect(Collectors.groupingBy(
                        TrafficSummary::getEntity,
                        Collectors.reducing(FlowQueryIT::mergeTrafficSummaries)
                ))
                .values()
                .stream()
                .map(o -> o.get())
                .sorted(Comparator.comparing(s -> new Integer(s.getEntity())))
                .toArray();

        List<TrafficSummary<String>> elasticResult = smartQueryService.getFieldSummaries(field, filters).get();

        assertThat(elasticResult, contains(memoryResult));
    }

    private void canGetFieldSeriesOfLoadedFlows(LimitedCardinalityField field, Function<FlowDocument, Integer> aggregateBy, Filter filter) throws Exception {

        int step = 8;

        List<Filter> filters = filter != null ? getFilters(filter) : getFilters();

        Predicate<FlowDocument> predicate = filters
                .stream()
                .map(FlowQueryIT::filterPredicate)
                .reduce(fd -> true, (p1, p2) -> fd -> p1.test(fd) && p2.test(fd));

        Map<Directional<String>, Map<Long, Double>> memoryResult = DEFAULT_FLOWS
                .stream()
                .filter(predicate)
                .map(fd -> flowDoc2Pair(fd, step, aggregateBy.apply(fd).toString()))
                // collect the pairs of directionals and maps (of indexes into bytes) into a map
                // -> the key is the directional and the value are the merged maps for that key
                .collect(Collectors.groupingBy(
                        Pair::getLeft,
                        Collectors.reducing(
                                Collections.<Long, Double>emptyMap(),
                                Pair::getRight,
                                FlowQueryIT::mergeSeries
                        )
                        )
                );

        Table<Directional<String>, Long, Double> elasticResult = smartQueryService.getFieldSeries(field, step, filters).get();

        // The result calculated in memory the result returned by elastic
        // can not be asserted for equality because of rounding errors
        // -> construct a specific hamcrest matcher that allows for some discrepancy when comparing the
        //    numbers of transferred bytes (that are represented by doubles)
        // -> this assertion checks that there is a matching entry in elastic's result for each entry of the memory result
        assertThat(
                elasticResult.rowMap(),
                allOf(memoryResult
                        .entrySet()
                        .stream()
                        .map(dme -> hasEntry(
                                equalTo(dme.getKey()),
                                allOf(dme.getValue()
                                        .entrySet()
                                        .stream()
                                        .map(ime -> hasEntry(
                                                equalTo(ime.getKey()),
                                                closeTo(ime.getValue(), 0.1)
                                        ))
                                        .collect(Collectors.toList())
                                )
                        ))
                        .collect(Collectors.toList())
                )
        );

        // check the other way round:
        // -> check that there is a matching entry in the memory result for each entry in elastic's result
        assertThat(memoryResult, allOf(elasticResult.rowKeySet().stream().map(k -> hasKey(k)).collect(Collectors.toList())));
    }

    @Test
    public void canGetDscpValues() throws Exception {
        canGetFieldValues(LimitedCardinalityField.DSCP, fd -> fd.getDscp());
    }

    @Test
    public void canGetDscpSummaries() throws Exception {
        canGetFieldSummaries(LimitedCardinalityField.DSCP, fd -> fd.getDscp());
    }

    @Test
    public void canGetDscpSeries() throws Exception {
        canGetFieldSeries(LimitedCardinalityField.DSCP, fd -> fd.getDscp());
    }

    private List<Filter> allFilterCombinations(
            LimitedCardinalityField field,
            Function<FlowDocument, Integer> fieldAccess,
            Function<List<Integer>, Filter> filterCreator
    ) {
        List<Filter> res = new ArrayList<>();
        Object[] values = defaultFlowsFieldValues(fieldAccess);

        // check all combinations of filter values
        // -> for each value decide if it is used in the filter or not
        // -> combinations correspond to binary numbers with up values.length digits
        // -> if a bit is set then the corresponding value is included

        int combinations = 1 << values.length;

        // start with 1 -> at least one bit is set
        for (int i = 1; i < combinations; i++) {
            List<Integer> filterValues = new ArrayList<>();
            for (int j = 0; j < values.length; j++) {
                if ((i >> j & 1) == 1) {
                    filterValues.add(Integer.parseInt((String) values[j]));
                }
            }
            res.add(filterCreator.apply(filterValues));
        }

        return res;
    }

    private void canFilterSeriesByLimitedCardinalityField(
            LimitedCardinalityField field,
            Function<FlowDocument, Integer> fieldAccess,
            Function<List<Integer>, Filter> filterCreator
    ) throws Exception {
        loadDefaultFlows();
        for (Filter filter: allFilterCombinations(field, fieldAccess, filterCreator)) {
            canGetFieldSeriesOfLoadedFlows(field, fieldAccess, filter);
        }
    }

    private void canFilterSummariesByLimitedCardinalityField(
            LimitedCardinalityField field,
            Function<FlowDocument, Integer> fieldAccess,
            Function<List<Integer>, Filter> filterCreator
    ) throws Exception {
        loadDefaultFlows();
        for (Filter filter: allFilterCombinations(field, fieldAccess, filterCreator)) {
            canGetFieldSummariesOfLoadedFlows(field, fieldAccess, filter);
        }
    }

    @Test
    public void canFilterSeriesByDscp() throws Exception {
        canFilterSeriesByLimitedCardinalityField(LimitedCardinalityField.DSCP, FlowDocument::getDscp, DscpFilter::new);
    }


    @Test
    public void canFilterSummariesByDscp() throws Exception {
        canFilterSummariesByLimitedCardinalityField(LimitedCardinalityField.DSCP, FlowDocument::getDscp, DscpFilter::new);
    }

    private static <K> TrafficSummary<K> flowDoc2TrafficSummary(FlowDocument fd, K key) {
        return TrafficSummary
                .from(key)
                .withBytes(
                        fd.getDirection() == Direction.INGRESS ? fd.getBytes() : 0,
                        fd.getDirection() == Direction.EGRESS ? fd.getBytes() : 0)
                .withCongestionEncountered(fd.getTos() != null && fd.getTos() % 4 == 3)
                .withNonEcnCapableTransport(fd.getTos() != null && fd.getTos() % 4 == 0)
                .build();
    }

    private static <K> Pair<Directional<K>, Map<Long, Double>> flowDoc2Pair(FlowDocument fd, long step, K key) {
        long firstSwitched = fd.getFirstSwitched();
        long lastSwitched = fd.getLastSwitched();
        long duration = lastSwitched - firstSwitched;
        double bytes = fd.getBytes();
        long firstIndex = firstSwitched / step;
        long lastIndex = (lastSwitched - 1) / step;
        HashMap<Long, Double> res = new HashMap<>();
        for (long idx = firstIndex; idx <= lastIndex; idx++) {
            long from = Math.max(idx * step, firstSwitched);
            long to = Math.min((idx + 1) * step, lastSwitched);
            double value = bytes * (to - from) / duration;
            res.put(idx * step, value);
        }
        return Pair.of(new Directional(key, fd.getDirection() == Direction.INGRESS), res);
    }

    private static <K> TrafficSummary<K> mergeTrafficSummaries(TrafficSummary<K> t1, TrafficSummary<K> t2) {
        return TrafficSummary.from(t1.getEntity())
                .withBytes(t1.getBytesIn() + t2.getBytesIn(), t1.getBytesOut() + t2.getBytesOut())
                .withCongestionEncountered(t1.isCongestionEncountered() || t2.isCongestionEncountered())
                .withNonEcnCapableTransport(t1.isNonEcnCapableTransport() || t2.isNonEcnCapableTransport())
                .build();

    }

    private static Map<Long, Double> mergeSeries(Map<Long, Double> m1, Map<Long, Double> m2) {
        HashMap<Long, Double> res = new HashMap<>();
        res.putAll(m1);
        m2.forEach((l, d) -> {
            Double o = res.get(l);
            res.put(l, o != null ? o + d : d);
        });
        return res;
    }

    private <L> void verifyHttpsSeries(Table<Directional<L>, Long, Double> appTraffic, L label) {
        // Pull the values from the table into arrays for easy comparison and validate
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
        // Pull the values from the table into arrays for easy comparison and validate
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

    private static List<FlowDocument> DEFAULT_FLOWS = new FlowBuilder()
            .withExporter("SomeFs", "SomeFid", 99)
            .withSnmpInterfaceId(98)
            // 192.168.1.100:43444 <-> 10.1.1.11:80 (110 bytes in [3,15])
            .withDirection(Direction.INGRESS)
            .withTos(4 + 64)
            .withFlow(new Date(3), new Date(15), "192.168.1.100", 43444, "10.1.1.11", 80, 10)
            .withDirection(Direction.EGRESS)
            .withTos(8 + 128)
            .withFlow(new Date(3), new Date(15), "10.1.1.11", 80, "192.168.1.100", 43444, 100)
            // 192.168.1.100:43445 <-> 10.1.1.12:443 (1100 bytes in [13,26])
            .withDirection(Direction.INGRESS)
            .withHostnames(null, "la.le.lu")
            .withTos(16 + 64)
            .withFlow(new Date(13), new Date(26), "192.168.1.100", 43445, "10.1.1.12", 443, 100)
            .withDirection(Direction.EGRESS)
            .withHostnames("la.le.lu", null)
            .withTos(32 + 128)
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


    private void loadDefaultFlows() throws FlowException {
        this.loadFlows(DEFAULT_FLOWS);
    }

    private void loadFlows(final List<FlowDocument> flowDocuments) throws FlowException {
        final List<Flow> flows = flowDocuments.stream().map(TestFlow::new).collect(Collectors.toList());
        flowRepository.persist(flows, new FlowSource("test", "127.0.0.1", null));

        // Retrieve all the flows we just persisted
        await().atMost(60, TimeUnit.SECONDS).until(() -> smartQueryService.getFlowCount(Collections.singletonList(
                new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo(Long.valueOf(flows.size())));
    }

    private List<Filter> getFilters(Filter... filters) {
        final List<Filter> filterList = Lists.newArrayList(filters);
        filterList.add(new TimeRangeFilter(0, System.currentTimeMillis()));
        // Match the SNMP interface id in the flows
        filterList.add(new SnmpInterfaceIdFilter(98));
        return filterList;
    }
}
