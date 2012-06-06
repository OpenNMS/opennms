package org.opennms.features.vaadin.topology;

import static org.opennms.features.topology.app.internal.Constants.GROUP_ICON;
import static org.opennms.features.topology.app.internal.Constants.SERVER_ICON;

import org.junit.Test;
import org.opennms.features.topology.app.internal.topr.SimpleTopologyProvider;

public class SimpleGraphContainerTest {

	@Test
	public void test() {
		SimpleTopologyProvider topologyProvider = new SimpleTopologyProvider();
		
		String vertexA = (String) topologyProvider.addVertex(50, 100, SERVER_ICON);
		String vertexB = (String) topologyProvider.addVertex(100, 50, SERVER_ICON);
		String vertexC = (String) topologyProvider.addVertex(100, 150, SERVER_ICON);
		String vertexD = (String) topologyProvider.addVertex(150, 100, SERVER_ICON);
		String vertexE = (String) topologyProvider.addVertex(200, 200, SERVER_ICON);
		String group1 = (String) topologyProvider.addGroup(GROUP_ICON);
		String group2 = (String) topologyProvider.addGroup(GROUP_ICON);
		topologyProvider.getVertexContainer().setParent(vertexA, group1);
		topologyProvider.getVertexContainer().setParent(vertexB, group1);
		topologyProvider.getVertexContainer().setParent(vertexC, group2);
		topologyProvider.getVertexContainer().setParent(vertexD, group2);
		
		topologyProvider.connectVertices(vertexA, vertexB);
		topologyProvider.connectVertices(vertexA, vertexC);
		topologyProvider.connectVertices(vertexB, vertexC);
		topologyProvider.connectVertices(vertexB, vertexD);
		topologyProvider.connectVertices(vertexC, vertexD);
		topologyProvider.connectVertices(vertexA, vertexE);
		topologyProvider.connectVertices(vertexD, vertexE);
		
		topologyProvider.save("test-graph.xml");
		
		topologyProvider.load("test-graph.xml");
		
	}
	
	
	

}
