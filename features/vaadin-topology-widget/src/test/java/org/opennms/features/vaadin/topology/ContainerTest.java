package org.opennms.features.vaadin.topology;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class ContainerTest {
	
	/*
	 * Want to have different levels for groups from 0 to n
	 * 
	 * Each level is associated with a particular graph
	 * Be able to create groups
	 * Define and un-define groups
	 * Each group will be associated with a level greater than or equal to 1.
	 * 
	 * 
	 * A group is a collection of vertices or other groups
	 * 
	 * A group is called 'collapsed' at a level if we 'summarize' the vertices and other groups it
	 * contains using a single 'group' vertex whose edges are the sum of all of the edges
	 * connecting its contained vertices.
	 * 
	 * A group is called 'expanded' at a level we simple show its contained vertices and other groups
	 * in the topology
	 * 
	 * Relationship between levels, groups and graphs
	 * 
	 * The graph at level n has all of the groups whose level is less than n expanded and greater
	 * than or equal n collapsed.
	 * 
	 * 
	 */
	public static class TestTopologyContainer implements TopologyContainer{
		
		private Graph m_graph = new Graph();
		private int m_level;
		
		public TestTopologyContainer() {
			for(int i = 0; i < 9; i++) {
				Vertex vertex = new Vertex(m_graph.getNextId());
				m_graph.addVertex(vertex);
			}
			
		}
		
		public Graph getGraph(int level) {
			return m_graph;
		}

		public void createGroup(List<Vertex> vertices) {
			Group group = new Group(10);
			for(Vertex vertex : vertices) {
				vertex.setGroup(group);
			}
			
		}
		
	}
	
	
	
	
	@Test
	@Ignore
	public void testContainer() {
		TopologyContainer c = new TestTopologyContainer();
		Graph g = c.getGraph(0);
		assertEquals(10, g.getVertices().size());
		
		List<Vertex> verticesToGroup = g.getVertices().subList(0, 2);
		c.createGroup(verticesToGroup);
	}
	

}
