package org.opennms.features.topology.app.internal;

import java.util.Map;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class GraphPainter extends BaseGraphVisitor {

	private final GraphContainer m_graphContainer;
	private final IconRepositoryManager m_iconRepoManager;
	private final PaintTarget m_target;
	private final Layout m_layout;
	private final StatusProvider m_statusProvider;

	GraphPainter(GraphContainer graphContainer, Layout layout, IconRepositoryManager iconRepoManager, PaintTarget target, StatusProvider statusProvider) {
		m_graphContainer = graphContainer;
		m_layout = layout;
		m_iconRepoManager = iconRepoManager;
		m_target = target;
		m_statusProvider = statusProvider;
	}
	
	public StatusProvider getStatusProvider() {
	    return m_statusProvider;
	}

	@Override
	public void visitGraph(Graph graph) throws PaintException {
		m_target.startTag("graph");
	}

	@Override
	public void visitVertex(Vertex vertex) throws PaintException {
		Point initialLocation = m_layout.getInitialLocation(vertex);
		Point location = m_layout.getLocation(vertex);
		m_target.startTag("vertex");
		m_target.addAttribute("key", vertex.getKey());
		m_target.addAttribute("initialX", initialLocation.getX());
		m_target.addAttribute("initialY", initialLocation.getY());
		m_target.addAttribute("x", location.getX());
		m_target.addAttribute("y", location.getY());
		m_target.addAttribute("selected", isSelected(m_graphContainer.getSelectionManager(), vertex));
		if(m_graphContainer.getStatusProvider() != null) {
		    addStatusProviderProperties(m_graphContainer.getStatusProvider(), vertex, m_target);
//		    m_target.addAttribute("status", getStatus(vertex) );
		}

		m_target.addAttribute("iconUrl", m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		m_target.addAttribute("label", vertex.getLabel());
		m_target.addAttribute("tooltipText", getTooltipText(vertex));
		m_target.endTag("vertex");
	}

    private void addStatusProviderProperties(StatusProvider statusProvider, Vertex vertex, PaintTarget target) throws PaintException {
        if (statusProvider.getStatusForVertex(vertex) == null) return;
        Map<String, String> statusProps = statusProvider.getStatusForVertex(vertex).getStatusProperties();
        if (statusProps == null) return;
        for(String key : statusProps.keySet()) {
            target.addAttribute(key, statusProps.get(key));
        }
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
		m_target.startTag("edge");
		m_target.addAttribute("key", edge.getKey());
		m_target.addAttribute("source", getSourceKey(edge));
		m_target.addAttribute("target", getTargetKey(edge));
		m_target.addAttribute("selected", isSelected(m_graphContainer.getSelectionManager(), edge));
		m_target.addAttribute("cssClass", getStyleName(edge));
		m_target.addAttribute("tooltipText", getTooltipText(edge));
		m_target.endTag("edge");
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
		m_target.endTag("graph");
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