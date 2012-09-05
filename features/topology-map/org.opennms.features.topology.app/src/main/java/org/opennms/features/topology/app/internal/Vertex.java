/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    private static final Object NODE_ID = "nodeID";
    private static final String ICON_KEY = "iconKey";
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
	
	public int getNodeID() {
		Property nodeIDProperty = m_item.getItemProperty(NODE_ID);
		int nodeID = nodeIDProperty == null ? -1 : (Integer)nodeIDProperty.getValue();
		return nodeID;
	}

	public void setGroupId(Object groupId) {
		m_groupId = groupId;
	}

	public void setGroupKey(String groupKey) {
		m_groupKey = groupKey;
	}

    public String getIconKey() {
        return (String) m_item.getItemProperty(ICON_KEY).getValue();
    }

}