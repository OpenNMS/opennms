/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.Constants;

public class SimpleGraphProviderTest {

    private VertexRef addVertexToTopology() {
        return m_topologyProvider.addVertex(0, 0);
    }

    private SimpleGraphProvider m_topologyProvider;

    @Before
    public void setUp() {
        if(m_topologyProvider == null) {
            m_topologyProvider = new SimpleGraphProvider();
        }
        m_topologyProvider.resetContainer();
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(0, m_topologyProvider.getVertices().size());

        Vertex vertexA = m_topologyProvider.addVertex(50, 100);
        Assert.assertEquals(1, m_topologyProvider.getVertices().size());
        assertTrue(m_topologyProvider.containsVertexId(vertexA));
        assertTrue(m_topologyProvider.containsVertexId("v0"));
        assertFalse(m_topologyProvider.containsVertexId("v1"));
        ((AbstractVertex)vertexA).setIpAddress("10.0.0.4");
        final String namespace = m_topologyProvider.getNamespace();
        VertexRef ref0 = new DefaultVertexRef(namespace, "v0", namespace + ":v0");
        VertexRef ref1 = new DefaultVertexRef(namespace, "v1", namespace + ":v0");
        Assert.assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        Assert.assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());

        Vertex vertexB = m_topologyProvider.addVertex(100, 50);
        assertTrue(m_topologyProvider.containsVertexId(vertexB));
        assertTrue(m_topologyProvider.containsVertexId("v1"));
        Assert.assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());

        Vertex vertexC = m_topologyProvider.addVertex(100, 150);
        Vertex vertexD = m_topologyProvider.addVertex(150, 100);
        Vertex vertexE = m_topologyProvider.addVertex(200, 200);
        Assert.assertEquals(5, m_topologyProvider.getVertices().size());

        Vertex group1 = m_topologyProvider.addGroup("Group 1", Constants.GROUP_ICON_KEY);
        Vertex group2 = m_topologyProvider.addGroup("Group 2", Constants.GROUP_ICON_KEY);
        Assert.assertEquals(7, m_topologyProvider.getVertices().size());

        m_topologyProvider.setParent(vertexA, group1);
        m_topologyProvider.setParent(vertexB, group1);
        m_topologyProvider.setParent(vertexC, group2);
        m_topologyProvider.setParent(vertexD, group2);

        m_topologyProvider.connectVertices(vertexA, vertexB);
        m_topologyProvider.connectVertices(vertexA, vertexC);
        m_topologyProvider.connectVertices(vertexB, vertexC);
        m_topologyProvider.connectVertices(vertexB, vertexD);
        m_topologyProvider.connectVertices(vertexC, vertexD);
        m_topologyProvider.connectVertices(vertexA, vertexE);
        m_topologyProvider.connectVertices(vertexD, vertexE);

        Assert.assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        Assert.assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());
        Assert.assertEquals(7, m_topologyProvider.getVertices().size());
        Assert.assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref0)).length);
        Assert.assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref1)).length);

        m_topologyProvider.resetContainer();

        // Ensure that the topology provider has been erased
        Assert.assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        Assert.assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());
        Assert.assertEquals(0, m_topologyProvider.getVertices().size());
        Assert.assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref0)).length);
        Assert.assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref1)).length);
    }

    @Test
    public void testConnectVertices() {
        m_topologyProvider.resetContainer();

        Vertex vertexId = m_topologyProvider.addVertex(0, 0);

        Assert.assertEquals(1, m_topologyProvider.getVertices().size());
        Vertex vertex0 = m_topologyProvider.getVertices().iterator().next();
        assertEquals("v0", vertex0.getId());

        Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
        Assert.assertEquals(2, m_topologyProvider.getVertices().size());

        Edge edgeId = m_topologyProvider.connectVertices(vertex0, vertex1);
        Assert.assertEquals(1, m_topologyProvider.getEdges().size());
        SimpleLeafVertex sourceLeafVert = (SimpleLeafVertex) edgeId.getSource().getVertex();
        SimpleLeafVertex targetLeafVert = (SimpleLeafVertex) edgeId.getTarget().getVertex();

        assertEquals("v0", sourceLeafVert.getId());
        assertEquals("v1", targetLeafVert.getId());

        EdgeRef[] edgeIds = m_topologyProvider.getEdgeIdsForVertex(vertexId);
        assertEquals(1, edgeIds.length);
        assertEquals(edgeId, edgeIds[0]);

    }

    @Test
    public void testTopoProviderSetParent() {
        VertexRef vertexId1 = addVertexToTopology();
        VertexRef vertexId2 = addVertexToTopology();

        final AtomicInteger eventsReceived = new AtomicInteger(0);

        m_topologyProvider.addVertexListener(new VertexListener() {

            @Override
            public void vertexSetChanged(VertexProvider provider,
                    Collection<? extends Vertex> added,
                    Collection<? extends Vertex> update,
                    Collection<String> removedVertexIds) {
                eventsReceived.incrementAndGet();
            }

            @Override
            public void vertexSetChanged(VertexProvider provider) {
                eventsReceived.incrementAndGet();
            }
        });

        Vertex groupId = m_topologyProvider.addGroup("Test Group", "groupIcon.jpg");
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);

        m_topologyProvider.setParent(vertexId1, groupId);
        m_topologyProvider.setParent(vertexId2, groupId);

        assertEquals(2, eventsReceived.get());
    }
}
