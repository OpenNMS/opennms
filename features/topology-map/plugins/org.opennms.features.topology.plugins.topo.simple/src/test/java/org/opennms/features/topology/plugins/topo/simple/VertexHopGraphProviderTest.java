package org.opennms.features.topology.plugins.topo.simple;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class VertexHopGraphProviderTest {

	private VertexHopGraphProvider m_provider;

	private static final String TEST_ID = "TEST";

	private static class TestCollapsibleCriteria extends VertexHopCriteria implements CollapsibleCriteria {

		@Override
		public boolean isCollapsed() {
			return true;
		}

		@Override
		public void setCollapsed(boolean collapsed) {
		}

		@Override
		public Set<VertexRef> getVertices() {
			Set<VertexRef> retval = new HashSet<VertexRef>();
			retval.add(new AbstractVertexRef("nodes", "g0", "g0"));
			retval.add(new AbstractVertexRef("nodes", "g1", "g1"));
			retval.add(new AbstractVertexRef("nodes", "g2", "g2"));
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
	public void testGraphProvider() {
		FocusNodeHopCriteria criteria = new FocusNodeHopCriteria();
		criteria.add(new AbstractVertexRef("nodes", "g0"));
		m_provider.getVertices(criteria);

		assertEquals(0, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "g0")));
		assertEquals(1, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "g1")));
		assertEquals(1, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "g2")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "v1")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "v2")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "v3")));
		assertEquals(2, m_provider.getSemanticZoomLevel(new AbstractVertexRef("nodes", "v4")));
	}

	@Test
	public void testCollapseVertices() {
		List<Vertex> vertices = m_provider.getVertices(new Criteria[] { new TestCollapsibleCriteria() });

		// Test vertex that replaces the collapsed vertices
		assertTrue(vertices.contains(new AbstractVertexRef("nodes", TEST_ID)));

		// These vertices should be "collapsed"
		assertFalse(vertices.contains(new AbstractVertexRef("nodes", "g0")));
		assertFalse(vertices.contains(new AbstractVertexRef("nodes", "g1")));
		assertFalse(vertices.contains(new AbstractVertexRef("nodes", "g2")));
		
		// These vertices remain uncollapsed
		assertTrue(vertices.contains(new AbstractVertexRef("nodes", "v1")));
		assertTrue(vertices.contains(new AbstractVertexRef("nodes", "v2")));
		assertTrue(vertices.contains(new AbstractVertexRef("nodes", "v3")));
		assertTrue(vertices.contains(new AbstractVertexRef("nodes", "v4")));
	}
}
