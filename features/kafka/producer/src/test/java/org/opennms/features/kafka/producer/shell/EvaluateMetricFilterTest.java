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
package org.opennms.features.kafka.producer.shell;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.kafka.producer.collection.KafkaPersisterFactory;
import org.opennms.features.kafka.producer.collection.ResourceTreeMapperTest;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class EvaluateMetricFilterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private PrintStream originalOut;

    private EvaluateMetricFilter command;
    private Hashtable<String, Object> config;
    private OnmsNode node;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        originalOut = System.out;
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));

        node = ResourceTreeMapperTest.node(5, "Raleigh");
        final OnmsResource top = new OnmsResource("5", "node-5", new ResourceTreeMapperTest.StubResourceType("node"),
                Collections.emptySet(), children(), ResourcePath.get("snmp", "5"));
        top.setEntity(node);
        top.getChildResources().forEach(c -> c.setParent(top));

        command = new EvaluateMetricFilter();
        command.nodeDao = mock(NodeDao.class);
        when(command.nodeDao.get("5")).thenReturn(node);
        command.resourceDao = mock(ResourceDao.class);
        when(command.resourceDao.getResourceForNode(node)).thenReturn(top);
        when(command.nodeDao.getNodeIds()).thenReturn(Collections.singletonList(5));
        when(command.nodeDao.get(5)).thenReturn(node);
        command.sessionUtils = mock(SessionUtils.class);
        when(command.sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<Object>) invocation.getArgument(0)).get());

        config = new Hashtable<>();
        config.put("forward.metrics", "true");
        config.put("metricTopic", "metrics");
        final Configuration configuration = mock(Configuration.class);
        when(configuration.getProperties()).thenReturn(config);
        command.configAdmin = mock(ConfigurationAdmin.class);
        when(command.configAdmin.listConfigurations(anyString())).thenReturn(new Configuration[]{configuration});

        command.bundleContext = mock(BundleContext.class);
        when(command.bundleContext.getServiceReferences(PersisterFactory.class, "(strategy=kafka)")).thenReturn(Collections.emptyList());

        // hrStorageIndex is stored by sibling column (its label, not its index), queueMetrics by index
        command.resourceTypesDao = mock(ResourceTypesDao.class);
        final java.util.Map<String, ResourceType> types = new java.util.HashMap<>();
        types.put("hrStorageIndex", resourceType("hrStorageIndex", "org.opennms.netmgt.collection.support.SiblingColumnStorageStrategy"));
        types.put("queueMetrics", resourceType("queueMetrics", "org.opennms.netmgt.collection.support.IndexStorageStrategy"));
        when(command.resourceTypesDao.getResourceTypes()).thenReturn(types);
    }

    private static ResourceType resourceType(String name, String storageStrategyClass) {
        final ResourceType type = new ResourceType();
        type.setName(name);
        final StorageStrategy strategy = new StorageStrategy();
        strategy.setClazz(storageStrategyClass);
        type.setStorageStrategy(strategy);
        return type;
    }

    @SuppressWarnings("unchecked")
    private void registerPersisterFactories(PersisterFactory... factories) throws Exception {
        final List<ServiceReference<PersisterFactory>> references = new java.util.ArrayList<>();
        for (PersisterFactory factory : factories) {
            final ServiceReference<PersisterFactory> reference = mock(ServiceReference.class);
            when(command.bundleContext.getService(reference)).thenReturn(factory);
            references.add(reference);
        }
        when(command.bundleContext.getServiceReferences(PersisterFactory.class, "(strategy=kafka)")).thenReturn(references);
    }

    private List<OnmsResource> children() {
        final OnmsResource nodeLevel = ResourceTreeMapperTest.resource(null, "nodeSnmp", "",
                new RrdGraphAttribute("loadavg1", "snmp/5", "loadavg1.rrd"));
        final OnmsResource eth0 = ResourceTreeMapperTest.resource(null, "interfaceSnmp", "eth0-aabbcc");
        eth0.setEntity(new OnmsSnmpInterface(node, 3));
        final OnmsResource storage = ResourceTreeMapperTest.resource(null, "hrStorageIndex", "1");
        final OnmsResource icmp = ResourceTreeMapperTest.resource(null, "responseTime", "10.0.0.1");
        // The JMX collector's directory: listed as an interface by the tree, forwarded as node-level data
        final OnmsResource jvm = ResourceTreeMapperTest.resource(null, "interfaceSnmp", "opennms-jvm",
                new RrdGraphAttribute("HeapUsed", "snmp/5/opennms-jvm", "HeapUsed.rrd"),
                new RrdGraphAttribute("HeapMax", "snmp/5/opennms-jvm", "HeapMax.rrd"));
        // Service status, which the producer never forwards
        final OnmsResource status = ResourceTreeMapperTest.resource(null, "status", "10.0.0.1");
        return Arrays.asList(nodeLevel, eth0, storage, icmp, jvm, status);
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    private String run() throws Exception {
        command.execute();
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    public void reportsUnknownNode() throws Exception {
        command.nodeCriteria = "99";
        assertThat(run(), containsString("No node found with criteria: 99"));
    }

    @Test
    public void nodeWithOnlyNonSnmpDataGetsANodeLevelRow() throws Exception {
        // A node whose only persisted data comes from JMX: the tree shows interfaceSnmp[opennms-jvm] and status[...]
        final OnmsNode jmxNode = ResourceTreeMapperTest.node(6, "Default");
        final OnmsResource jvm = ResourceTreeMapperTest.resource(null, "interfaceSnmp", "opennms-jvm",
                new RrdGraphAttribute("HeapUsed", "snmp/6/opennms-jvm", "HeapUsed.rrd"));
        final OnmsResource status = ResourceTreeMapperTest.resource(null, "status", "10.0.0.6");
        final OnmsResource top = new OnmsResource("6", "node-6", new ResourceTreeMapperTest.StubResourceType("node"),
                Collections.emptySet(), Arrays.asList(jvm, status), ResourcePath.get("snmp", "6"));
        top.setEntity(jmxNode);
        top.getChildResources().forEach(c -> c.setParent(top));
        when(command.nodeDao.get("6")).thenReturn(jmxNode);
        when(command.resourceDao.getResourceForNode(jmxNode)).thenReturn(top);

        command.nodeCriteria = "6";
        command.spelExpression = "nodeId == 6";
        command.verbose = true;
        final String output = run();
        assertThat(output, containsString("node[6].nodeSnmp[]"));
        assertThat(output, containsString("│ 1       │ 0       │ FORWARD"));
        assertThat(output, containsString("treeResource     = node[6].interfaceSnmp[opennms-jvm]"));
        assertThat(output, containsString("Resources: 1, forwarded: 1, dropped: 0"));
        assertThat(output, containsString("status[10.0.0.6]: service status is never forwarded"));
        assertThat(output, not(containsString("No resources")));
    }

    @Test
    public void printsSkippedResourcesWhenNothingIsForwardable() throws Exception {
        final OnmsNode statusOnly = ResourceTreeMapperTest.node(7, "Default");
        final OnmsResource status = ResourceTreeMapperTest.resource(null, "status", "10.0.0.7");
        final OnmsResource top = new OnmsResource("7", "node-7", new ResourceTreeMapperTest.StubResourceType("node"),
                Collections.emptySet(), Collections.singletonList(status), ResourcePath.get("snmp", "7"));
        top.setEntity(statusOnly);
        status.setParent(top);
        when(command.nodeDao.get("7")).thenReturn(statusOnly);
        when(command.resourceDao.getResourceForNode(statusOnly)).thenReturn(top);

        command.nodeCriteria = "7";
        final String output = run();
        assertThat(output, containsString("No resources the producer would forward were found"));
        assertThat(output, containsString("Skipped 1 resource(s) the producer never forwards:"));
        assertThat(output, containsString("status[10.0.0.7]: service status is never forwarded"));
    }

    @Test
    public void reportsInvalidNodeCriteria() throws Exception {
        when(command.nodeDao.get("router1")).thenThrow(new NumberFormatException("For input string: \"router1\""));
        command.nodeCriteria = "router1";
        assertThat(run(), containsString("Invalid node criteria 'router1'"));
    }

    @Test
    public void evaluatesExpressionFromCommandLine() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "ifIndex == 3";
        final String output = run();

        assertThat(output, containsString("SPEL Expression: ifIndex == 3 (from command line)"));
        assertThat(output, containsString("Node: node-5 (id=5, foreignSource=fs, foreignId=fid-5, location=Raleigh)"));
        assertThat(output, containsString("node[5].interfaceSnmp[eth0-aabbcc]"));
        assertThat(output, containsString("node[5].hrStorageIndex[1]"));
        assertThat(output, containsString("Resources: 4, forwarded: 1, dropped: 3, errors (forwarded anyway): 0, uncertain: 0"));
        assertThat(output, containsString("Skipped 1 resource(s) the producer never forwards:"));
        assertThat(output, containsString("status[10.0.0.1]: service status is never forwarded"));
        // the table shows the producer's id, not the tree id (the top-level resource is named "5" here, so both agree)
        assertThat(output, containsString("node[5].nodeSnmp[]"));
        // the JMX directory is merged into the node-level row: 1 attribute of its own plus 2 from the JMX directory
        assertThat(output, not(containsString("opennms-jvm")));
        assertThat(output, containsString("│ 3       │ 0       │ DROP"));
        assertThat(output, containsString("not from a live collection"));
    }

    @Test
    public void fallsBackToConfiguredFilter() throws Exception {
        config.put("metricFilter", "resourceTypeName == 'responseTime'");
        command.nodeCriteria = "5";
        final String output = run();

        assertThat(output, containsString("SPEL Expression: resourceTypeName == 'responseTime' (from configuration (org.opennms.features.kafka.producer))"));
        assertThat(output, containsString("Resources: 4, forwarded: 1, dropped: 3"));
    }

    @Test
    public void forwardsEverythingWithoutFilter() throws Exception {
        command.nodeCriteria = "5";
        final String output = run();
        assertThat(output, containsString("No metric filter is set"));
        assertThat(output, containsString("FORWARD (no filter)"));
        assertThat(output, containsString("Resources: 4, forwarded: 4, dropped: 0"));
    }

    @Test
    public void matchesResponseTimeResourcesThroughTheirNode() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "nodeId == 5 and resourceTypeName == 'responseTime'";
        final String output = run();
        assertThat(output, containsString("Resources: 4, forwarded: 1, dropped: 3"));
    }

    @Test
    public void marksInstanceResultsOfLabelNamedTypesAsUncertain() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "instance == '1'";
        command.verbose = true;
        final String output = run();
        // hrStorageIndex[1] matches by its tree name, but the producer would send the raw index, which the tree does not know
        assertThat(output, containsString("1 (label)"));
        assertThat(output, containsString("FORWARD (uncertain)"));
        assertThat(output, containsString("uncertain: 1"));
        assertThat(output, containsString("1 result(s) are uncertain: the expression uses instance"));
        assertThat(output, containsString("(hrStorageIndex)"));
        assertThat(output, containsString("uncertain        = instance is the storage label"));

        // an expression that does not look at instance is never uncertain
        out.reset();
        command.spelExpression = "resourceName == '1'";
        assertThat(run(), containsString("uncertain: 0"));

        // without the ResourceTypesDao nothing can be told apart, so nothing is marked
        out.reset();
        command.resourceTypesDao = null;
        command.spelExpression = "instance == '1'";
        assertThat(run(), containsString("uncertain: 0"));
    }

    @Test
    public void reportsWhenNothingMatches() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "nodeId == 42";
        final String output = run();
        assertThat(output, containsString("Resources: 4, forwarded: 0, dropped: 4"));
        assertThat(output, containsString("No resource matches the filter"));
    }

    @Test
    public void reportsEvaluationErrorsAsForwarded() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "resourceName.startsWith('eth')";
        command.verbose = true;
        final String output = run();
        // node level and response time resources have no resource name, the expression fails on them
        assertThat(output, containsString("ERROR (forwarded)"));
        assertThat(output, containsString("Resources: 4, forwarded: 3, dropped: 1, errors (forwarded anyway): 2"));
        assertThat(output, containsString("resourceTypeName = interfaceSnmp"));
        assertThat(output, containsString("ifIndex          = 3"));
    }

    @Test
    public void reportsParseFailure() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "nodeId ==";
        assertThat(run(), containsString("Failed to parse expression 'nodeId =='"));
    }

    @Test
    public void walksTopLevelResourcesAndHonoursLimit() throws Exception {
        command.limit = 2;
        command.spelExpression = "nodeId == 5";
        final String output = run();
        assertThat(output, containsString("Resources: 2, forwarded: 2, dropped: 0"));
        assertThat(output, containsString("Stopped after 2 resources"));
    }

    @Test
    public void printsJsonOfSurvivingResources() throws Exception {
        command.nodeCriteria = "5";
        command.spelExpression = "ifIndex == 3";
        command.json = true;
        final String output = run();
        assertThat(output, containsString("\"instance\":\"eth0-aabbcc\""));
        assertThat(output, containsString("\"ifIndex\":3"));
        // no made up values, the printer omits the unset value field
        assertThat(output, not(containsString("NaN")));
        assertThat(output, not(containsString("\"value\"")));
        assertThat(output, not(containsString("hrStorageIndex\"")));
    }

    @Test
    public void showConfigWarnsWhenRunningFilterDiffers() throws Exception {
        config.put("metricFilter", "nodeId == 2");
        final KafkaPersisterFactory factory = new KafkaPersisterFactory();
        factory.setMetricFilter("nodeId == 1");
        factory.setTopicName("metrics");
        // Another persister factory registered with the same strategy must not hide the Kafka one
        registerPersisterFactories(mock(PersisterFactory.class), factory);
        command.nodeCriteria = "5";
        command.showConfig = true;
        final String output = run();

        assertThat(output, containsString("forward.metrics            = true"));
        assertThat(output, containsString("metricFilter               = nodeId == 2"));
        assertThat(output, containsString("metricFilter               = nodeId == 1"));
        assertThat(output, containsString("WARNING: the running values of metricFilter differ from the configuration"));

        // the other settings the activator reads once are compared as well
        out.reset();
        factory.setMetricFilter("nodeId == 2");
        factory.setTopicName("metrics");
        config.put("metricTopic", "other-metrics");
        config.put("disable.metrics.splitting", "true");
        final String output2 = run();
        assertThat(output2, containsString("WARNING: the running values of metricTopic, disable.metrics.splitting differ from the configuration"));
    }

    @Test
    public void showConfigReportsMissingPersister() throws Exception {
        registerPersisterFactories(mock(PersisterFactory.class));
        command.nodeCriteria = "5";
        command.showConfig = true;
        final String output = run();
        assertThat(output, containsString("the Kafka persister is not registered"));
        assertThat(output, containsString("metricFilter               = (not set)"));
    }
}
