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

package org.opennms.features.topology.api.topo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

public class AbstractVertex implements Vertex {

	// Required
	private String m_namespace;
	// Required
	private String m_id;

	private String m_label;
	private String m_tooltipText;
	private String m_iconKey;
	private String m_styleName;
	private AbstractVertex m_parent;
	protected Item m_item;
	int m_x;
	int m_y;
	private boolean m_selected;
	private boolean m_locked = false;
	private String m_ipAddr ="127.0.0.1";
	private int m_nodeID = -1;
	private int m_semanticZoomLevel = -1;

	/**
	 * No-arg constructor for JAXB.
	 */
	public AbstractVertex() {}

	public AbstractVertex(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
	}

	/**
	 * This JAXB function is used to set the namespace since we expect it to be set in the parent object.
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (m_namespace == null) {
			try {
				BeanInfo info = Introspector.getBeanInfo(parent.getClass());
				for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
					if ("namespace".equals(descriptor.getName())) {
						m_namespace = (String)descriptor.getReadMethod().invoke(parent);
						LoggerFactory.getLogger(this.getClass()).debug("Setting namespace on {} to {} from parent", this, m_namespace);
					}
				}
			} catch (IntrospectionException e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
			} catch (IllegalArgumentException e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
			} catch (IllegalAccessException e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
			} catch (InvocationTargetException e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
			}
		}
	}

	@Override
	@XmlID
	public final String getId() {
		return m_id;
	}

	/**
	 * This setter is private so that it can only be used by JAXB.
	 */
	@SuppressWarnings("unused")
	private final void setId(String id) {
		m_id = id;
	}

	@Override
	@XmlTransient
	public final String getNamespace() {
		return m_namespace;
	}

	@Override
	public final String getKey() {
		return m_namespace+":"+m_id;
	}

	@Override
	public Item getItem() {
		return m_item;
	}

	@Override
	public final String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText != null ? m_tooltipText : getLabel();
	}

	public final void setTooltipText(String tooltpText) {
		m_tooltipText = tooltpText;
	}

	@Override
	public final String getIconKey() {
		return m_iconKey;
	}

	public final void setIconKey(String iconKey) {
		m_iconKey = iconKey;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public final int getX() {
		return m_x;
	}

	public final void setX(int x) {
		m_x = x;
	}

	@Override
	public final int getY() {
		return m_y;
	}

	public final void setY(int y) {
		m_y = y;
	}

	@XmlIDREF
	public final AbstractVertex getParent() {
		return m_parent;
	}

	/**
	 * @param parent
	 */
	@Override
	public final void setParent(Vertex parent) {
		if (parent == null) {
			m_parent = null;
		} else if (parent instanceof AbstractVertex) {
			m_parent = (AbstractVertex)parent;
		} else {
			LoggerFactory.getLogger(this.getClass()).warn("Could not set parent to instance of {}", parent.getClass().getName());
		}
	}

	@Override
	public final boolean isLocked() {
		return m_locked;
	}

	public final void setLocked(boolean locked) {
		m_locked = locked;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public boolean isRoot() {
		return getParent() == null;
	}

	@Override
	public final boolean isSelected() {
		return m_selected;
	}

	public final void setSelected(boolean selected) {
		m_selected = selected;
	}

	@Override
	public final String getIpAddress() {
		return m_ipAddr;
	}

	public final void setIpAddress(String ipAddr){
		m_ipAddr = ipAddr;
	}

	@Override
	public final int getNodeID() {
		return m_nodeID;
	}

	public final void setNodeID(int nodeID) {
		m_nodeID = nodeID;
	}

	@Override
	public final int getSemanticZoomLevel() {
		return m_semanticZoomLevel >= 0
		? m_semanticZoomLevel
				: getParent() == null 
				? 0 
						: getParent().getSemanticZoomLevel() + 1;
	}

	@Override
	public Vertex getDisplayVertex(int semanticZoomLevel) {
		if(getParent() == null || getSemanticZoomLevel() <= semanticZoomLevel) {
			return this;
		}else {
			return getParent().getDisplayVertex(semanticZoomLevel);
		}

	}

	@Override
	public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		 result = prime * result
		 + ((m_namespace == null) ? 0 : m_namespace.hashCode());
		 return result;
	 }

	 @Override
	 public boolean equals(Object obj) {
		 if (this == obj) return true;
		 if (obj == null) return false;
		 if (!(obj instanceof VertexRef))	return false;

		 VertexRef e = (VertexRef)obj;
		 return getNamespace().equals(e.getNamespace()) && getId().equals(e.getId());

	 }

	 @Override
	 public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
}
