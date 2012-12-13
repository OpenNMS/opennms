package org.opennms.features.topology.plugins.topo.adapter.internal;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.plugins.topo.adapter.TPGraphProvider;
import org.opennms.features.topology.plugins.topo.simple.internal.SimpleTopologyProvider;

public class TPGraphProviderTest {
	
	
	SimpleTopologyProvider m_topoProvider;
	TPGraphProvider m_graphProvider;
	
	@Before
	public void setUp() {
		m_topoProvider = new SimpleTopologyProvider();
		m_graphProvider = new TPGraphProvider(m_topoProvider);
	}
	
	@Test
	public void testVertices() {
		Vertex vertex = m_graphProvider.getVertex("vmware", "192.168.30.138");
		assertNotNull(vertex);
		assertEquals("vmware", vertex.getNamespace());
		assertEquals("192.168.30.138", vertex.getId());
		assertEquals("Captn Crunch (192.168.30.138)", vertex.getLabel());
		assertEquals("DATACENTER_ICON", vertex.getIconKey());
		
		m_graphProvider.getRootGroup().contains(vertex);
		
		assertTrue(m_graphProvider.hasChildren(vertex));
		
		assertEquals("vmware", m_graphProvider.getNamespace());
		
	}
	
	/*
	 <edge>
        <id>192.168.30.138/host-52->network-30</id>
        <source>192.168.30.138/host-52</source>
        <target>192.168.30.138/network-30</target>
     </edge>
	 */
	
	@Test
	public void testEdges() {
		Edge edge = m_graphProvider.getEdge("vmware", "192.168.30.138/host-52->network-30");
		assertNotNull(edge);
		assertEquals("vmware", edge.getNamespace());
		assertEquals("192.168.30.138/host-52->network-30", edge.getId());
		
		Vertex source = m_graphProvider.getVertex(edge.getSource().getVertex());
		Vertex target = m_graphProvider.getVertex(edge.getTarget().getVertex());
		
		assertEquals("vmware", source.getNamespace());
		assertEquals("192.168.30.138/host-52", source.getId());
		assertEquals("192.168.30.142", source.getLabel());
		assertEquals("HOSTSYSTEM_ICON_UNKNOWN", source.getIconKey());
		
		assertEquals("vmware", source.getNamespace());
		assertEquals("192.168.30.138/network-30", target.getId());
		assertEquals("VM Network", target.getLabel());
		assertEquals("NETWORK_ICON", target.getIconKey());
	}
	
	@Test
	public void testVertexEvents() {
		
		VertexListener listener = new VertexListener() {

			@Override
			public void vertexSetChanged(VertexProvider provider) {
				throw new UnsupportedOperationException("VertexListener.vertexSetChanged is not yet implemented.");
			}

			@Override
			public void vertexSetChanged(VertexProvider provider,
					List<? extends Vertex> added,
					List<? extends Vertex> update,
					List<String> removedVertexIds)
			{
				System.err.printf("Added: %d, Updating: %d, Removing: %d\n", added.size(), update.size(), removedVertexIds.size());
			}
			
		};
		
		m_graphProvider.addVertexListener(listener);
		
		m_topoProvider.load("src/test/resources/test-graph.xml");
		
	}

}
