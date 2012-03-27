package org.opennms.features.vaadin.topology.gwt.client;

public class Vertex{
	int m_x;
	int m_y;
	private int m_id;
	
	public Vertex(int id, int x, int y){
		m_id = id;
		m_x = x;
		m_y = y;
	}
	
	public int getX() {
		return m_x;
	};
	
	public int getY(){
		return m_y;
	}
	
	public int getId() {
		return m_id;
	}

    public void setX(int x) {
        m_x = x;
    }

    public void setY(int y) {
        m_y = y;
    }
}