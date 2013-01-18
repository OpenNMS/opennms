package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class GraphPainter extends BaseGraphVisitor {

	private final GraphContainer m_graphContainer;
	private final IconRepositoryManager m_iconRepoManager;
	private final PaintTarget m_target;
	private final Layout m_layout;

	GraphPainter(GraphContainer graphContainer, Layout layout, IconRepositoryManager iconRepoManager, PaintTarget target) {
		m_graphContainer = graphContainer;
		m_layout = layout;
		m_iconRepoManager = iconRepoManager;
		m_target = target;
	}
	
	public SelectionManager getSelectionManager() {
		return m_graphContainer.getSelectionManager();
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
		m_target.addAttribute("selected", isSelected(vertex));
		m_target.addAttribute("iconUrl", m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		m_target.addAttribute("label", vertex.getLabel());
		m_target.addAttribute("tooltipText", getTooltipText(vertex));
		m_target.endTag("vertex");
	}

	private String getTooltipText(Vertex vertex) {
		String tooltipText = vertex.getTooltipText();
		tooltipText = tooltipText != null ? tooltipText : vertex.getLabel();
		return tooltipText != null ? tooltipText : "";
	}

	@Override
	public void visitEdge(Edge edge) throws PaintException {
		m_target.startTag("edge");
		m_target.addAttribute("key", edge.getKey());
		m_target.addAttribute("source", getSourceKey(edge));
		m_target.addAttribute("target", getTargetKey(edge));
		m_target.addAttribute("selected", isSelected(edge));
		m_target.addAttribute("cssClass", getStyleName(edge));
		m_target.addAttribute("tooltipText", getTooltipText(edge));
		m_target.endTag("edge");
	}

	private String getTooltipText(Edge edge) {
		String tooltipText = edge.getTooltipText();
		tooltipText = tooltipText != null ? tooltipText : edge.getLabel();
		tooltipText = tooltipText != null ? tooltipText : "";
		return tooltipText;
	}

	@Override
	public void completeGraph(Graph graph) throws PaintException {
		m_target.endTag("graph");
	}

	private String getSourceKey(Edge edge) {
		return m_graphContainer.getVertex(edge.getSource().getVertex()).getKey();
	}

	private String getTargetKey(Edge edge) {
		return m_graphContainer.getVertex(edge.getTarget().getVertex()).getKey();
	}

	private String getStyleName(Edge edge) {
		return isSelected(edge) ? edge.getStyleName()+" selected" : edge.getStyleName();
	}

	private boolean isSelected(Vertex vertex) {
		return getSelectionManager().isVertexRefSelected(vertex);
	}

	private boolean isSelected(Edge edge) {
		return getSelectionManager().isEdgeRefSelected(edge);
	}

}