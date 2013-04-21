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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.FIELD)
public class WrappedVertex implements VertexRef {
	
	public String iconKey;
	@XmlID
	public String id;
	public String ipAddr;
	public String label;
	@XmlTransient
	public String namespace;
	public Integer nodeID;
	public String styleName;
	public String tooltipText;
	public Integer x;
	public Integer y;
	@XmlTransient
	public boolean group;
	public boolean locked;
	public boolean selected;
	@XmlIDREF
	public WrappedVertex parent;
	
	public static WrappedVertex create(Vertex vertex) {
		return (vertex.isGroup() ? new WrappedGroup(vertex) : new WrappedLeafVertex(vertex));
	}

	/**
	 * No-arg constructor for JAXB.
	 */
	public WrappedVertex() {}

	protected WrappedVertex(VertexRef vertex) {
		if (vertex.getId() == null) {
			throw new IllegalArgumentException("Vertex has null ID: " + vertex);
		} else if (vertex.getNamespace() == null) {
			throw new IllegalArgumentException("Vertex has null namespace: " + vertex);
		}
		id = vertex.getId();
		label = vertex.getLabel();
		namespace = vertex.getNamespace();
	}

	protected WrappedVertex(Vertex vertex) {
		this((VertexRef)vertex);
		iconKey = vertex.getIconKey();
		ipAddr = vertex.getIpAddress();
		nodeID = vertex.getNodeID();
		if (vertex.getParent() != null) parent = new WrappedVertex(vertex.getParent());
		styleName = vertex.getStyleName();
		tooltipText = vertex.getTooltipText();
		x = vertex.getX();
		y = vertex.getY();
		group = vertex.isGroup();
		locked = vertex.isLocked();
		selected = vertex.isSelected();
	}

	/**
	 * This JAXB function is used to set the namespace since we expect it to be set in the parent object.
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (namespace == null) {
			try {
				BeanInfo info = Introspector.getBeanInfo(parent.getClass());
				for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
					if ("namespace".equals(descriptor.getName())) {
						namespace = (String)descriptor.getReadMethod().invoke(parent);
						LoggerFactory.getLogger(this.getClass()).debug("Setting namespace on {} to {} from parent", this, namespace);
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
	public String getId() {
		return id;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result
				+ ((getNamespace() == null) ? 0 : getNamespace().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (!(obj instanceof VertexRef)) return false;

		VertexRef ref = (VertexRef)obj;
		
		return getNamespace().equals(ref.getNamespace()) && getId().equals(ref.getId());

	}

	@Override
	public int compareTo(Ref o) {
		if (this.equals(o)) {
			return 0;
		} else {
			// Order by namespace, then ID
			if (this.getNamespace().equals(o.getNamespace())) {
				if (this.getId().equals(o.getId())) {
					// Shouldn't happen because equals() should return true
					throw new IllegalStateException("equals() was inaccurate in " + this.getClass().getName());
				} else {
					return this.getId().compareTo(o.getId());
				}
			} else {
				return this.getNamespace().compareTo(o.getNamespace());
			}
		}
	}

	@Override
	public String toString() { return "WrappedVertex:"+namespace+":"+id+ "[label="+label+", styleName="+styleName+"]"; } 
}
