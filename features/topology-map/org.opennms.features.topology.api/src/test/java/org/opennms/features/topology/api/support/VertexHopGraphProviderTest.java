/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class VertexHopGraphProviderTest {

	private VertexHopGraphProvider m_provider;

	private static final String TEST_ID = "TEST";

	private static class TestCollapsibleCriteria extends VertexHopCriteria implements CollapsibleCriteria {
		
		public TestCollapsibleCriteria() {
			super("TEST VERTEX");
		}
		
		public TestCollapsibleCriteria(String label) {
			super(label);
		}

		@Override
		public boolean isCollapsed() {
			return true;
		}

		@Override
		public void setCollapsed(boolean collapsed) {
		}

		@Override
		public Set<VertexRef> getVertices() {
			Set<VertexRef> retval = new HashSet<>();
			retval.add(new DefaultVertexRef("nodes", "g0", "g0"));
			retval.add(new DefaultVertexRef("nodes", "g1", "g1"));
			retval.add(new DefaultVertexRef("nodes", "g2", "g2"));
			return retval;
		}

		@Override
		public Vertex getCollapsedRepresentation() {
			return new AbstractVertex("nodes", TEST_ID, "TEST VERTEX");
		}

		@Override
		public String getNamespace() {
			return "nodes";
		}

		@Override
		public int hashCode() {
			return getLabel().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return getLabel().equals(obj);
		}
	}

	@Before
	public void setUp() {

		MockLogAppender.setupLogging();

		GraphProvider baseProvider = new SimpleGraphBuilder("nodes")
			.vertex("g0").vLabel("group0").vIconKey("group").vTooltip("root group").vStyleName("vertex")
			.vertex("g1").parent("g0").vLabel("group1").vIconKey("group").vTooltip("group 1").vStyleName("vertex")
			.vertex("v1").parent("g1").vLabel("vertex1").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v2").parent("g1").vLabel("vertex2").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("g2").parent("g0").vLabel("group2").vIconKey("group").vTooltip("group 2").vStyleName("vertex")
			.vertex("v3").parent("g2").vLabel("vertex3").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v4").parent("g2").vLabel("vertex4").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.edge("e1", "g0", "g1").eLabel("edge1").eStyleName("edge")
			.edge("e2", "g0", "g2").eLabel("edge2").eStyleName("edge")
			.edge("e3", "g1", "v1").eLabel("edge3").eStyleName("edge")
			.edge("e4", "g1", "v2").eLabel("edge4").eStyleName("edge")
			.edge("e5", "g2", "v3").eLabel("edge5").eStyleName("edge")
			.edge("e6", "g2", "v4").eLabel("edge6").eStyleName("edge")
			.get();

		m_provider = new VertexHopGraphProvider(baseProvider);
	}
	
	@Test
	public void testCollapseEdges() {
		CollapsibleCriteria collapseMe = new CollapsibleCriteria() {
			
			@Override
			public void setCollapsed(boolean collapsed) {}
			
			@Override
			public boolean isCollapsed() {
				return true;
			}
			
			@Override
			public Set<VertexRef> getVertices() {
				Set<VertexRef> retval = new HashSet<>();
				retval.add(new DefaultVertexRef("nodes", "g2"));
				return retval;
			}
			
			@Override
			public String getNamespace() {
				return "nodes";
			}
			
			@Override
			public String getLabel() {
				return "Test Criteria";
			}
			
			@Override
			public String getId() {
				return "Test Criteria";
			}
			
			@Override
			public Vertex getCollapsedRepresentation() {
				return new AbstractVertex("category", "c");
			}
		};

		Set<Edge> edges = VertexHopGraphProvider.collapseEdges(new HashSet<Edge>(m_provider.getEdges()), new CollapsibleCriteria[] { collapseMe });
		for (Edge edge : edges) {
			assertEquals("nodes", edge.getNamespace());

			/*
			Here's the original list of edges

			.edge("e1", "g0", "g1").eLabel("edge1").eStyleName("edge")
			.edge("e2", "g0", "g2").eLabel("edge2").eStyleName("edge")
			.edge("e3", "g1", "v1").eLabel("edge3").eStyleName("edge")
			.edge("e4", "g1", "v2").eLabel("edge4").eStyleName("edge")
			.edge("e5", "g2", "v3").eLabel("edge5").eStyleName("edge")
			.edge("e6", "g2", "v4").eLabel("edge6").eStyleName("edge")
			*/
			if (edge.getId().equals("e1")) {
				assertEquals("nodes", edge.getSource().getVertex().getNamespace());
				assertEquals("g0", edge.getSource().getVertex().getId());
				assertEquals("nodes", edge.getTarget().getVertex().getNamespace());
				assertEquals("g1", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedTarget-e2")) {
				assertEquals("nodes", edge.getSource().getVertex().getNamespace());
				assertEquals("g0", edge.getSource().getVertex().getId());
				assertEquals("category", edge.getTarget().getVertex().getNamespace());
				assertEquals("c", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("e3")) {
				assertEquals("nodes", edge.getSource().getVertex().getNamespace());
				assertEquals("g1", edge.getSource().getVertex().getId());
				assertEquals("nodes", edge.getTarget().getVertex().getNamespace());
				assertEquals("v1", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("e4")) {
				assertEquals("nodes", edge.getSource().getVertex().getNamespace());
				assertEquals("g1", edge.getSource().getVertex().getId());
				assertEquals("nodes", edge.getTarget().getVertex().getNamespace());
				assertEquals("v2", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedSource-e5")) {
				assertEquals("category", edge.getSource().getVertex().getNamespace());
				assertEquals("c", edge.getSource().getVertex().getId());
				assertEquals("nodes", edge.getTarget().getVertex().getNamespace());
				assertEquals("v3", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedSource-e6")) {
				assertEquals("category", edge.getSource().getVertex().getNamespace());
				assertEquals("c", edge.getSource().getVertex().getId());
				assertEquals("nodes", edge.getTarget().getVertex().getNamespace());
				assertEquals("v4", edge.getTarget().getVertex().getId());
			} else {
				fail("Unexpected edge found: " + edge.toString());
			}
		}
	}

	@Test
	public void testGraphProvider() {
		DefaultVertexHopCriteria criteria = new DefaultVertexHopCriteria(new DefaultVertexRef("nodes", "g0"));
		m_provider.getVertices(criteria);

		assertEquals(0, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g0")));
		assertEquals(1, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g1")));
		assertEquals(1, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g2")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v1")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v2")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v3")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v4")));

		// Make sure that the hop provider isn't showing nodes with parent/child relationships like the
		// older grouping providers did
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g0")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g1")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g2")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v1")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v2")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v3")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v4")));

		criteria = new DefaultVertexHopCriteria(new DefaultVertexRef("nodes", "v1"));
		m_provider.getVertices(criteria);

		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g0")));
		assertEquals(1, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g1")));
		assertEquals(3, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g2")));
		assertEquals(0, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v1")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v2")));
		assertEquals(4, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v3")));
		assertEquals(4, m_provider.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v4")));

		// Make sure that the hop provider isn't showing nodes with parent/child relationships like the
		// older grouping providers did
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g0")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g1")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "g2")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v1")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v2")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v3")));
		assertNull(m_provider.getParent(new DefaultVertexRef("nodes", "v4")));
	}

	@Test
	public void testCollapseVertices() {
		List<Vertex> vertices = m_provider.getVertices(new Criteria[] { new TestCollapsibleCriteria() });

		// Test vertex that replaces the collapsed vertices
		assertTrue(vertices.contains(new DefaultVertexRef("nodes", TEST_ID)));

		// These vertices should be "collapsed"
		assertFalse(vertices.contains(new DefaultVertexRef("nodes", "g0")));
		assertFalse(vertices.contains(new DefaultVertexRef("nodes", "g1")));
		assertFalse(vertices.contains(new DefaultVertexRef("nodes", "g2")));
		
		// These vertices remain uncollapsed
		assertTrue(vertices.contains(new DefaultVertexRef("nodes", "v1")));
		assertTrue(vertices.contains(new DefaultVertexRef("nodes", "v2")));
		assertTrue(vertices.contains(new DefaultVertexRef("nodes", "v3")));
		assertTrue(vertices.contains(new DefaultVertexRef("nodes", "v4")));
	}
}
