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
package org.opennms.features.topology.api.topo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.simple.SimpleGraphBuilder;

public class CollapsibleGraphTest {

	private static final String TEST_ID = "TEST";

	private static class TestCollapsibleCriteria extends VertexHopCriteria implements CollapsibleCriteria {

		public TestCollapsibleCriteria() {
			super("TEST VERTEX");
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

	private CollapsibleGraph collapsibleGraph;

	@Before
	public void setUp() {
		MockLogAppender.setupLogging();

		final BackendGraph baseGraph = new SimpleGraphBuilder("nodes")
			.vertex("g0").vLabel("group0").vIconKey("group").vTooltip("root group").vStyleName("vertex")
			.vertex("g1").vLabel("group1").vIconKey("group").vTooltip("group 1").vStyleName("vertex")
			.vertex("v1").vLabel("vertex1").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v2").vLabel("vertex2").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("g2").vLabel("group2").vIconKey("group").vTooltip("group 2").vStyleName("vertex")
			.vertex("v3").vLabel("vertex3").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v4").vLabel("vertex4").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.edge("e1", "g0", "g1").eLabel("edge1").eStyleName("edge")
			.edge("e2", "g0", "g2").eLabel("edge2").eStyleName("edge")
			.edge("e3", "g1", "v1").eLabel("edge3").eStyleName("edge")
			.edge("e4", "g1", "v2").eLabel("edge4").eStyleName("edge")
			.edge("e5", "g2", "v3").eLabel("edge5").eStyleName("edge")
			.edge("e6", "g2", "v4").eLabel("edge6").eStyleName("edge")
			.get();

		collapsibleGraph = new CollapsibleGraph(baseGraph);
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

		final Set<Edge> edges = CollapsibleGraph.collapseEdges(new HashSet<>(collapsibleGraph.getEdges()), new CollapsibleCriteria[] { collapseMe });
		for (Edge edge : edges) {
			assertEquals("nodes", edge.getNamespace());
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
		collapsibleGraph.getVertices(criteria); // calculate szl

		assertEquals(0, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g0")));
		assertEquals(1, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g1")));
		assertEquals(1, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g2")));
		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v1")));
		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v2")));
		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v3")));
		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v4")));

		criteria = new DefaultVertexHopCriteria(new DefaultVertexRef("nodes", "v1"));
		collapsibleGraph.getVertices(criteria); // re-calculate szl

		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g0")));
		assertEquals(1, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g1")));
		assertEquals(3, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "g2")));
		assertEquals(0, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v1")));
		assertEquals(2, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v2")));
		assertEquals(4, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v3")));
		assertEquals(4, collapsibleGraph.getSemanticZoomLevel(new DefaultVertexRef("nodes", "v4")));
	}

	@Test
	public void testCollapseVertices() {
		final List<Vertex> vertices = collapsibleGraph.getVertices(new Criteria[] { new TestCollapsibleCriteria() });

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
