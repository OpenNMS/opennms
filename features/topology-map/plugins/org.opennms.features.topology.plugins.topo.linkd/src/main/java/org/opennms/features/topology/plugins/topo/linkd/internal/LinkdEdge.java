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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.SimpleConnector;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

@XmlRootElement(name="edge")
public class LinkdEdge extends AbstractEdge {
	String m_tooltipText;
	Connector m_source;
	Connector m_target;

	public LinkdEdge(String id, Vertex source, Vertex target) {
		super("linkd", id);
		setSource(new SimpleConnector("linkd", source.getId() + "::" + target.getId(), source, this));
		setTarget(new SimpleConnector("linkd", target.getId() + "::" + source.getId(), target, this));
	}

	@Override
	public Item getItem() {
		return new BeanItem<LinkdEdge>(this);
	}

	@Override
	@XmlIDREF
	public Connector getSource() {
		return m_source;
	}

	public void setSource(Connector source) {
		m_source = source;
	}

	@Override
	@XmlIDREF
	public Connector getTarget() {
		return m_target;
	}

	public void setTarget(Connector target) {
		m_target = target;
	}
}
