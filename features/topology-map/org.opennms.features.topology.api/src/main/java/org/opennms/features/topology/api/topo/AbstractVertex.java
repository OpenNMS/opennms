package org.opennms.features.topology.api.topo;

import javax.xml.bind.annotation.XmlID;

import com.vaadin.data.Item;

public abstract class AbstractVertex implements Vertex {
	
	private final String m_namespace;
	private final String m_id;
	private String m_label;
	private String m_tooltipText;
	private String m_iconKey;
	private String m_styleName;
	protected Item m_item;

	public AbstractVertex(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
	}

	@Override
	@XmlID
	public final String getId() {
		return m_id;
	}

	public final void setId(String id) {
		throw new UnsupportedOperationException("Property id is not writable");
	}

	@Override
	public final String getNamespace() {
		return m_namespace;
	}

	@Override
	public final String getKey() {
		return m_namespace+":"+m_id;
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
		return m_tooltipText != null ? m_tooltipText : getLabel();
	}

	public final void setTooltipText(String tooltpText) {
		m_tooltipText = tooltpText;
	}

	@Override
	public String getIconKey() {
		return m_iconKey;
	}

	public final void setIconKey(String iconKey) {
		m_iconKey = iconKey;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		result = prime * result
				+ ((m_namespace == null) ? 0 : m_namespace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof VertexRef))	return false;
		
		VertexRef e = (VertexRef)obj;
		return getNamespace().equals(e.getNamespace()) && getId().equals(e.getId());
					
	}

	@Override
	public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
}
