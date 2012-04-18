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
	private LayoutAlgorithm m_layoutAlgorithm = new SimpleLayoutAlgorithm();

	
	public Graph(){
		setVertices(new ArrayList<Vertex>(Arrays.asList(new Vertex[] {new Vertex(getNextId(),60,25)})));
		m_edges = new ArrayList<Edge>();
		
		for(Vertex vert : getVertices()) {
			m_vertexMap.put(vert.getId(), vert);
		}
		
		updateLayout();
	}
	public Graph(List<Vertex> vertex, List<Edge> edges){
		setVertices(vertex);
		m_edges = edges;
		for(Vertex vert : getVertices()) {
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
		getVertices().add(vertex);
		
		m_vertexMap.put(vertex.getId(), vertex);
		updateLayout();
	}
	public void addEdge(Edge edge) {
		m_edges.add(edge);
	}
	
	public List<Edge> getEdgesForVertex(Vertex vertex){
	    List<Edge> edgeList = new ArrayList<Edge>();
	    Iterator<Edge> it = m_edges.iterator();
        while(it.hasNext()) {
            Edge edge = it.next();
            if(edge.getSource() == vertex || edge.getTarget() == vertex) {
                edgeList.add(edge);
            }
        }
	    return edgeList;
	}
	
	void updateLayout() {
        getLayoutAlgorithm().updateLayout(this);
    }
    public void removeRandomVertext() {
    	if (getVertices().size() <= 0) return;
    	
        int index = (int)Math.round(Math.random() * (getVertices().size() - 2)) + 1;
        
        if (index >= getVertices().size()) {
        	return;
        }

        Vertex vert = getVertices().remove(index);
        m_vertexMap.remove(vert.getId());
        
        removeEdges(vert);
        
    }
    private void removeEdges(Vertex vert) {
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
    public void addVertexTo(Vertex source) {
        Vertex target = new Vertex(getNextId());
        Edge edge = new Edge(source, target);
        addVertex(target);
        addEdge(edge);
    }
    public void removeVertex(Vertex target) {
        if (getVertices().size() <= 0) return;
        
        Iterator<Vertex> it = getVertices().iterator();
        while(it.hasNext()) {
            Vertex vertex = it.next();
            if(vertex == target) {
                it.remove();
                
            }
        }
        removeEdges(target);
    }
    private void setVertices(List<Vertex> vertices) {
        m_vertices = vertices;
    }
    private LayoutAlgorithm getLayoutAlgorithm() {
        return m_layoutAlgorithm;
    }
    private void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
        m_layoutAlgorithm = layoutAlgorithm;
    }
    
	
}