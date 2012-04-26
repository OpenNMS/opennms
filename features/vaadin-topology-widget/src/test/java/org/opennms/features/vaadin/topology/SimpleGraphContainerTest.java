package org.opennms.features.vaadin.topology;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleGraphContainerTest {

	@Test
	public void test() {
		SimpleGraphContainer container = new SimpleGraphContainer();
		
		container.addVertex("a", 50, 100);
		container.addVertex("b", 100, 50);
		container.addVertex("c", 100, 150);
		container.addVertex("d", 150, 100);
		
		container.connectVertices("e1", "a", "b");
		container.connectVertices("e2", "a", "c");
		container.connectVertices("e3", "b", "c");
		container.connectVertices("e4", "b", "d");
		container.connectVertices("e5", "c", "d");
		
		container.save();
		
		container.load();
		
	}
	
	
	

}
