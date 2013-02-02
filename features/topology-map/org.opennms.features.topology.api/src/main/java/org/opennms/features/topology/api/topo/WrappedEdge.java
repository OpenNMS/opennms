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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

@XmlRootElement(name="edge")
@XmlAccessorType(XmlAccessType.FIELD)
public class WrappedEdge {

	public String key;
	public String label;
	public String tooltipText;
	@XmlIDREF
	public WrappedVertex source;
	@XmlIDREF
	public WrappedVertex target;
	@XmlID
	public String id;
	@XmlTransient
	public String namespace;

	/**
	 * No-arg constructor for JAXB.
	 */
	public WrappedEdge() {}

	public WrappedEdge(Edge edge, WrappedVertex source, WrappedVertex target) {
		key = edge.getKey();
		label = edge.getLabel();
		tooltipText = edge.getTooltipText();
		id = edge.getId();
		namespace = edge.getNamespace();

		this.source = source;
		this.target = target;
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
	public String toString() { return "WrappedEdge:"+namespace+":"+id+ "[label="+label+"]"; } 
}
