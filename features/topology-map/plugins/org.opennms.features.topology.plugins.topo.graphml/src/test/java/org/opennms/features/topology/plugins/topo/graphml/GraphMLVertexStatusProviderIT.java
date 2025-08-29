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
package org.opennms.features.topology.plugins.topo.graphml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.internal.AlarmSummaryWrapper;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLDefaultVertexStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLPropagateVertexStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLScriptVertexStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLVertexStatus;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class GraphMLVertexStatusProviderIT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private TransactionOperations transactionOperations;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    private GraphMLServiceAccessor serviceAccessor;
    private AlarmSummaryWrapper alarmSummaryWrapper;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();

        this.serviceAccessor = new GraphMLServiceAccessor();
        this.serviceAccessor.setTransactionOperations(this.transactionOperations);
        this.serviceAccessor.setNodeDao(this.nodeDao);
        this.serviceAccessor.setSnmpInterfaceDao(this.snmpInterfaceDao);
        this.serviceAccessor.setMeasurementsService(request -> new QueryResponse());

        this.alarmSummaryWrapper = nodeIds -> Lists.newArrayList(
                createSummary(1, "North", OnmsSeverity.WARNING, 1),
                createSummary(2, "West", OnmsSeverity.MINOR, 2),
                createSummary(3, "South", OnmsSeverity.MAJOR, 3),
                createSummary(4, "West 2", OnmsSeverity.MAJOR, 4),
                createSummary(5, "East 1", OnmsSeverity.CRITICAL, 5),
                createSummary(6, "East 2", OnmsSeverity.WARNING, 6)
        );
    }

    @Test
    public void testDefaultStatusProvider() throws InvalidGraphException {
        GraphML graphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph.xml"));
        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(graphML.getGraphs().get(0), new GraphMLServiceAccessor());
        GraphMLDefaultVertexStatusProvider statusProvider = new GraphMLDefaultVertexStatusProvider(topologyProvider.getNamespace(), this.alarmSummaryWrapper);
        BackendGraph graph = topologyProvider.getCurrentGraph();

        List<VertexRef> vertices = graph.getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(graph, vertices, new Criteria[0]);
        Assert.assertEquals(4, statusForVertices.size());
        Assert.assertEquals(ImmutableMap.of(
                createVertexRef(topologyProvider.getNamespace(), "north"), createStatus(OnmsSeverity.WARNING, 1),
                createVertexRef(topologyProvider.getNamespace(), "west"), createStatus(OnmsSeverity.MINOR, 2),
                createVertexRef(topologyProvider.getNamespace(), "south"), createStatus(OnmsSeverity.MAJOR, 3),
                createVertexRef(topologyProvider.getNamespace(), "east"), createStatus(OnmsSeverity.NORMAL, 0)), statusForVertices);
    }

    @Test
    public void testPropagateStatusProvider() throws Exception {
        final File graphXml = this.tempFolder.newFile();
        Resources.asByteSource(Resources.getResource("test-graph.xml")).copyTo(Files.asByteSink(graphXml));

        final GraphMLMetaTopologyProvider metaTopoProvider = new GraphMLMetaTopologyProvider(new GraphMLServiceAccessor());
        metaTopoProvider.setTopologyLocation(graphXml.getAbsolutePath());
        metaTopoProvider.reload();

        final GraphMLTopologyProvider childTopologyProvider = metaTopoProvider.getGraphProvider("acme:markets");
        final GraphMLDefaultVertexStatusProvider childStatusProvider = new GraphMLDefaultVertexStatusProvider(childTopologyProvider.getNamespace(), this.alarmSummaryWrapper);

        final ServiceReference<StatusProvider> statusProviderReference = mock(ServiceReference.class);

        final BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getServiceReferences(StatusProvider.class, null)).thenReturn(ImmutableList.of(statusProviderReference));
        when(bundleContext.getService(statusProviderReference)).thenReturn(childStatusProvider);

        final GraphMLTopologyProvider topologyProvider = metaTopoProvider.getGraphProvider("acme:regions");
        final GraphMLPropagateVertexStatusProvider statusProvider = new GraphMLPropagateVertexStatusProvider(topologyProvider.getNamespace(),
                                                                                                             metaTopoProvider,
                                                                                                             bundleContext);

        List<VertexRef> vertices = topologyProvider.getCurrentGraph().getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider.getCurrentGraph(), vertices, new Criteria[0]);
        Assert.assertEquals(4, statusForVertices.size());
        Assert.assertEquals(ImmutableMap.of(
                createVertexRef(topologyProvider.getNamespace(), "north"), createStatus(OnmsSeverity.NORMAL, 0),
                createVertexRef(topologyProvider.getNamespace(), "west"), createStatus(OnmsSeverity.MAJOR, 4),
                createVertexRef(topologyProvider.getNamespace(), "south"), createStatus(OnmsSeverity.NORMAL, 0),
                createVertexRef(topologyProvider.getNamespace(), "east"), createStatus(OnmsSeverity.CRITICAL, 11)), statusForVertices);
    }

    @Test
    public void testScriptStatusProvider() throws InvalidGraphException {
        GraphML graphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph.xml"));
        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(graphML.getGraphs().get(0), new GraphMLServiceAccessor());
        GraphMLScriptVertexStatusProvider statusProvider = new GraphMLScriptVertexStatusProvider(topologyProvider.getNamespace(),
                                                                                                 this.alarmSummaryWrapper,
                                                                                                 new ScriptEngineManager(),
                                                                                                 this.serviceAccessor,
                                                                                                 Paths.get("src", "test", "opennms-home", "etc", "graphml-vertex-status"));

        List<VertexRef> vertices = topologyProvider.getCurrentGraph().getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider.getCurrentGraph(), vertices, new Criteria[0]);
        Assert.assertEquals(4, statusForVertices.size());
        Assert.assertEquals(ImmutableMap.of(
                createVertexRef(topologyProvider.getNamespace(), "north"), createStatus(OnmsSeverity.NORMAL, 0),
                createVertexRef(topologyProvider.getNamespace(), "west"), createStatus(OnmsSeverity.NORMAL, 0),
                createVertexRef(topologyProvider.getNamespace(), "south"), createStatus(OnmsSeverity.WARNING, 42),
                createVertexRef(topologyProvider.getNamespace(), "east"), createStatus(OnmsSeverity.CRITICAL, 23)), statusForVertices);
    }

    private static Status createStatus(OnmsSeverity severity, long count) {
        return new GraphMLVertexStatus(severity, count);
    }

    private static DefaultVertexRef createVertexRef(String namespace, String id) {
        return new DefaultVertexRef(namespace, id);
    }

    private static AlarmSummary createSummary(int nodeId, String label, OnmsSeverity maxSeverity, long count) {
        return new AlarmSummary(nodeId, label, new Date(), maxSeverity, count);
    }
}
