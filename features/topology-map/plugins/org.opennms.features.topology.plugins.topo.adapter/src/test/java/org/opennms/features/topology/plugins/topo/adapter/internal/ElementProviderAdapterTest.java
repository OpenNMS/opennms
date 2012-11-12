package org.opennms.features.topology.plugins.topo.adapter.internal;

import static org.junit.Assert.*;
	
import org.junit.Before;
import org.junit.Test;

public class ElementProviderAdapterTest {
	
	SimpleGraphProvider m_linkd;
	SimpleEdgeProvider m_ncs;
	
	@Before
	public void setUp() {
		
		m_linkd = new SimpleGraphBuilder("linkd")
			.vertex("v1").vLabel("node1").vTooltip("This is node1").vIconKey("linkd:server").vStyleName("server")
			.vertex("v2").vLabel("node2").vTooltip("This is node2").vIconKey("linkd:server").vStyleName("server")
			.vertex("v3").vLabel("node3").vTooltip("This is node3").vIconKey("linkd:server").vStyleName("server")
			.vertex("v4").vLabel("node4").vTooltip("This is node4").vIconKey("linkd:server").vStyleName("server")
			.edge("e1", "v1", "v2").eLabel("edge1").eTooltip("This is edge 1").eStyleName("linkd-edge")
			.edge("e2", "v2", "v3").eLabel("edge2").eTooltip("This is edge 2").eStyleName("linkd-edge")
			.edge("e3", "v2", "v4").eLabel("edge3").eTooltip("This is edge 3").eStyleName("linkd-edge")
			.get();

		m_ncs = new SimpleEdgeBuilder("ncs")
			.edge("e4", "linkd", "v3", "linkd", "v4").label("ncs label").tooltip("This is an ncs edge!").styleName("ncs-edge")
			.get();

	}

	@Test
	public void test() {
		assertTrue(true);
	}

}
