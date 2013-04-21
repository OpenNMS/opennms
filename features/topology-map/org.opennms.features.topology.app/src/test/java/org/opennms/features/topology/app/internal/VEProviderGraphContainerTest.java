package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.opennms.features.topology.api.topo.AbstractEdgeRef;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class VEProviderGraphContainerTest {

	private GraphProvider m_graphProvider;
	private EdgeProvider m_edgeProvider;
	private GraphContainer m_graphContainer;
	private Set<VertexRef> m_expectedVertices = new HashSet<VertexRef>();
	private Map<VertexRef, String> m_expectedVertexStyles = new HashMap<VertexRef, String>();
	private Set<EdgeRef> m_expectedEdges = new HashSet<EdgeRef>();
	private Map<EdgeRef, String> m_expectedEdgeStyles = new HashMap<EdgeRef, String>();

	
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

		m_edgeProvider = new SimpleEdgeBuilder("ncs", "nodes")
			.edge("ncs1", "nodes", "v1", "nodes", "v3").label("ncsedge1").styleName("ncs edge")
			.edge("ncs2", "nodes", "v2", "nodes", "v4").label("ncsedge2").styleName("ncs edge")
			.edge("ncs3", "nodes", "v1", "nodes", "v2").label("ncsedge3").styleName("ncs edge")
			.get();
		
		ProviderManager providerManager = new ProviderManager();
		providerManager.onEdgeProviderBind(m_edgeProvider);

		GraphContainer graphContainer = new VEProviderGraphContainer(m_graphProvider, providerManager);
		
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
	public void testContainer() throws Exception {
			
		Graph graph = m_graphContainer.getGraph();
	
		expectVertex("nodes", "g0", "vertex");
		
		graph.visit(verifier());
		
		verify();
		
		reset();
		
		m_graphContainer.setSemanticZoomLevel(1);
		
		expectVertex("nodes", "g1", "vertex");
		expectVertex("nodes", "g2", "vertex");
		expectEdge("pseudo-nodes", "<nodes:g1>-<nodes:g2>", "edge");
		
		graph = m_graphContainer.getGraph();
		
		graph.visit(verifier());
		
		verify();
		
		reset();
		
		m_graphContainer.setCriteria(SimpleEdgeProvider.labelMatches("ncs", "ncsedge."));
		
		expectVertex("nodes", "g1", "vertex");
		expectVertex("nodes", "g2", "vertex");
		expectEdge("pseudo-nodes", "<nodes:g1>-<nodes:g2>", "edge");
		expectEdge("pseudo-ncs", "<nodes:g1>-<nodes:g2>", "ncs edge");

		graph = m_graphContainer.getGraph();
		
		graph.visit(verifier());
		
		verify();
		
		reset();

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
		return new BaseGraphVisitor() {
			
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
		AbstractVertexRef vertexRef = new AbstractVertexRef(namespace, vertexId);
		m_expectedVertices.add(vertexRef);
		m_expectedVertexStyles.put(vertexRef, styles);
	}
	
	private void expectEdge(String namespace, String edgeId, String styles) {
		AbstractEdgeRef edgeRef = new AbstractEdgeRef(namespace, edgeId);
		m_expectedEdges.add(edgeRef);
		m_expectedEdgeStyles.put(edgeRef, styles);
	}
	
	private void reset() {
		m_expectedVertices.clear();
		m_expectedEdges.clear();
		m_expectedVertexStyles.clear();
		m_expectedEdgeStyles.clear();
	}
}
