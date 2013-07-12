package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.gwt.client.SharedEdge;
import org.opennms.features.topology.app.internal.gwt.client.SharedVertex;
import org.opennms.features.topology.app.internal.gwt.client.TopologyComponentState;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.server.PaintException;

public class GraphPainter extends BaseGraphVisitor {

	private final GraphContainer m_graphContainer;
	private final IconRepositoryManager m_iconRepoManager;
	private final Layout m_layout;
	private final StatusProvider m_statusProvider;
	private final TopologyComponentState m_componentState;
    private final List<SharedVertex> m_vertices = new ArrayList<SharedVertex>();
    private final List<SharedEdge> m_edges = new ArrayList<SharedEdge>();

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
	public void visitVertex(Vertex vertex) throws PaintException {
		Point initialLocation = m_layout.getInitialLocation(vertex);
		Point location = m_layout.getLocation(vertex);
		SharedVertex v = new SharedVertex();
		v.setKey(vertex.getKey());
		v.setInitialX(initialLocation.getX());
		v.setInitialY(initialLocation.getY());
		v.setX(location.getX());
		v.setY(location.getY());
		v.setSelected(isSelected(m_graphContainer.getSelectionManager(), vertex));
		if(m_graphContainer.getStatusProvider().getNamespace() != null) {
            //TODO: This assumes Alarm status need to provide a better api
            v.setStatus(getStatus(vertex));
            v.setStatusCount(getStatusCount(vertex));
        }
        v.setIconUrl(m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		v.setLabel(vertex.getLabel());
		v.setTooltipText(getTooltipText(vertex));
		m_vertices.add(v);
	}

    private String getStatusCount(Vertex vertex) {
        Map<String,String> statusProperties = m_graphContainer.getStatusProvider().getStatusForVertex(vertex).getStatusProperties();
        return statusProperties.get("statusCount") == null ? "" : statusProperties.get("statusCount");
    }

    private String getStatus(Vertex vertex) {
        return m_statusProvider != null && m_statusProvider.getStatusForVertex(vertex) != null ? m_statusProvider.getStatusForVertex(vertex).computeStatus() : "";
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
		SharedEdge e = new SharedEdge();
		e.setKey(edge.getKey());
		e.setSourceKey(getSourceKey(edge));
		e.setTargetKey(getTargetKey(edge));
		e.setSelected(isSelected(m_graphContainer.getSelectionManager(), edge));
		e.setCssClass(getStyleName(edge));
		e.setTooltipText(getTooltipText(edge));
		m_edges.add(e);
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
		return m_graphContainer.getBaseTopology().getVertex(edge.getSource().getVertex()).getKey();
	}

	private String getTargetKey(Edge edge) {
		return m_graphContainer.getBaseTopology().getVertex(edge.getTarget().getVertex()).getKey();
	}

	/**
	 * Cannot return null
	 */
	private String getStyleName(Edge edge) {
		String styleName = edge.getStyleName();
		// If the style is null, use a blank string
		styleName = (styleName == null ? "" : styleName);

		return isSelected(m_graphContainer.getSelectionManager(), edge) ? styleName + " selected" : styleName;
	}

	private static boolean isSelected(SelectionManager selectionManager, Vertex vertex) {
		return selectionManager.isVertexRefSelected(vertex);
	}

	private static boolean isSelected(SelectionManager selectionManager, Edge edge) {
		return selectionManager.isEdgeRefSelected(edge);
	}

}