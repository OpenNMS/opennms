/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

import javax.script.ScriptEngineManager;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(null, graphML.getGraphs().get(0), new GraphMLServiceAccessor());
        GraphMLDefaultVertexStatusProvider statusProvider = new GraphMLDefaultVertexStatusProvider(topologyProvider.getNamespace(),
                                                                                                   this.alarmSummaryWrapper);

        List<VertexRef> vertices = topologyProvider.getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider, vertices, new Criteria[0]);
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

        final GraphMLTopologyProvider childTopologyProvider = metaTopoProvider.getRawTopologyProvider("acme:markets");
        final GraphMLDefaultVertexStatusProvider childStatusProvider = new GraphMLDefaultVertexStatusProvider(childTopologyProvider.getNamespace(),
                                                                                                         this.alarmSummaryWrapper);

        final ServiceReference<StatusProvider> statusProviderReference = EasyMock.niceMock(ServiceReference.class);

        final BundleContext bundleContext = EasyMock.niceMock(BundleContext.class);
        EasyMock.expect(bundleContext.getServiceReferences(StatusProvider.class, null)).andReturn(ImmutableList.of(statusProviderReference)).anyTimes();
        EasyMock.expect(bundleContext.getService(statusProviderReference)).andReturn(childStatusProvider);
        EasyMock.replay(statusProviderReference, bundleContext);

        final GraphMLTopologyProvider topologyProvider = metaTopoProvider.getRawTopologyProvider("acme:regions");
        final GraphMLPropagateVertexStatusProvider statusProvider = new GraphMLPropagateVertexStatusProvider(topologyProvider.getNamespace(),
                                                                                                             metaTopoProvider,
                                                                                                             bundleContext);

        List<VertexRef> vertices = topologyProvider.getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider, vertices, new Criteria[0]);
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
        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(null, graphML.getGraphs().get(0), new GraphMLServiceAccessor());
        GraphMLScriptVertexStatusProvider statusProvider = new GraphMLScriptVertexStatusProvider(topologyProvider.getNamespace(),
                                                                                                 this.alarmSummaryWrapper,
                                                                                                 new ScriptEngineManager(),
                                                                                                 this.serviceAccessor,
                                                                                                 Paths.get("src", "test", "opennms-home", "etc", "graphml-vertex-status"));

        List<VertexRef> vertices = topologyProvider.getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getNamespace(), statusProvider.getNamespace());
        Assert.assertTrue(statusProvider.contributesTo(topologyProvider.getNamespace()));

        Map<? extends VertexRef, ? extends Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider, vertices, new Criteria[0]);
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
