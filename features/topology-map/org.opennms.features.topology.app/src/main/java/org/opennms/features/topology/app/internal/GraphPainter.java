package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;

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
		m_target.startTag("vertex");
		m_target.addAttribute("key", vertex.getKey());
		m_target.addAttribute("initialX", getInitialX(vertex));
		m_target.addAttribute("initialY", getInitialY(vertex));
		m_target.addAttribute("x", getX(vertex));
		m_target.addAttribute("y", getY(vertex));
		m_target.addAttribute("selected", isSelected(vertex));
		m_target.addAttribute("iconUrl", m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		m_target.addAttribute("label", vertex.getLabel());
		m_target.addAttribute("tooltipText", vertex.getTooltipText());
		m_target.endTag("vertex");
	}

	@Override
	public void visitEdge(Edge edge) throws PaintException {
		m_target.startTag("edge");
		m_target.addAttribute("key", edge.getKey());
		m_target.addAttribute("source", getSourceKey(edge));
		m_target.addAttribute("target", getTargetKey(edge));
		m_target.addAttribute("selected", isSelected(edge));
		m_target.addAttribute("cssClass", getStyleName(edge));
		m_target.addAttribute("tooltipText", edge.getTooltipText());
		m_target.endTag("edge");
	}

	@Override
	public void completeGraph(Graph graph) throws PaintException {
		m_target.endTag("graph");
	}

	private int getInitialX(Vertex vertex) {
		Vertex parent = m_graphContainer.getParent(vertex);
		return parent == null ? (int)(Math.random()*1000) : m_graphContainer.getVertexX(parent);
	}

	private int getInitialY(Vertex vertex) {
		Vertex parent = m_graphContainer.getParent(vertex);
		return parent == null ? (int)(Math.random()*1000) : m_graphContainer.getVertexY(parent);
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
		return getSelectionManager().isVertexSelected(vertex.getItemId());
	}

	private boolean isSelected(Edge edge) {
		return getSelectionManager().isEdgeSelected(edge.getItemId());
	}


	private int getX(Vertex vertex) {
		return m_layout.getVertexX(vertex);
	}

	private int getY(Vertex vertex) {
		return m_layout.getVertexY(vertex);
	}

}