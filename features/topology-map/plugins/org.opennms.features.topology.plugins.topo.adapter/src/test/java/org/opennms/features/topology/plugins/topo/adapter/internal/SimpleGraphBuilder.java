package org.opennms.features.topology.plugins.topo.adapter.internal;

import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleGraphBuilder {
	
	
	private final SimpleGraphProvider m_graphProvider;
	private SimpleVertex m_currentVertex;
	private SimpleEdge m_currentEdge;
	
	public SimpleGraphBuilder(String namespace) {
		m_graphProvider = new SimpleGraphProvider(namespace);
	}
	
	public SimpleGraphBuilder vertex(String id) {
		m_currentVertex = new SimpleVertex(ns(), id);
		m_graphProvider.add(m_currentVertex);
		return this;
	}
	
	public SimpleGraphBuilder vLabel(String label) {
		m_currentVertex.setLabel(label);
		return this;
	}
	
	public SimpleGraphBuilder vTooltip(String tooltipText) {
		m_currentVertex.setTooltpText(tooltipText);
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
	
	public SimpleGraphBuilder edge(String id, String srcId, String tgtId) {
		
		VertexRef srcVertex = m_graphProvider.getVertex(srcId);
		if (srcVertex == null) {
			srcVertex = new SimpleVertexRef(ns(), srcId);
		}
		
		VertexRef tgtVertex = m_graphProvider.getVertex(tgtId);
		if (tgtVertex == null) {
			tgtVertex = new SimpleVertexRef(ns(), tgtId);
		}
		
		
		SimpleConnector source = new SimpleConnector(ns(), srcId+"-"+id+"-connector", srcVertex);
		SimpleConnector target = new SimpleConnector(ns(), tgtId+"-"+id+"-connector", tgtVertex);
		
		m_currentEdge = new SimpleEdge(ns(), id, source, target);
		
		source.setEdge(m_currentEdge);
		target.setEdge(m_currentEdge);
		
		m_graphProvider.add(m_currentEdge);
		
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
	
	public SimpleGraphProvider get() {
		return m_graphProvider;
	}

	private String ns() {
		return m_graphProvider.getNamespace();
	}
	
}
