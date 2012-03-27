package org.opennms.features.vaadin.topology.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Graph{
	private Vertex[] m_vertices;
	private Edge[] m_edges;
	private Map<Integer, Vertex> m_vertexMap = new HashMap<Integer, Vertex>();

	
	public Graph(){
		m_vertices = new Vertex[] {new Vertex(1,60,25), new Vertex(2, 50, 50), new Vertex(3, 33, 75), new Vertex(4, 67, 75) };
		m_edges = new Edge[] {new Edge(m_vertices[0], m_vertices[1]), new Edge(m_vertices[1], m_vertices[2]), new Edge(m_vertices[1], m_vertices[3])};
		
		for(Vertex vert : m_vertices) {
			m_vertexMap.put(vert.getId(), vert);
		}
	};
	public Graph(Vertex[] nodes, Edge[] links){
		m_vertices = nodes;
		m_edges = links;
		for(Vertex vert : m_vertices) {
			m_vertexMap.put(vert.getId(), vert);
		}
	}
	
	public Vertex[] getVertices(){
		return m_vertices;
	}
	
	public Edge[] getEdges(){
		return m_edges;
	}
	
	public Vertex getVertexById(int id) {
		return m_vertexMap.get(id);
	}
	public void addVertex(Vertex vertex) {
		List<Vertex> vertices = new ArrayList<Vertex> (Arrays.asList(m_vertices));
		vertices.add(vertex); 
		m_vertices = vertices.toArray(new Vertex[0]);
		
	}
	public void addEdge(Edge edge) {
		List<Edge> edges = new ArrayList<Edge> (Arrays.asList(m_edges));
		edges.add(edge);
		m_edges = edges.toArray(new Edge[0]);
		
	}
	
}