package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.topo.AbstractVertex;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class SimpleVertex extends AbstractVertex {
	
	private String m_label;
	private String m_tooltpText;
	private String m_iconKey;
	private String m_styleName;
	private Item m_item;

	public SimpleVertex(String namespace, String id) {
		super(namespace, id);
		m_item = new BeanItem<SimpleVertex>(this);
	}
	
	@Override
	public Item getItem() {
		return m_item;
	}

	@Override
	public String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	@Override
	public String getTooltipText() {
		return m_tooltpText;
	}

	public void setTooltipText(String tooltpText) {
		m_tooltpText = tooltpText;
	}

	@Override
	public String getIconKey() {
		return m_iconKey;
	}

	public void setIconKey(String iconKey) {
		m_iconKey = iconKey;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public void setStyleName(String styleName) {
		m_styleName = styleName;
	}
	
	@Override
	public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 

}
