package org.opennms.features.topology.plugins.topo.adapter.internal;

import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Edge;

public class SimpleEdge implements Edge {
	
	private final String m_namespace;
	private final String m_id;
	private final Connector m_source;
	private final Connector m_target;
	
	private String m_label;
	private String m_tooltipText;
	private String m_styleName;
	

	public SimpleEdge(String namespace, String id, Connector source, Connector target) {
		m_namespace = namespace;
		m_id = id;
		m_source = source;
		m_target = target;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public Connector getSource() {
		return m_source;
	}

	@Override
	public Connector getTarget() {
		return m_target;
	}

	@Override
	public String getLabel() {
		return m_label;
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public void setTooltipText(String tooltipText) {
		m_tooltipText = tooltipText;
	}

	public void setStyleName(String styleName) {
		m_styleName = styleName;
	}

}
