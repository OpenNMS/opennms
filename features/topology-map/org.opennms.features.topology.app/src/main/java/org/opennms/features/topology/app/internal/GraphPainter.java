package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class GraphPainter extends BaseGraphVisitor {

	private final GraphContainer m_graphContainer;
	private final IconRepositoryManager m_iconRepoManager;
	private final PaintTarget m_target;

	GraphPainter(GraphContainer graphContainer, IconRepositoryManager iconRepoManager, PaintTarget target) {
		m_graphContainer = graphContainer;
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
	public void completeGraph(TopoGraph graph) throws Exception {
		graph.visit(new BaseGraphVisitor() { 
			@Override
			public void visitVertex(TopoVertex vertex) throws Exception {
				paintParent(vertex);
			}
		});
		
		 m_target.endTag("graph");
	}

	private void paintVertex(TopoVertex vertex) throws PaintException {
		m_target.startTag(vertex.vertexTag());
		m_target.addAttribute("key", vertex.getKey());
		m_target.addAttribute("x", vertex.getX());
		m_target.addAttribute("y", vertex.getY());
		m_target.addAttribute("selected", getSelectionManager().isVertexSelected(vertex.getItemId()));
		m_target.addAttribute("iconUrl", m_iconRepoManager.findIconUrlByKey(vertex.getIconKey()));
		m_target.addAttribute("semanticZoomLevel", vertex.getSemanticZoomLevel());
		m_target.addAttribute("label", vertex.getLabel());
		m_target.addAttribute("tooltipText", vertex.getTooltipText());
		m_target.endTag(vertex.vertexTag());
	}

	private void paintEdge(TopoEdge edge) throws PaintException {
		m_target.startTag("edge");
		m_target.addAttribute("key", edge.getKey());
		m_target.addAttribute("source", edge.getSource().getKey());
		m_target.addAttribute("target", edge.getTarget().getKey());
		m_target.addAttribute("selected", getSelectionManager().isEdgeSelected(edge.getItemId()));
		m_target.addAttribute("cssClass", edge.getCssClass());
		m_target.addAttribute("tooltipText", edge.getTooltipText());
		m_target.endTag("edge");
	}


	private void paintParent(TopoVertex vertex) throws PaintException {
		if (vertex.getGroupId() != null) {
			m_target.startTag(vertex.vertexParentTag());
			m_target.addAttribute("key", vertex.getKey());
			m_target.addAttribute("parentKey", vertex.getGroupKey());
			m_target.endTag(vertex.vertexParentTag());
		}
	}
}