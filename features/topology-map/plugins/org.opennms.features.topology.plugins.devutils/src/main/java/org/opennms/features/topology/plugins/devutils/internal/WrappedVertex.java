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

package org.opennms.features.topology.plugins.devutils.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.data.Item;
import com.vaadin.data.Property;


public abstract class WrappedVertex implements Vertex {
	
	public static WrappedVertex create(Item vertex) {
		Property leaf = vertex.getItemProperty("leaf");
		boolean isLeaf = leaf == null ? true : ((Boolean)leaf.getValue()).booleanValue();
		
		return isLeaf ? new WrappedLeafVertex(vertex) : new WrappedGroup(vertex);
	}

	Item m_vertex;
	WrappedGroup m_parent = null;
	List<WrappedEdge> m_edges = new ArrayList<WrappedEdge>();
	
	public WrappedVertex() {}
	
	public WrappedVertex(Item vertex) {
		m_vertex = vertex;
	}

	@XmlIDREF
	public WrappedGroup getParent() {
		return m_parent;
	}
	
	public void setParent(WrappedGroup parent) {
		if (m_parent != null) {
			m_parent.removeMember(this);
		}
		m_parent = parent;
		if (m_parent != null) {
			m_parent.addMember(this);
		}
	}
	
	protected final Object getProperty(String propertyId) {
		Property property = m_vertex.getItemProperty(propertyId);
		return property == null ? null : property.getValue();
	}
	
	protected final void setProperty(String propertyId, Object value) {
		Property property = m_vertex.getItemProperty(propertyId);
		if (property != null) {
			property.setValue(value);
		}
	}
	
	@Override
	public final boolean isLocked() {
		return (Boolean) getProperty("locked");
	}

	public final void setLocked(Boolean locked) {
		setProperty("locked", locked);
	}

	@Override
	public abstract boolean isLeaf();
	
	@Override
	public final boolean isRoot() {
		return m_parent == null;
	}

	@Override
	public final Item getItem() {
		return m_vertex;
	}

	@XmlID
	@Override
	public final String getId() {
		return (String) getProperty("id");
	}

	public final void setId(String id) {
		setProperty("id", id);
	}

	@Override
	public final int getX() {
		return (Integer) getProperty("x");
	}

	public final void setX(Integer x) {
		setProperty("x", x);
	}

	@Override
	public final int getY() {
		return (Integer) getProperty("y");
	}

	public final void setY(Integer y) {
		setProperty("y", y);
	}

	@Override
	public final boolean isSelected() {
		return (Boolean) getProperty("selected");
	}

	public final void setSelected(Boolean selected) {
		setProperty("selected", selected);
	}

	@Override
	public final String getLabel() {
		return (String) getProperty("label");
	}

	public final void setLabel(String label) {
		setProperty("label", label);
	}
	
	@Override
	public final String getIpAddress() {
		return (String) getProperty("ipAddr");
	}
	
	public final void setIpAddress(String ipAddr){
		setProperty("ipAddr", ipAddr);
	}
	
	@Override
	public final int getNodeID() {
		return (Integer) getProperty("nodeID");
	}
	
	public final void setNodeID(int nodeID) {
		setProperty("nodeID", nodeID);
	}

	@Override
	public final Vertex getDisplayVertex(int semanticZoomLevel) {
		throw new UnsupportedOperationException(this.getClass().getName() + ".getDisplayVertex() not implemented");
	}

	@Override
	public final String getKey() {
		return (String) getProperty("key");
	}

	@Override
	public final int getSemanticZoomLevel() {
		return (Integer) getProperty("semanticZoomLevel");
	}

	@Override
	public final String getStyleName() {
		return (String) getProperty("styleName");
	}

	@Override
	public final String getTooltipText() {
		return (String) getProperty("tooltipText");
	}

	@Override
	public final void setParent(Vertex parent) {
		setProperty("tooltipText", parent);
	}

	@Override
	public final String getNamespace() {
		return (String) getProperty("namespace");
	}

	@XmlTransient
    public final List<WrappedEdge> getEdges() {
		return m_edges;
	}
	
	final void addEdge(WrappedEdge edge) {
		m_edges.add(edge);
	}
	
	final void removeEdge(WrappedEdge edge) {
		m_edges.remove(edge);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WrappedVertex other = (WrappedVertex) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
    public String getIconKey() {
        return (String) getProperty("iconKey");
    }

    public void setIconKey(String iconKey) {
    	setProperty("iconKey", iconKey);
    }
    
//	public int getSemanticZoomLevel() {
//		return m_semanticZoomLevel >= 0
//				? m_semanticZoomLevel
//				: m_parent == null 
//				? 0 
//				: m_parent.getSemanticZoomLevel() + 1;
//	}
	
//	public WrappedVertex getDisplayVertex(int semanticZoomLevel) {
//		if(getParent() == null || getSemanticZoomLevel() <= semanticZoomLevel) {
//			return this;
//		}else {
//			return getParent().getDisplayVertex(semanticZoomLevel);
//		}
//
//	}
	
	
}
