package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.SelectionManager;
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
	public void visitGraph(TopoGraph graph) throws Exception {
		m_target.startTag("graph");
	}

	@Override
	public void visitVertex(TopoVertex vertex) throws Exception {
		paintVertex(vertex);
	}

	@Override
	public void visitEdge(TopoEdge edge) throws Exception {
		paintEdge(edge);
	}

	@Override
	public void completeVertex(TopoVertex vertex) throws Exception {
		paintParent(vertex);
	}

	@Override
	public void completeGraph(TopoGraph graph) throws Exception {
		m_target.endTag("graph");
	}

	private void paintVertex(TopoVertex vertex) throws PaintException {
		m_target.startTag(getVertexTag(vertex));
		m_target.addAttribute("key", getKey(vertex));
		m_target.addAttribute("x", getX(vertex));
		m_target.addAttribute("y", getY(vertex));
		m_target.addAttribute("selected", isSelected(vertex));
		m_target.addAttribute("iconUrl", m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		m_target.addAttribute("semanticZoomLevel", getSemanticZoomLevel(vertex));
		m_target.addAttribute("label", vertex.getLabel());
		m_target.addAttribute("tooltipText", vertex.getTooltipText());
		m_target.endTag(getVertexTag(vertex));
	}

	private int getSemanticZoomLevel(TopoVertex vertex) {
		return vertex.getSemanticZoomLevel();
	}

	private void paintEdge(TopoEdge edge) throws PaintException {
		m_target.startTag("edge");
		m_target.addAttribute("key", getKey(edge));
		m_target.addAttribute("source", getSourceKey(edge));
		m_target.addAttribute("target", getTargetKey(edge));
		m_target.addAttribute("selected", isSelected(edge));
		m_target.addAttribute("cssClass", getStyleName(edge));
		m_target.addAttribute("tooltipText", edge.getTooltipText());
		m_target.endTag("edge");
	}

	private String getKey(TopoEdge edge) {
		return edge.getKey();
	}

	private String getTargetKey(TopoEdge edge) {
		return edge.getTarget().getKey();
	}

	private String getSourceKey(TopoEdge edge) {
		return edge.getSource().getKey();
	}

	private void paintParent(TopoVertex vertex) throws PaintException {
		if (vertex.getGroupId() != null) {
			m_target.startTag(getVertexParentTag(vertex));
			m_target.addAttribute("key", getKey(vertex));
			m_target.addAttribute("parentKey", getParentKey(vertex));
			m_target.endTag(getVertexParentTag(vertex));
		}
	}

	private String getParentKey(TopoVertex vertex) {
		return vertex.getGroupKey();
	}

	private String getKey(TopoVertex vertex) {
		return vertex.getKey();
	}

	private String getStyleName(TopoEdge edge) {
		return isSelected(edge) ? edge.getStyleName()+" selected" : edge.getStyleName();
	}

	private boolean isSelected(TopoVertex vertex) {
		return getSelectionManager().isVertexSelected(getVertexId(vertex));
	}

	private boolean isSelected(TopoEdge edge) {
		return getSelectionManager().isEdgeSelected(edge.getItemId());
	}


	private String getVertexTag(TopoVertex vertex) {
		return m_graphContainer.hasChildren(getVertexId(vertex)) ? "group" : "vertex";
	}

	private int getX(TopoVertex vertex) {
		return m_layout.getX(getVertexId(vertex));
	}

	private int getY(TopoVertex vertex) {
		return m_layout.getY(getVertexId(vertex));
	}

	private String getVertexParentTag(TopoVertex vertex) {
		return m_graphContainer.hasChildren(getVertexId(vertex)) ? "groupParent" : "vertexParent";
	}

	private Object getVertexId(TopoVertex vertex) {
		return vertex.getItemId();
	}

}