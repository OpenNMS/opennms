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

package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.SimpleGraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleGraphBuilder {

	private final GraphProvider m_graphProvider;
	private AbstractVertex m_currentVertex;
	private AbstractEdge m_currentEdge;
	
	public SimpleGraphBuilder(String namespace) {
		m_graphProvider = new SimpleGraphProvider(namespace);
	}
	
	public SimpleGraphBuilder vertex(String id) {
		m_currentVertex = new AbstractVertex(ns(), id);
		m_graphProvider.addVertices(m_currentVertex);
		return this;
	}
	
	public SimpleGraphBuilder parent(String parentId) {
		Vertex parent = m_graphProvider.getVertex(ns(), parentId);
		m_graphProvider.setParent(m_currentVertex, parent);
		return this;
	}
	
	public SimpleGraphBuilder vLabel(String label) {
		m_currentVertex.setLabel(label);
		return this;
	}
	
	public SimpleGraphBuilder vTooltip(String tooltipText) {
		m_currentVertex.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleGraphBuilder vIconKey(String iconKey) {
		m_currentVertex.setIconKey(iconKey);
		return this;
	}
	
	public SimpleGraphBuilder vStyleName(String styleName) {
		m_currentVertex.setStyleName(styleName);
		return this;
	}

	public SimpleGraphBuilder vX(int x) {
		m_currentVertex.setX(x);
		return this;
	}

	public SimpleGraphBuilder vY(int y) {
		m_currentVertex.setY(y);
		return this;
	}
	
	public SimpleGraphBuilder edge(String id, String srcId, String tgtId) {
		
		VertexRef srcVertex = m_graphProvider.getVertex(ns(), srcId);
		if (srcVertex == null) {
			srcVertex = new DefaultVertexRef(ns(), srcId);
		}
		
		VertexRef tgtVertex = m_graphProvider.getVertex(ns(), tgtId);
		if (tgtVertex == null) {
			tgtVertex = new DefaultVertexRef(ns(), tgtId);
		}
		
		
		SimpleConnector source = new SimpleConnector(ns(), srcId+"-"+id+"-connector", srcVertex);
		SimpleConnector target = new SimpleConnector(ns(), tgtId+"-"+id+"-connector", tgtVertex);
		
		m_currentEdge = new AbstractEdge(ns(), id, source, target);
		
		source.setEdge(m_currentEdge);
		target.setEdge(m_currentEdge);
		
		m_graphProvider.addEdges(m_currentEdge);
		
		return this;
	}
	
	public SimpleGraphBuilder eLabel(String label) {
		m_currentEdge.setLabel(label);
		return this;
	}
	
	public SimpleGraphBuilder eTooltip(String tooltipText) {
		m_currentEdge.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleGraphBuilder eStyleName(String styleName) {
		m_currentEdge.setStyleName(styleName);
		return this;
	}
	
	public GraphProvider get() {
		return m_graphProvider;
	}

	private String ns() {
		return m_graphProvider.getNamespace();
	}
	
}
