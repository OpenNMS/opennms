package org.opennms.features.topology.plugins.topo.adapter.internal;

public class SimpleVertexBuilder {
	
	SimpleVertexProvider m_vertexProvider;
	SimpleVertex m_currentVertex;
	
	public SimpleVertexBuilder(String namespace) {
		this(new SimpleVertexProvider(namespace));
	}
	
	public SimpleVertexBuilder(SimpleVertexProvider vertexProvider) {
		m_vertexProvider = vertexProvider;
	}
	
	public SimpleVertexBuilder vertex(String id) {
		m_currentVertex = new SimpleVertex(ns(), id);
		m_vertexProvider.add(m_currentVertex);
		return this;
	}
	
	public SimpleVertexBuilder vLabel(String label) {
		m_currentVertex.setLabel(label);
		return this;
	}
	
	public SimpleVertexBuilder vTooltip(String tooltipText) {
		m_currentVertex.setTooltpText(tooltipText);
		return this;
	}
	
	public SimpleVertexBuilder vIconKey(String iconKey) {
		m_currentVertex.setIconKey(iconKey);
		return this;
	}
	
	public SimpleVertexBuilder vStyleName(String styleName) {
		m_currentVertex.setStyleName(styleName);
		return this;
	}
	
	public String ns() {
		return m_vertexProvider.getNamespace();
	}
	


}
