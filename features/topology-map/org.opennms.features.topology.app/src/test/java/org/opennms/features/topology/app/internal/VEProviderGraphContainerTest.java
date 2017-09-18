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

package org.opennms.features.topology.app.internal;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.support.SemanticZoomLevelCriteria;
import org.opennms.features.topology.api.support.SimpleGraphBuilder;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractEdgeRef;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleMetaTopologyProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;

public class VEProviderGraphContainerTest {

	private GraphProvider m_graphProvider;
	private GraphContainer m_graphContainer;
	private Set<VertexRef> m_expectedVertices = new HashSet<>();
	private Map<VertexRef, String> m_expectedVertexStyles = new HashMap<>();
	private Set<EdgeRef> m_expectedEdges = new HashSet<>();
	private Map<EdgeRef, String> m_expectedEdgeStyles = new HashMap<>();

	private static abstract class TestCollapsibleCriteria extends VertexHopGraphProvider.VertexHopCriteria implements CollapsibleCriteria {

		public TestCollapsibleCriteria() {
			super("Collapsed vertex");
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
		public abstract Set<VertexRef> getVertices();

		protected abstract String getCollapsedId();

		@Override
		public Vertex getCollapsedRepresentation() {
			AbstractVertex retval = new AbstractVertex("nodes", getCollapsedId(), "Collapsed vertex");
			retval.setStyleName("test");
			return retval;
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

	private static class TestCriteria1 extends TestCollapsibleCriteria {

		protected String getCollapsedId() {
			return "test";
		}

		public Set<VertexRef> getVertices() {
			Set<VertexRef> retval = new HashSet<>();
			retval.add(new DefaultVertexRef("nodes", "v2", "vertex2"));
			retval.add(new DefaultVertexRef("nodes", "v4", "vertex4"));
			return retval;
		}
	}

	private static class TestCriteria2 extends TestCollapsibleCriteria {

		protected String getCollapsedId() {
			return "collapse-v3";
		}

		public Set<VertexRef> getVertices() {
			Set<VertexRef> retval = new HashSet<>();
			retval.add(new DefaultVertexRef("nodes", "v3", "vertex3"));
			return retval;
		}
	}

	@Before
	public void setUp() {

		MockLogAppender.setupLogging();

		m_graphProvider = new SimpleGraphBuilder("nodes")
			.vertex("g0").vLabel("group0").vIconKey("group").vTooltip("root group").vStyleName("vertex")
			.vertex("g1").parent("g0").vLabel("group1").vIconKey("group").vTooltip("group 1").vStyleName("vertex")
			.vertex("v1").parent("g1").vLabel("vertex1").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v2").parent("g1").vLabel("vertex2").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("g2").parent("g0").vLabel("group2").vIconKey("group").vTooltip("group 2").vStyleName("vertex")
			.vertex("v3").parent("g2").vLabel("vertex3").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v4").parent("g2").vLabel("vertex4").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.edge("e1", "v1", "v2").eStyleName("edge")
			.edge("e2", "v2", "v3").eStyleName("edge")
			.edge("e3", "v3", "v4").eStyleName("edge")
			.edge("e4", "v4", "v1").eStyleName("edge")
			.get();


		MetaTopologyProvider metaTopologyProvider = new SimpleMetaTopologyProvider(m_graphProvider);
		DefaultTopologyService topologyService = new DefaultTopologyService();
		topologyService.setServiceLocator(new SimpleServiceLocator(metaTopologyProvider));

        VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
		graphContainer.setSemanticZoomLevel(0);
		graphContainer.setTopologyService(topologyService);
		graphContainer.setSelectedNamespace(m_graphProvider.getNamespace());
		graphContainer.setMetaTopologyId(m_graphProvider.getNamespace());

		m_graphContainer = graphContainer;
	}

	@Test
	public void testGraphProvider() {
		List<? extends Vertex> roots = m_graphProvider.getRootGroup();
		assertEquals(1, roots.size());
		Vertex root = roots.get(0);
		assertNotNull(root);

		assertEquals("nodes", root.getNamespace());
		assertEquals("g0", root.getId());

		List<? extends Vertex> children = m_graphProvider.getChildren(root);
		assertEquals(2, children.size());
		assertEquals(root, m_graphProvider.getParent(children.get(0)));
	}

	@Test
	public void testContainerWithHopProvider() throws Exception {
        VertexHopGraphProvider vertexHopGraphProvider = new VertexHopGraphProvider(m_graphProvider);

        DefaultTopologyService topologyService = new DefaultTopologyService();
        SimpleMetaTopologyProvider simpleMetaTopologyProvider = new SimpleMetaTopologyProvider(vertexHopGraphProvider);
		topologyService.setServiceLocator(new SimpleServiceLocator(simpleMetaTopologyProvider));

		// Wrap the test GraphProvider in a VertexHopGraphProvider
		VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
		graphContainer.setSemanticZoomLevel(0);
		graphContainer.setTopologyService(topologyService);
		graphContainer.setMetaTopologyId(simpleMetaTopologyProvider.getId());
		graphContainer.setSelectedNamespace(vertexHopGraphProvider.getNamespace());

		m_graphContainer = graphContainer;

		// There should be zero vertices or edges if no focus vertices are set
		Graph graph = m_graphContainer.getGraph();
		assertEquals(0, graph.getDisplayVertices().size());
		assertEquals(0, graph.getDisplayEdges().size());

		// Add one focus vertex
		VertexHopGraphProvider.DefaultVertexHopCriteria hopCriteria = new VertexHopGraphProvider.DefaultVertexHopCriteria(new DefaultVertexRef("nodes", "v1"));
		m_graphContainer.addCriteria(hopCriteria);
		// This needs to be 2 because there is a SemanticZoomLevelCriteria in there also
		assertEquals(2, m_graphContainer.getCriteria().length);

		// Verify that a single vertex is in the graph
		graph = m_graphContainer.getGraph();
		assertEquals(1, graph.getDisplayVertices().size());
		assertEquals(0, graph.getDisplayEdges().size());

		expectVertex("nodes", "v1", "vertex");
		graph.visit(verifier());
		verify();
		verifyConnectedness(graph);
		reset();


		// Change SZL to 1
		m_graphContainer.setSemanticZoomLevel(1);
		assertEquals(2, m_graphContainer.getCriteria().length);

		// Focus vertex
		expectVertex("nodes", "v1", "vertex");
		expectVertex("nodes", "v2", "vertex");
		/*
			This is a problem with the VEProviderGraphContainer... it wraps a delegate GraphProvider
			in a MergingGraphProvider like so:

			VEProviderGraphContainer { MergingGraphProvider { VertexHopGraphProvider } } }

			But for the VertexHopProvider to calculate the SZL correctly, it needs to be aware of all
			edges, including those provided by the MergingGraphProvider. So we should rearrange things
			so that they are laid out like:

			VEProviderGraphContainer { VertexHopGraphProvider { MergingGraphProvider } } }

			We should decouple the MergingGraphProvider from the VEProviderGraphContainer and then just
			inject them in the correct order. When this problem is fixed, uncomment all of the lines that
			are commented out in this test.
		*/
		//expectVertex("nodes", "v3", "vertex");
		expectVertex("nodes", "v4", "vertex");

		expectEdge("nodes", "e1", "edge");
		//expectEdge("nodes", "e2", "edge");
		//expectEdge("nodes", "e3", "edge");
		expectEdge("nodes", "e4", "edge");

		graph = m_graphContainer.getGraph();
		//assertEquals(4, graph.getDisplayVertices().size());
		//assertEquals(5, graph.getDisplayEdges().size());
		assertEquals(3, graph.getDisplayVertices().size());
		assertEquals(2, graph.getDisplayEdges().size());

		graph.visit(verifier());
		verify();
		verifyConnectedness(graph);
		reset();


		// Add a collapsed criteria to the container
		Criteria collapsibleCriteria = new TestCriteria1();
		m_graphContainer.addCriteria(collapsibleCriteria);
		assertEquals(3, m_graphContainer.getCriteria().length);

		// Make sure that the TestCollapsibleCriteria is mapping "v2" and "v4" to the collapsed "test" vertex
		Map<VertexRef,Set<Vertex>> collapsed = VertexHopGraphProvider.getMapOfVerticesToCollapsedVertices(
				VertexHopGraphProvider.getCollapsedCriteria(m_graphContainer.getCriteria())
		);
		assertTrue(collapsed.containsKey(new DefaultVertexRef("nodes", "v2")));
		assertTrue(collapsed.containsKey(new DefaultVertexRef("nodes", "v4")));
		assertTrue(collapsed.get(new DefaultVertexRef("nodes", "v2")).equals(Collections.singleton(new DefaultVertexRef("nodes", "test"))));
		assertTrue(collapsed.get(new DefaultVertexRef("nodes", "v4")).equals(Collections.singleton(new DefaultVertexRef("nodes", "test"))));

		assertEquals(
            m_graphContainer.getGraph().getDisplayVertices().toString(),
			3,
			m_graphContainer.getGraph().getDisplayVertices().size()
		);
		assertEquals(
			m_graphContainer.getTopologyServiceClient().getGraphProviderBy("nodes").getVertices(new TestCriteria1()).toString(),
			3,
			m_graphContainer.getTopologyServiceClient().getGraphProviderBy("nodes").getVertices(new TestCriteria1()).size()
		);

		expectVertex("nodes", "v1", "vertex");
		expectVertex("nodes", "v3", "vertex");
		// Collapsed vertex that contains v2 and v4
		expectVertex("nodes", "test", "test");

		expectEdge("nodes", "collapsedTarget-e1", "edge");
		expectEdge("nodes", "collapsedSource-e2", "edge");
		expectEdge("nodes", "collapsedTarget-e3", "edge");
		expectEdge("nodes", "collapsedSource-e4", "edge");

		graph = m_graphContainer.getGraph();

		assertEquals(3, graph.getDisplayVertices().size());
		assertEquals(4, graph.getDisplayEdges().size());

		for (Edge edge : graph.getDisplayEdges()) {
			if (edge.getId().equals("collapsedTarget-e1")) {
				assertEquals("v1", edge.getSource().getVertex().getId());
				assertEquals("test", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedSource-e2")) {
				assertEquals("test", edge.getSource().getVertex().getId());
				assertEquals("v3", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedTarget-e3")) {
				assertEquals("v3", edge.getSource().getVertex().getId());
				assertEquals("test", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedSource-e4")) {
				assertEquals("test", edge.getSource().getVertex().getId());
				assertEquals("v1", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("ncs1")) {
				assertEquals("v1", edge.getSource().getVertex().getId());
				assertEquals("v3", edge.getTarget().getVertex().getId());
			} else {
				fail("Unknown edge ID: " + edge.getId());
			}
		}

		graph.visit(verifier());
		verify();
		verifyConnectedness(graph);
		reset();

		// Remove the collapsed criteria and make sure that the state reverts correctly
		m_graphContainer.removeCriteria(collapsibleCriteria);
		graph = m_graphContainer.getGraph();

		//assertEquals(4, graph.getDisplayVertices().size());
		//assertEquals(5, graph.getDisplayEdges().size());
		assertEquals(3, graph.getDisplayVertices().size());
		assertEquals(2, graph.getDisplayEdges().size());

		collapsibleCriteria = new TestCriteria1();
		m_graphContainer.addCriteria(collapsibleCriteria);
		collapsibleCriteria = new TestCriteria2();
		m_graphContainer.addCriteria(collapsibleCriteria);
		assertEquals(4, m_graphContainer.getCriteria().length);

		graph = m_graphContainer.getGraph();

		assertEquals(3, m_graphContainer.getGraph().getDisplayVertices().size());
		/*
		 * One edge is missing because of the VertexHopGraphProvider issue mentioned above.
		assertEquals(
			ArrayUtils.toString(m_graphContainer.getGraph().getDisplayEdges()),
			5,
			m_graphContainer.getGraph().getDisplayEdges().size()
		);
		 */
		assertEquals(4, m_graphContainer.getGraph().getDisplayEdges().size());

		for (Edge edge : graph.getDisplayEdges()) {
			if (edge.getId().equals("collapsedTarget-e1")) {
				assertEquals("v1", edge.getSource().getVertex().getId());
				assertEquals("test", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsed-e2")) {
				assertEquals("test", edge.getSource().getVertex().getId());
				assertEquals("collapse-v3", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsed-e3")) {
				assertEquals("collapse-v3", edge.getSource().getVertex().getId());
				assertEquals("test", edge.getTarget().getVertex().getId());
			} else if (edge.getId().equals("collapsedSource-e4")) {
				assertEquals("test", edge.getSource().getVertex().getId());
				assertEquals("v1", edge.getTarget().getVertex().getId());
			/**
			 * This edge is not found because of the issue mentioned above.
			} else if (edge.getId().equals("collapsedTarget-ncs1")) {
				assertEquals("v1", edge.getSource().getVertex().getId());
				assertEquals("collapse-v3", edge.getTarget().getVertex().getId());
			 */
			} else {
				fail("Unknown edge ID: " + edge.getId());
			}
		}
	}

	@Test
	public void testContainer() throws Exception {

		Graph graph = m_graphContainer.getGraph();

		expectVertex("nodes", "g0", "vertex");

		graph.visit(verifier());

		verify();
		verifyConnectedness(graph);

		reset();

		m_graphContainer.setSemanticZoomLevel(1);

		expectVertex("nodes", "g1", "vertex");
		expectVertex("nodes", "g2", "vertex");

		graph = m_graphContainer.getGraph();

		graph.visit(verifier());

		verify();
		verifyConnectedness(graph);

		reset();

		m_graphContainer.addCriteria(SimpleEdgeProvider.labelMatches("ncs", "ncsedge."));

		expectVertex("nodes", "g1", "vertex");
		expectVertex("nodes", "g2", "vertex");

		graph = m_graphContainer.getGraph();

		graph.visit(verifier());

		verify();
		verifyConnectedness(graph);

		reset();

	}

	@Test
	public void testFindCriteria() {
		// The easiest test
		Set<Criteria> criteria = m_graphContainer.findCriteria(Criteria.class);
		assertNotNull(criteria);
		assertEquals(criteria.size(), m_graphContainer.getCriteria().length);

		// verify that subclasses also match
		Set<TestCriteria1> testCriteria = m_graphContainer.findCriteria(TestCriteria1.class);
		assertNotNull(testCriteria);
		assertEquals(0, testCriteria.size());

		m_graphContainer.addCriteria(new TestCriteria1());
		m_graphContainer.addCriteria(new TestCriteria2());

		testCriteria = m_graphContainer.findCriteria(TestCriteria1.class);
		assertNotNull(testCriteria);
		assertEquals(1, testCriteria.size());
	}

	@Test
	public void testFindSingleCriteria() {
		assertEquals(m_graphContainer.getCriteria()[0], m_graphContainer.findSingleCriteria(SemanticZoomLevelCriteria.class));
		assertEquals(m_graphContainer.getCriteria()[0], m_graphContainer.findSingleCriteria(Criteria.class));
		assertNull(m_graphContainer.findSingleCriteria(TestCriteria1.class));
	}

	private void verify() {
		if (!m_expectedVertices.isEmpty()) {
			fail("Expected Vertices not seen: " + m_expectedVertices);
		}

		if (!m_expectedEdges.isEmpty()) {
			fail("Expected Edges not seen: " + m_expectedEdges);
		}
	}

	private GraphVisitor verifier() {
		return new GraphVisitor() {

            @Override
            public void visitGraph(Graph graph) throws Exception {

            }

            @Override
            public void completeGraph(Graph graph) throws Exception {

            }

            @Override
			public void visitVertex(Vertex vertex) {
				assertTrue("Unexpected vertex " + vertex + " encountered!", m_expectedVertices.contains(vertex));
				m_expectedVertices.remove(vertex);
				assertEquals("Unexpected style for vertex " + vertex, m_expectedVertexStyles.get(vertex), vertex.getStyleName());
			}

			@Override
			public void visitEdge(Edge edge) {
				assertTrue("Unexpected edge " + edge + " encountered!", m_expectedEdges.contains(edge));
				m_expectedEdges.remove(edge);
				assertEquals("Unexpected style for edge " + edge, m_expectedEdgeStyles.get(edge), edge.getStyleName());
			}

		};
	}



	private void expectVertex(String namespace, String vertexId, String styles) {
		DefaultVertexRef vertexRef = new DefaultVertexRef(namespace, vertexId);
		m_expectedVertices.add(vertexRef);
		m_expectedVertexStyles.put(vertexRef, styles);
	}

	private void expectEdge(String namespace, String edgeId, String styles) {
		AbstractEdgeRef edgeRef = new AbstractEdgeRef(namespace, edgeId);
		m_expectedEdges.add(edgeRef);
		m_expectedEdgeStyles.put(edgeRef, styles);
	}

	private static void verifyConnectedness(Graph graph) {
		Collection<Vertex> vertices = graph.getDisplayVertices();
		for (Edge edge : graph.getDisplayEdges()) {
			assertTrue(vertices.contains(edge.getSource().getVertex()));
			assertTrue(vertices.contains(edge.getTarget().getVertex()));
		}
	}

	private void reset() {
		m_expectedVertices.clear();
		m_expectedEdges.clear();
		m_expectedVertexStyles.clear();
		m_expectedEdgeStyles.clear();
	}
}
