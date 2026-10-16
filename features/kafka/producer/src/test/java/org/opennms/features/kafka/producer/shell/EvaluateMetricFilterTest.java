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
        when(command.resourceDao.findTopLevelResources()).thenReturn(Collections.singletonList(top));
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
        // An interface whose SNMP interface is gone from the database: the producer never forwards it
        final OnmsResource stale = ResourceTreeMapperTest.resource(null, "interfaceSnmp", "eth9-stale");
        return Arrays.asList(nodeLevel, eth0, storage, icmp, stale);
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
        assertThat(output, containsString("Resources: 4, forwarded: 1, dropped: 3, errors (forwarded anyway): 0"));
        assertThat(output, containsString("Skipped 1 resource(s) the producer never forwards:"));
        assertThat(output, containsString("interfaceSnmp[eth9-stale]: no matching SNMP interface"));
        // the table shows the producer's id, not the tree id (the top-level resource is named "5" here, so both agree)
        assertThat(output, containsString("node[5].nodeSnmp[]"));
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
        assertThat(output, not(containsString("hrStorageIndex\"")));
    }

    @Test
    public void showConfigWarnsWhenRunningFilterDiffers() throws Exception {
        config.put("metricFilter", "nodeId == 2");
        final KafkaPersisterFactory factory = new KafkaPersisterFactory();
        factory.setMetricFilter("nodeId == 1");
        // Another persister factory registered with the same strategy must not hide the Kafka one
        registerPersisterFactories(mock(PersisterFactory.class), factory);
        command.nodeCriteria = "5";
        command.showConfig = true;
        final String output = run();

        assertThat(output, containsString("forward.metrics    = true"));
        assertThat(output, containsString("metricFilter       = nodeId == 2"));
        assertThat(output, containsString("Running metric filter: nodeId == 1"));
        assertThat(output, containsString("WARNING: the running filter differs from the configured one"));
    }

    @Test
    public void showConfigReportsMissingPersister() throws Exception {
        registerPersisterFactories(mock(PersisterFactory.class));
        command.nodeCriteria = "5";
        command.showConfig = true;
        final String output = run();
        assertThat(output, containsString("the Kafka persister is not registered"));
        assertThat(output, containsString("metricFilter       = (not set)"));
    }
}
