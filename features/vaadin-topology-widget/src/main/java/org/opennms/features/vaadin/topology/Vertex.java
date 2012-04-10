package org.opennms.features.vaadin.topology;

import java.util.Date;

import com.google.gwt.user.client.Window;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class Vertex implements Paintable{
	int m_x;
	int m_y;
	private String m_id;
	private boolean m_selected;
	private Date m_date = new Date();
	
	public Vertex(String id) {
		m_id = id;
	}
	
	public Vertex(String id, int x, int y){
		this(id);
		m_x = x;
		m_y = y;
	}
	
	public int getX() {
		return m_x;
	};
	
	public int getY(){
		return m_y;
	}
	
	public String getId() {
		return m_id;
	}

    public void setX(int x) {
        m_x = x;
    }

    public void setY(int y) {
        m_y = y;
    }
    
    public String toString() {
    	return "v" + m_id + "(" + m_x  + "," + m_y + "):" + (m_selected ? "selected" : "unselected") + "ts:" + m_date.getTime();
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        Window.alert("getting Update from Server");
    }

	public void setSelected(boolean selected) {
		m_selected = selected;
	}
	
	public boolean isSelected() {
		return m_selected;
	}

	public Object getItem() {
		// TODO HACK FOR NOW!!! FIX THIS
		return this;
	}
}