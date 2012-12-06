package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class SimpleEdge extends AbstractEdge {
	
	private final Connector m_source;
	private final Connector m_target;
	
	private String m_label;
	private String m_tooltipText;
	private String m_styleName;
	

	public SimpleEdge(String namespace, String id, Connector source, Connector target) {
		super(namespace, id);
		m_source = source;
		m_target = target;
	}
	
	@Override
	public Item getItem() {
		return new BeanItem<SimpleEdge>(this);
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

	@Override
	public String toString() { return "Edge:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
}
