/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleEdgeBuilder {
	
	SimpleEdgeProvider m_edgeProvider;
	AbstractEdge m_currentEdge;
	
	public SimpleEdgeBuilder(String namespace, String contributesTo) {
		this(new SimpleEdgeProvider(namespace, contributesTo));
	}
	
	public SimpleEdgeBuilder(String namespace) {
		this(new SimpleEdgeProvider(namespace));
	}
	
	public SimpleEdgeBuilder(SimpleEdgeProvider edgeProvider) {
		m_edgeProvider = edgeProvider;
	}
	
	public SimpleEdgeBuilder edge(String id, String srcNs, String srcId, String tgtNs, String tgtId) {
		
		VertexRef srcVertex = new DefaultVertexRef(srcNs, srcId);
		VertexRef tgtVertex = new DefaultVertexRef(tgtNs, tgtId);
		
		SimpleConnector source = new SimpleConnector(ns(), srcId+"-"+id+"-connector", srcVertex);
		SimpleConnector target = new SimpleConnector(ns(), tgtId+"-"+id+"-connector", tgtVertex);
		
		m_currentEdge = new AbstractEdge(ns(), id, source, target);
		
		source.setEdge(m_currentEdge);
		target.setEdge(m_currentEdge);
		
		m_edgeProvider.add(m_currentEdge);
		
		return this;
	}
	
	public SimpleEdgeBuilder label(String label) {
		m_currentEdge.setLabel(label);
		return this;
	}
	
	public SimpleEdgeBuilder tooltip(String tooltipText) {
		m_currentEdge.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleEdgeBuilder styleName(String styleName) {
		m_currentEdge.setStyleName(styleName);
		return this;
	}
	
	public SimpleEdgeProvider get() {
		return m_edgeProvider;
	}

	private String ns() {
		return m_edgeProvider.getNamespace();
	}


}
