package org.opennms.features.topology.plugins.topo.adapter.internal;

import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleEdgeBuilder {
	
	SimpleEdgeProvider m_edgeProvider;
	SimpleEdge m_currentEdge;
	
	public SimpleEdgeBuilder(String namespace) {
		this(new SimpleEdgeProvider(namespace));
	}
	
	public SimpleEdgeBuilder(SimpleEdgeProvider edgeProvider) {
		m_edgeProvider = edgeProvider;
	}
	
	public SimpleEdgeBuilder edge(String id, String srcNs, String srcId, String tgtNs, String tgtId) {
		
		VertexRef srcVertex = new SimpleVertexRef(srcNs, srcId);
		VertexRef tgtVertex = new SimpleVertexRef(tgtNs, tgtId);
		
		SimpleConnector source = new SimpleConnector(ns(), srcId+"-"+id+"-connector", srcVertex);
		SimpleConnector target = new SimpleConnector(ns(), tgtId+"-"+id+"-connector", tgtVertex);
		
		m_currentEdge = new SimpleEdge(ns(), id, source, target);
		
		source.setEdge(m_currentEdge);
		target.setEdge(m_currentEdge);
		
		m_edgeProvider.add(m_currentEdge);
		
		return this;
	}
	
	public SimpleEdgeBuilder label(String label) {
		m_currentEdge.setLabel(label);
		return this;
	}
	
	public SimpleEdgeBuilder tooltip(String tooltipText) {
		m_currentEdge.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleEdgeBuilder styleName(String styleName) {
		m_currentEdge.setStyleName(styleName);
		return this;
	}
	
	public SimpleEdgeProvider get() {
		return m_edgeProvider;
	}

	private String ns() {
		return m_edgeProvider.getNamespace();
	}


}
