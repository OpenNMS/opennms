package org.opennms.features.topology.plugins.topo.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;

public class VertexHopGraphProviderTest {

	private VertexHopGraphProvider m_provider;

	
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
		VertexHopCriteria criteria = new VertexHopCriteria();
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
}
