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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;

public class LinkdVertex extends AbstractVertex {
	List<Edge> m_edges = new ArrayList<Edge>();
	
	/**
	 * Only used for unit tests.
	 */
	LinkdVertex() {
		super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, "fakeId");
	}

	public LinkdVertex(String id, String iconKey, String label, String ipAddr) {
		super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id);
	    setIconKey(iconKey);
	    setLabel(label);
	    setIpAddress(ipAddr);
	}
	
	public LinkdVertex(String id, int x, int y, String iconKey, String label, String ipAddr) {
		super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id);
		setIconKey(iconKey);
		setLabel(label);
		setIpAddress(ipAddr);
		setX(x);
		setY(y);
	}

	@XmlTransient
	public List<Edge> getEdges() {
		return m_edges;
	}
	
	public void addEdge(LinkdEdge edge) {
		m_edges.add(edge);
	}
	
	public void removeEdge(LinkdEdge edge) {
		m_edges.remove(edge);
	}
}
