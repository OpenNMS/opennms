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
package org.opennms.netmgt.graph.provider.pathoutage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Exercises {@link PathOutageGraphProvider} against the real Hibernate
 * {@link NodeDao} on a temporary database. The mockito unit test can't cover
 * the one risky part: {@code OnmsNode.getParent()} is a LAZY many-to-one, so
 * the provider must traverse it inside its own read-only transaction. This
 * test therefore deliberately runs WITHOUT a test-managed transaction --
 * {@code loadGraph()} is called the way the Graph REST service calls it, and
 * would die with a LazyInitializationException if the provider's transaction
 * wrapping were missing.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class PathOutageGraphProviderIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MonitoringLocationDao locationDao;

    @Autowired
    private SessionUtils sessionUtils;

    private PathOutageGraphProvider provider;

    private int routerId;
    private int switchId;
    private int serverId;
    private int standaloneId;

    private OnmsNode node(final String label, final OnmsNode parent) {
        final OnmsNode node = new OnmsNode(locationDao.getDefaultLocation(), label);
        node.setParent(parent);
        nodeDao.save(node);
        return node;
    }

    @Before
    public void setUp() {
        // router -> switch -> server, plus a standalone node outside the hierarchy.
        sessionUtils.withTransaction(() -> {
            final OnmsNode router = node("router", null);
            final OnmsNode sw = node("switch", router);
            final OnmsNode server = node("server", sw);
            final OnmsNode standalone = node("standalone", null);
            routerId = router.getId();
            switchId = sw.getId();
            serverId = server.getId();
            standaloneId = standalone.getId();
        });
        provider = new PathOutageGraphProvider(nodeDao, sessionUtils);
    }

    @Test
    public void loadsTheParentHierarchyWithoutACallerTransaction() {
        // No surrounding transaction here, on purpose -- see the class javadoc.
        final GenericGraph graph = provider.loadGraph().asGenericGraph();

        assertEquals(PathOutageGraphProvider.NAMESPACE, graph.getNamespace());

        final List<String> vertexIds = graph.getVertices().stream()
                .map(GenericVertex::getId)
                .collect(Collectors.toList());
        assertTrue(vertexIds.containsAll(List.of(
                String.valueOf(routerId), String.valueOf(switchId), String.valueOf(serverId))));
        // The standalone node takes part in no parent-child relation -> excluded.
        assertEquals(3, graph.getVertices().size());
        assertTrue(!vertexIds.contains(String.valueOf(standaloneId)));

        // The parent's label was read through the lazy proxy.
        final GenericVertex switchVertex = graph.getVertex(String.valueOf(switchId));
        assertEquals("switch", switchVertex.getProperty(GenericProperties.LABEL));
        assertEquals(String.valueOf(switchId), switchVertex.getProperty(GenericProperties.NODE_ID));

        // Directed parent -> child: router -> switch and switch -> server.
        assertEquals(2, graph.getEdges().size());
        for (final GenericEdge edge : graph.getEdges()) {
            if (edge.getTarget().getId().equals(String.valueOf(switchId))) {
                assertEquals(String.valueOf(routerId), edge.getSource().getId());
            } else {
                assertEquals(String.valueOf(switchId), edge.getSource().getId());
                assertEquals(String.valueOf(serverId), edge.getTarget().getId());
            }
        }
    }
}
