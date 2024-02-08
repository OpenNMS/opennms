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
        final GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(graph, serviceAccessor);
        final GraphMLEdgeStatusProvider statusProvider = new GraphMLEdgeStatusProvider(
                topologyProvider,
                new ScriptEngineManager(),
                serviceAccessor,
                Paths.get("src","test", "opennms-home", "etc", "graphml-edge-status"));

        assertThat(statusProvider.contributesTo("acme:regions"), is(true));
        assertThat(statusProvider.getNamespace(), is("acme:regions"));

        // Calculating the status executes some tests defined int the according scripts as a side effect
        final EdgeRef edgeRef = topologyProvider.getCurrentGraph().getEdge("acme:regions", "center_north");
        final Map<? extends EdgeRef, ? extends Status> status = statusProvider.getStatusForEdges(topologyProvider.getCurrentGraph(), ImmutableList.of(edgeRef), new Criteria[0]);

        // Checking nodeID creation for vertices with only foreignSource/foreignID set
        final VertexRef vertexRef = topologyProvider.getCurrentGraph().getVertex("acme:regions", "west");
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
