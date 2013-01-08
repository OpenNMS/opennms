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

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

@XmlRootElement(name="edge")
public class WrappedEdge extends AbstractEdge {
	Item m_edge;
	Connector m_source;
	Connector m_target;

	public WrappedEdge() {}

	public WrappedEdge(Item edge, WrappedVertex source, WrappedVertex target) {
		super((String)edge.getItemProperty("namespace").getValue(), (String)edge.getItemProperty("id").getValue(), source, target);
		m_edge = edge;
	}

	private Object getProperty(String propertyId) {
		Property property = m_edge.getItemProperty(propertyId);
		return property == null ? null : property.getValue();
	}

	@Override
	public Item getItem() {
		return m_edge;
	}

	@Override
	public String getKey() {
		return (String) getProperty("key");
	}

	@Override
	public String getLabel() {
		return (String) getProperty("label");
	}

	@Override
	public String getStyleName() {
		return (String) getProperty("styleName");
	}

	@Override
	public String getTooltipText() {
		return (String) getProperty("tooltipText");
	}
}
