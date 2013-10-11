package org.opennms.features.topology.plugins.topo.sfree.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class VertexHopGraphProviderScaleTest {

	private VertexHopGraphProvider m_provider;
	private int m_vertexCount;
	private int m_edgeCount;

	
	@Before
	public void setUp() {

		SFreeTopologyProvider baseProvider = new SFreeTopologyProvider();
		baseProvider.setNodeCount(8000);
		baseProvider.setConnectedness(1.5);
		baseProvider.load(SFreeTopologyProvider.ERDOS_RENIS);
		
		m_vertexCount = baseProvider.getVertices().size();
		m_edgeCount = baseProvider.getEdges().size();
		System.err.println(m_vertexCount);
		System.err.println(m_edgeCount);
		
		m_provider = new VertexHopGraphProvider(baseProvider);
		
		//System.err.printf("SZL: %d\n", m_provider.
	}
	
	public int randomInt(int max) {
		return (int) Math.round(Math.random()*max);
	}
	
	@SuppressWarnings("deprecation")
	public VertexRef randomVertex() {
		return new AbstractVertexRef("sfree", Integer.toString(randomInt(m_vertexCount)));
	}
	
	@Test
	public void testGraphProvider() {
		FocusNodeHopCriteria criteria = new FocusNodeHopCriteria();
		int focusNodeCount = 1;
		for(int i = 0; i < focusNodeCount; i++) {
			criteria.add(randomVertex());
		}
		
		System.err.printf("Focus Nodes: %s\n", criteria.getVertices());
		
		int count = 1;
		long start = System.nanoTime();
		int found = 0;
		for(int i = 0; i < count; i++) {
			found += m_provider.getVertices(criteria).size();
		}
		long end = System.nanoTime();
		
		double time = (end-start)/(count*1000000.0);
		System.err.printf("ElapsedTime = %f ms\n", time);
		System.err.printf("%d, %d, %f, %f\n", m_vertexCount, m_edgeCount, time, found/((double)count));
		

	}
}
