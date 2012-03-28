package org.opennms.features.vaadin.topology.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Graph{
	private List<Vertex> m_vertices;
	private List<Edge> m_edges;
	private Map<Integer, Vertex> m_vertexMap = new HashMap<Integer, Vertex>();

	
	public Graph(){
		m_vertices = new ArrayList<Vertex>(Arrays.asList(new Vertex[] {new Vertex(0,60,25)}));
		m_edges = new ArrayList<Edge>();
		
		for(Vertex vert : m_vertices) {
			m_vertexMap.put(vert.getId(), vert);
		}
		
		updateLayout();
	}
	public Graph(List<Vertex> vertex, List<Edge> edges){
		m_vertices = vertex;
		m_edges = edges;
		for(Vertex vert : m_vertices) {
			m_vertexMap.put(vert.getId(), vert);
		}
		updateLayout();
	}
	
	public List<Vertex> getVertices(){
		return m_vertices;
	}
	
	public List<Edge> getEdges(){
		return m_edges;
	}
	
	public Vertex getVertexById(int id) {
		return m_vertexMap.get(id);
	}
	public void addVertex(Vertex vertex) {
		m_vertices.add(vertex);
		
		updateLayout();
	}
	public void addEdge(Edge edge) {
		m_edges.add(edge);
	}
	
	private void updateLayout() {
	    int r = 30;
	    int cx = 50;
	    int cy = 50;
	    for(int i = 0; i < m_vertices.size(); i++) {
	        Vertex vertex = m_vertices.get(i);
	        if(i == 0) {
	            vertex.setX(cx);
	            vertex.setY(cy);
	        }else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(m_vertices.size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);
    	        
    	        vertex.setX(x);
    	        vertex.setY(y);
	        }
	    }
	}
    public void removeRandomVertext() {
        int random = (int) ((Math.random() * (m_vertices.size() - 2)) + 1);
        Vertex vert = m_vertices.remove(random);
        
        Iterator<Edge> it = m_edges.iterator();
        while(it.hasNext()) {
            Edge edge = it.next();
            if(edge.getSource() == vert || edge.getTarget() == vert) {
                it.remove();
            }
        }
        
    }
    public Map<String, Object> getGraphAsMap() {
        Map<String, Object> graph = new HashMap<String, Object>();
        graph.put("vertex", m_vertices.get(0));
        graph.put("edge", m_edges.get(0));
        return graph;
    }
	
}