package org.opennms.features.vaadin.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Graph{
	private List<Vertex> m_vertices;
	private List<Edge> m_edges;
	private Map<String, Vertex> m_vertexMap = new HashMap<String, Vertex>();
	private int m_counter = 0;

	
	public Graph(){
		m_vertices = new ArrayList<Vertex>(Arrays.asList(new Vertex[] {new Vertex(getNextId(),60,25)}));
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
	
	public Vertex getVertexById(String id) {
		return m_vertexMap.get(id);
	}
	public void addVertex(Vertex vertex) {
		m_vertices.add(vertex);
		
		m_vertexMap.put(vertex.getId(), vertex);
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
    	
    	if (m_vertices.size() <= 0) return;
    	
        int index = (int)Math.round(Math.random() * (m_vertices.size() - 2)) + 1;
        
        if (index >= m_vertices.size()) {
        	return;
        }

        Vertex vert = m_vertices.remove(index);
        m_vertexMap.remove(vert.getId());
        
        Iterator<Edge> it = m_edges.iterator();
        while(it.hasNext()) {
            Edge edge = it.next();
            if(edge.getSource() == vert || edge.getTarget() == vert) {
                it.remove();
            }
        }
        
    }
	public String getNextId() {
		return "" + m_counter ++;
	}
    
	
}