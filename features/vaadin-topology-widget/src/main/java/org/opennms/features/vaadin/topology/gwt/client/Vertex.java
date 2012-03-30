package org.opennms.features.vaadin.topology.gwt.client;

import com.google.gwt.user.client.Window;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class Vertex implements Paintable{
	int m_x;
	int m_y;
	private int m_id;
	
	public Vertex() {};
	
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
    
    public String toString() {
    	return "v" + m_id + "(" + m_x  + "," + m_y + ")";
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        Window.alert("getting Update from Server");
    }
}