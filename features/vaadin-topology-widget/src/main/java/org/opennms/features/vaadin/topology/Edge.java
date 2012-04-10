package org.opennms.features.vaadin.topology;


public class Edge{
	Vertex m_source;
	Vertex m_target;
	
	public Edge(Vertex source, Vertex target){
		m_source = source;
		m_target = target;
	}
	
	public Vertex getSource(){
		return m_source;
	}
	
	public int getX1(){
		return getSource().getX();
	}
	
	public int getX2(){
		return getTarget().getX();
	}
	
	public int getY1(){
		return getSource().getY();
	}
	
	public int getY2(){
		return getTarget().getY();
	}
	
	public String getId() {
	    return getSource().getId() + ":" + getTarget().getId();
	}
	
	public Vertex getTarget(){
		return m_target;
	}
	
	@Override
	public String toString() {
	    return "Edge :: source: " + getSource() + " target: " + getTarget();
	}
}