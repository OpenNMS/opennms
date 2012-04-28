package org.opennms.features.vaadin.topology;

import com.vaadin.data.Item;

public class Edge{
	private String m_key;
	private Object m_itemId;
	private Item m_item;
	private Vertex m_source;
	private Vertex m_target;

	public Edge(String key, Object itemId, Item item, Vertex source, Vertex target) {; 
		m_key = key;
		m_itemId = itemId;
		m_item = item;
		m_source = source;
		m_target = target;
	}

	public Vertex getSource(){
		return m_source;
	}
	
	public String getKey() {
		return m_key;
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
	
	public Object getItemId() {
		return m_itemId;
	}
	
	public Vertex getTarget(){
		return m_target;
	}
	
	@Override
	public String toString() {
	    return "Edge :: source: " + getSource() + " target: " + getTarget();
	}

	public Object getItem() {
		return m_item;
	}
}