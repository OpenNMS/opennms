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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.Map;

import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLEdgeStatus;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLEdgeStatusProvider;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.ImmutableList;

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
public class GraphMLEdgeStatusProviderIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private TransactionOperations transactionOperations;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();
    }

    @Test
    public void verify() throws Exception {
        final GraphMLServiceAccessor serviceAccessor = new GraphMLServiceAccessor();
        serviceAccessor.setTransactionOperations(transactionOperations);
        serviceAccessor.setNodeDao(nodeDao);
        serviceAccessor.setSnmpInterfaceDao(snmpInterfaceDao);
        serviceAccessor.setMeasurementsService(request -> new QueryResponse());

        final GraphMLGraph graph = GraphMLReader.read(getClass().getResourceAsStream("/test-graph2.xml")).getGraphs().get(0);
        final GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(null, graph, serviceAccessor);
        final GraphMLEdgeStatusProvider provider = new GraphMLEdgeStatusProvider(
                topologyProvider,
                new ScriptEngineManager(),
                serviceAccessor,
                Paths.get("src","test", "opennms-home", "etc", "graphml-edge-status"));

        assertThat(provider.contributesTo("acme:regions"), is(true));
        assertThat(provider.getNamespace(), is("acme:regions"));

        // Calculating the status executes some tests defined int the according scripts as a side effect
        final EdgeRef edgeRef = topologyProvider.getEdge("acme:regions", "center_north");
        final Map<? extends EdgeRef, ? extends Status> status = provider.getStatusForEdges(topologyProvider, ImmutableList.of(edgeRef), new Criteria[0]);

        // Checking nodeID creation for vertices with only foreignSource/foreignID set
        final VertexRef vertexRef = topologyProvider.getVertex("acme:regions", "west");
        assertThat(vertexRef, is(notNullValue()));
        assertThat(vertexRef, is(instanceOf(GraphMLVertex.class)));
        assertThat(((GraphMLVertex) vertexRef).getNodeID(), is(4));

        // Testing status merging from two scripts
        assertThat(status, is(notNullValue()));
        assertThat(status, is(hasEntry(edgeRef, new GraphMLEdgeStatus().severity(OnmsSeverity.WARNING)
                                                                       .style("stroke", "pink")
                                                                       .style("stroke-width", "3em"))));
    }
}
