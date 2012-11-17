package org.opennms.features.topology.app.internal;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Vertex;

public class VEProviderGraphContainerTest {

	private SimpleGraphProvider m_graphProvider;
	private SimpleEdgeProvider m_edgeProvider;
	private GraphContainer m_graphContainer;

	
	@Before
	public void setUp() {
		
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

		m_edgeProvider = new SimpleEdgeBuilder("ncs")
			.edge("ncs1", "nodes", "v1", "nodes", "v3")
			.edge("ncs2", "nodes", "v2", "nodes", "v4")
			.get();
		
		VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
		graphContainer.setBaseTopology(m_graphProvider);
		graphContainer.addEdgeProvider(m_edgeProvider);
		
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
	public void testContainer() {
			
		m_graphContainer.getGraph();
		
		
		
	}

}
