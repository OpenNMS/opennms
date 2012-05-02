package org.opennms.features.vaadin.topology;

import static org.opennms.features.vaadin.app.TopologyWidgetTestApplication.SERVER_ICON;
import static org.opennms.features.vaadin.app.TopologyWidgetTestApplication.GROUP_ICON;
import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleGraphContainerTest {

	@Test
	public void test() {
		SimpleGraphContainer container = new SimpleGraphContainer();
		
		container.addVertex("a", 50, 100, SERVER_ICON);
		container.addVertex("b", 100, 50, SERVER_ICON);
		container.addVertex("c", 100, 150, SERVER_ICON);
		container.addVertex("d", 150, 100, SERVER_ICON);
		container.addVertex("e", 200, 200, SERVER_ICON);
		container.addGroup("g1", GROUP_ICON);
		container.addGroup("g2", GROUP_ICON);
		container.getVertexContainer().setParent("a", "g1");
		container.getVertexContainer().setParent("b", "g1");
		container.getVertexContainer().setParent("c", "g2");
		container.getVertexContainer().setParent("d", "g2");
		
		container.connectVertices("e1", "a", "b");
		container.connectVertices("e2", "a", "c");
		container.connectVertices("e3", "b", "c");
		container.connectVertices("e4", "b", "d");
		container.connectVertices("e5", "c", "d");
		container.connectVertices("e6", "a", "e");
		container.connectVertices("e7", "d", "e");
		
		container.save("test-graph.xml");
		
		container.load("test-graph.xml");
		
	}
	
	
	

}
