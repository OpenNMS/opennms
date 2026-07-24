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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.model.OnmsNode;

public class PathOutageGraphProviderTest {

    private NodeDao nodeDao;
    private PathOutageGraphProvider provider;

    @Before
    public void setUp() {
        nodeDao = mock(NodeDao.class);
        final SessionUtils sessionUtils = mock(SessionUtils.class);
        // Run the supplier directly; the transaction wrapper is irrelevant to the mock-backed test.
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());
        provider = new PathOutageGraphProvider(nodeDao, sessionUtils);
    }

    private static OnmsNode node(int id, String label, OnmsNode parent) {
        final OnmsNode node = new OnmsNode();
        node.setId(id);
        node.setLabel(label);
        node.setParent(parent);
        // The provider reads the parent off the nodeParentID column, not the proxy.
        if (parent != null) {
            node.setNodeParentId(parent.getId());
        }
        return node;
    }

    @Test
    public void graphInfoUsesThePathoutageNamespace() {
        assertEquals(PathOutageGraphProvider.NAMESPACE, provider.getGraphInfo().getNamespace());
        assertNotNull(provider.getGraphInfo().getLabel());
    }

    @Test
    public void buildsParentChildForestAndSkipsIsolatedNodes() {
        final OnmsNode router = node(1, "router", null);
        final OnmsNode sw = node(2, "switch", router);
        final OnmsNode server = node(3, "server", sw);
        final OnmsNode standalone = node(4, "standalone", null);
        // The provider queries only nodes with a parent (isNotNull("parent")); mimic
        // that filter so router (top of the tree, reached via getParent()) and the
        // standalone node are excluded from the result the provider sees.
        when(nodeDao.findMatching(any(Criteria.class))).thenReturn(
                Arrays.asList(router, sw, server, standalone).stream()
                        .filter(n -> n.getParent() != null)
                        .collect(Collectors.toList()));
        when(nodeDao.getAllLabelsById()).thenReturn(
                Map.of(1, "router", 2, "switch", 3, "server", 4, "standalone"));

        final GenericGraph graph = provider.loadGraph().asGenericGraph();

        assertEquals(PathOutageGraphProvider.NAMESPACE, graph.getNamespace());
        // standalone has neither parent nor children -> excluded
        assertEquals(Arrays.asList("1", "2", "3"),
                graph.getVertices().stream().map(GenericVertex::getId).sorted().collect(Collectors.toList()));
        assertEquals(2, graph.getEdges().size());

        // Vertices carry the node linkage + label the REST consumers rely on
        final GenericVertex switchVertex = graph.getVertex("2");
        assertEquals("switch", switchVertex.getProperty(GenericProperties.LABEL));
        assertEquals("2", switchVertex.getProperty(GenericProperties.NODE_ID));

        // Edges are directed parent -> child
        for (final GenericEdge edge : graph.getEdges()) {
            if (edge.getTarget().getId().equals("2")) {
                assertEquals("1", edge.getSource().getId());
            } else {
                assertEquals("2", edge.getSource().getId());
                assertEquals("3", edge.getTarget().getId());
            }
        }
    }

    @Test
    public void skipsDanglingParentReference() {
        // NMS-19971: the child still carries a nodeParentID, but that parent row was
        // deleted, so it is absent from the id->label map. The relationship (and thus
        // the child, which has no other link) is dropped instead of blowing up.
        final OnmsNode orphan = node(5, "orphan", null);
        orphan.setNodeParentId(999);
        when(nodeDao.findMatching(any(Criteria.class))).thenReturn(List.of(orphan));
        when(nodeDao.getAllLabelsById()).thenReturn(Map.of(5, "orphan"));

        final GenericGraph graph = provider.loadGraph().asGenericGraph();

        assertTrue(graph.getVertices().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    public void emptyWhenNoParentsAreConfigured() {
        // No node has a parent, so the parent-filtered query returns nothing.
        when(nodeDao.findMatching(any(Criteria.class))).thenReturn(Collections.emptyList());
        final GenericGraph graph = provider.loadGraph().asGenericGraph();
        assertTrue(graph.getVertices().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    public void emptyWhenThereAreNoNodes() {
        when(nodeDao.findMatching(any(Criteria.class))).thenReturn(Collections.emptyList());
        final GenericGraph graph = provider.loadGraph().asGenericGraph();
        assertTrue(graph.getVertices().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }
}
