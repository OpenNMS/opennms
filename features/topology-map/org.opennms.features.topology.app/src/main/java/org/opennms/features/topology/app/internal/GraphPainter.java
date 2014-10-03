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

import com.vaadin.server.PaintException;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.app.internal.gwt.client.SharedEdge;
import org.opennms.features.topology.app.internal.gwt.client.SharedVertex;
import org.opennms.features.topology.app.internal.gwt.client.TopologyComponentState;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GraphPainter extends BaseGraphVisitor {

	private final GraphContainer m_graphContainer;
	private final IconRepositoryManager m_iconRepoManager;
	private final Layout m_layout;
	private final StatusProvider m_statusProvider;
	private final TopologyComponentState m_componentState;
    private final List<SharedVertex> m_vertices = new ArrayList<SharedVertex>();
    private final List<SharedEdge> m_edges = new ArrayList<SharedEdge>();
    private static final Logger s_log = LoggerFactory.getLogger(VEProviderGraphContainer.class);
    private final Map<VertexRef,Status> m_statusMap = new HashMap<VertexRef, Status>();
    private final Map<EdgeRef,Status> m_edgeStatusMap = new HashMap<EdgeRef, Status>();
    private Set<VertexRef> m_focusVertices = new HashSet<VertexRef>();


    GraphPainter(GraphContainer graphContainer, Layout layout, IconRepositoryManager iconRepoManager, StatusProvider statusProvider, TopologyComponentState componentState) {
		m_graphContainer = graphContainer;
		m_layout = layout;
		m_iconRepoManager = iconRepoManager;
		m_statusProvider = statusProvider;
		m_componentState = componentState;
	}
	
	public StatusProvider getStatusProvider() {
	    return m_statusProvider;
	}

    @Override
    public void visitGraph(Graph graph) throws PaintException {
        m_focusVertices.clear();
        Criteria[] criterias = m_graphContainer.getCriteria();
        for(Criteria criteria : criterias){
            try{
                VertexHopCriteria c = (VertexHopCriteria) criteria;
                m_focusVertices.addAll(c.getVertices());
            }catch(ClassCastException e){}
        }

        if (m_statusProvider != null) {
            Map<VertexRef, Status> newStatusMap = m_statusProvider.getStatusForVertices(m_graphContainer.getBaseTopology(), new ArrayList<VertexRef>(graph.getDisplayVertices()), m_graphContainer.getCriteria());
            if (newStatusMap != null) {
                m_statusMap.clear();
                m_statusMap.putAll(newStatusMap);
            }
        }

        if(m_graphContainer.getEdgeStatusProviders() != null) {
            for (EdgeStatusProvider statusProvider : m_graphContainer.getEdgeStatusProviders()) {
                if (statusProvider.contributesTo(m_graphContainer.getBaseTopology().getEdgeNamespace())) {
                    m_edgeStatusMap.putAll(statusProvider.getStatusForEdges(m_graphContainer.getBaseTopology(),
                            new ArrayList<EdgeRef>(graph.getDisplayEdges()),
                            m_graphContainer.getCriteria()));
                }
            }
        }
    }

    @Override
	public void visitVertex(Vertex vertex) throws PaintException {
		Point initialLocation = m_layout.getInitialLocation(vertex);
		Point location = m_layout.getLocation(vertex);
		SharedVertex v = new SharedVertex();
		v.setKey(vertex.getKey());
        //TODO cast to int for now
		v.setInitialX((int)initialLocation.getX());
		v.setInitialY((int)initialLocation.getY());
		v.setX((int)location.getX());
		v.setY((int)location.getY());
		v.setSelected(isSelected(m_graphContainer.getSelectionManager(), vertex));
        v.setStatus(getStatus(vertex));
        v.setStatusCount(getStatusCount(vertex));
        v.setSVGIconId(m_iconRepoManager.findSVGIconIdByKey(vertex.getIconKey()));
		v.setLabel(vertex.getLabel());
		v.setTooltipText(getTooltipText(vertex));
        v.setStyleName(getVertexStyle(vertex));
		m_vertices.add(v);
	}

    private String getVertexStyle(Vertex vertex) {
        StringBuilder style = new StringBuilder();
        style.append("vertex");
        if(isSelected(m_graphContainer.getSelectionManager(), vertex)){
            style.append(" selected");
        }

        if(m_componentState.isHighlightFocus()) {
            if(!m_focusVertices.contains(vertex)) {
                style.append(" opacity-40");
            }
        }

        return style.toString();

    }

    private String getStatusCount(Vertex vertex) {
        Status status = m_statusMap.get(vertex);
        Map<String, String> statusProperties = status != null ? status.getStatusProperties() : new HashMap<String, String>();
        return statusProperties.get("statusCount") == null ? "" : statusProperties.get("statusCount");
    }

    private String getStatus(Vertex vertex) {
        return m_statusMap.get(vertex) != null ? m_statusMap.get(vertex).computeStatus() : "";
    }

    private static String getTooltipText(Vertex vertex) {
		String tooltipText = vertex.getTooltipText();
		// If the tooltip text is null, use the label
		tooltipText = (tooltipText == null ? vertex.getLabel() : tooltipText);
		// If the label is null, use a blank string
		return (tooltipText == null ? "" : tooltipText);
	}

	@Override
	public void visitEdge(Edge edge) throws PaintException {
		String sourceKey = getSourceKey(edge);
		String targetKey = getTargetKey(edge);
		if (sourceKey == null) {
			s_log.debug("Discarding edge with no source vertex in the base topology: {}", edge);
		} else if (targetKey == null) {
			s_log.debug("Discarding edge with no target vertex in the base topology: {}", edge);
		} else {
			SharedEdge e = new SharedEdge();
			e.setKey(edge.getKey());
			e.setSourceKey(sourceKey);
			e.setTargetKey(targetKey);
			e.setSelected(isSelected(m_graphContainer.getSelectionManager(), edge));
            e.setStatus(getEdgeStatus(edge));

            if(m_componentState.isHighlightFocus()){
                e.setCssClass(getStyleName(edge) + " opacity-50");
            }else{
                e.setCssClass(getStyleName(edge));
            }

			e.setTooltipText(getTooltipText(edge));
			m_edges.add(e);
		}
	}

    private String getEdgeStatus(Edge edge) {
        Status status = m_edgeStatusMap.get(edge);
        return status != null ? status.computeStatus() : "";
    }

    /**
	 * Cannot return null
	 */
	private static String getTooltipText(Edge edge) {
		String tooltipText = edge.getTooltipText();
		// If the tooltip text is null, use the label
		tooltipText = (tooltipText == null ? edge.getLabel() : tooltipText);
		// If the label is null, use a blank string
		return (tooltipText == null ? "" : tooltipText);
	}

	@Override
	public void completeGraph(Graph graph) throws PaintException {
		m_componentState.setVertices(m_vertices);
		m_componentState.setEdges(m_edges);
	}

	private String getSourceKey(Edge edge) {
		return m_graphContainer.getBaseTopology().getVertex(edge.getSource().getVertex(), m_graphContainer.getCriteria()).getKey();
	}

	private String getTargetKey(Edge edge) {
		return m_graphContainer.getBaseTopology().getVertex(edge.getTarget().getVertex(), m_graphContainer.getCriteria()).getKey();
	}

	/**
	 * Cannot return null
	 */
	private String getStyleName(Edge edge) {
		String styleName = edge.getStyleName();
		// If the style is null, use a blank string
		styleName = (styleName == null ? "" : styleName);
        String status = " " + getEdgeStatus(edge);
        return isSelected(m_graphContainer.getSelectionManager(), edge) ? styleName + " selected" + status : styleName + status;
	}

	private static boolean isSelected(SelectionManager selectionManager, Vertex vertex) {
		return selectionManager.isVertexRefSelected(vertex);
	}

	private static boolean isSelected(SelectionManager selectionManager, Edge edge) {
		return selectionManager.isEdgeRefSelected(edge);
	}

}