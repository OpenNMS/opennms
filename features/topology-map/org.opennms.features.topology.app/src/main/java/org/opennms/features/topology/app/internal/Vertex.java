package org.opennms.features.topology.app.internal;

import com.google.gwt.user.client.Window;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class Vertex implements Paintable {
    
    public static final String LEAF_PROPERTY = "leaf";
    public static final String X_PROPERTY = "x";
    public static final String Y_PROPERTY = "y";
    public static final String LABEL_PROPERTY = "label";
    public static final String SELECTED_PROPERTY = "selected";
    public static final String ICON_PROPERTY = "icon";
    public static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";
    private static final Object IP_ADDRESS_PROPERTY = "ipAddr";
	private String m_key;
	private Object m_itemId;
	private Item m_item;
	private Object m_groupId;
	private String m_groupKey;
	private String m_label;
	private String m_ipAddr;
	
	public Vertex(String key, Object itemId, Item item, String groupKey, Object groupId) {
		m_key = key;
		m_itemId = itemId;
		m_item = item;
		m_groupKey = groupKey;
		m_groupId = groupId;
	}
	
	public Object getItemId() {
		return m_itemId;
	}
	
	public String getGroupKey() {
		return m_groupKey;
	}
	
	public Object getGroupId() {
		return m_groupId;
	}
	
	public boolean isLeaf() {
		Object value = m_item.getItemProperty(LEAF_PROPERTY).getValue();
        return (Boolean) value;
	}
	
	public int getX() {
		return (Integer) m_item.getItemProperty(X_PROPERTY).getValue();
		
	};
	
	public int getY(){
		return (Integer) m_item.getItemProperty(Y_PROPERTY).getValue();
	}
	
	public void setX(int x) {
		m_item.getItemProperty(X_PROPERTY).setValue(x);
    }

    public void setY(int y) {
    	m_item.getItemProperty(Y_PROPERTY).setValue(y);
    }
    
    public String toString() {
    	return "v" + getItemId() + "(" + getX()  + "," + getY() + "):" + (isSelected() ? "selected" : "unselected");
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        Window.alert("getting Update from Server");
    }

	public void setSelected(boolean selected) {
		m_item.getItemProperty(SELECTED_PROPERTY).setValue(selected);
	}
	
	public boolean isSelected() {
		return (Boolean) m_item.getItemProperty(SELECTED_PROPERTY).getValue();
	}

	public Object getItem() {
		return m_item;
	}
    
    public String getIconUrl() {
        return (String) m_item.getItemProperty(ICON_PROPERTY).getValue();
    }

	public String getKey() {
		return m_key;
	}
	
	public int getSemanticZoomLevel() {
		Integer zoomLevel = (Integer) m_item.getItemProperty(SEMANTIC_ZOOM_LEVEL).getValue();
        return zoomLevel;
	}
	
	public String getLabel() {
		Property labelProperty = m_item.getItemProperty(LABEL_PROPERTY);
		String label = labelProperty == null ? "no such label" : (String)labelProperty.getValue();
		return label;
	}
	
	public String getIpAddr() {
	    Property ipAddrProperty = m_item.getItemProperty(IP_ADDRESS_PROPERTY);
	    String ipAddr = ipAddrProperty == null ? "127.0.0.1" : (String)ipAddrProperty.getValue();
	    return ipAddr;
	}

	public void setGroupId(Object groupId) {
		m_groupId = groupId;
	}

	public void setGroupKey(String groupKey) {
		m_groupKey = groupKey;
	}

}