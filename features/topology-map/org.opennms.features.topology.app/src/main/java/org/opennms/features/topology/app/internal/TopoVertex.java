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

import java.util.Collections;

import org.opennms.features.topology.api.GraphContainer;

import com.google.gwt.user.client.Window;
import com.vaadin.data.Item;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class TopoVertex implements Paintable {
    
    public static final String LEAF_PROPERTY = "leaf";
    public static final String X_PROPERTY = "x";
    public static final String Y_PROPERTY = "y";
    public static final String LABEL_PROPERTY = "label";
    public static final String SELECTED_PROPERTY = "selected";
    public static final String ICON_PROPERTY = "icon";
    public static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";
    static final String ICON_KEY = "iconKey";
	private String m_key;
	private Object m_itemId;
	private Object m_groupId;
	private String m_groupKey;
	private SimpleGraphContainer m_graphContainer;
	
	public TopoVertex(GraphContainer graphContainer, String key, Object itemId, String groupKey, Object groupId) {
		m_graphContainer = (SimpleGraphContainer) graphContainer;
		m_key = key;
		setItemId(itemId);
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
		return getGraphContainer().getGroupId(getItemId());
	}
	
	public boolean isLeaf() {
		return getGraphContainer().isLeaf(getItemId());
	}

	public int getX() {
		return getGraphContainer().getX(getItemId());
		
	}

	public int getY(){
		return getGraphContainer().getY(getItemId());
	}

	public void setX(int x) {
		getGraphContainer().setX(getItemId(), x);
    }

	public void setY(int y) {
    	getGraphContainer().setY(getItemId(), y);
    }

	public String toString() {
    	return "v" + getItemId() + "(" + getX()  + "," + getY() + "):" + (isSelected() ? "selected" : "unselected");
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        Window.alert("getting Update from Server");
    }

	public void setSelected(boolean selected) {
		getGraphContainer().getSelectionManager().selectVertices(Collections.singleton(getItemId()));
	}

	public boolean isSelected() {
		return getGraphContainer().getSelectionManager().isVertexSelected(getItemId());
	}

	public Item getItem() {
		return getGraphContainer().getVertexItem(getItemId());
	}
	
	public String getKey() {
		return m_key;
	}
	
	public int getSemanticZoomLevel() {
		return getGraphContainer().getSemanticZoomLevel(getItemId());
	}

	public String getLabel() {
		return getGraphContainer().getLabel(getItemId());
	}

	public void setGroupId(Object groupId) {
		m_groupId = groupId;
	}

	public void setGroupKey(String groupKey) {
		m_groupKey = groupKey;
	}

    public String getIconKey() {
        return getGraphContainer().getIconKey(getItemId());
    }

	public String getTooltipText() {
        return getGraphContainer().getTooltipText(getItemId());
    }

	String vertexTag() {
		return isLeaf() ? "vertex" : "group";
	}

	String vertexParentTag() {
		return isLeaf() ? "vertexParent" : "groupParent";
	}

	public SimpleGraphContainer getGraphContainer() {
		return m_graphContainer;
	}

	private void setItemId(Object itemId) {
		m_itemId = itemId;
	}

	public void visit(GraphVisitor visitor) throws Exception {
		visitor.visitVertex(this);
		visitor.completeVertex(this);
	}

}