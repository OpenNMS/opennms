package org.opennms.features.topology.api.topo;

import javax.xml.bind.annotation.XmlID;

public abstract class AbstractEdge implements Edge {

	private final String m_namespace;
	private final String m_id;
	private String m_label;
	private String m_tooltipText;
	private String m_styleName;

	public AbstractEdge(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
	}

	@Override
	@XmlID
	public final String getId() {
		return m_id;
	}

	@Override
	public final String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getKey() {
		return m_namespace + ":" + m_id;
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

	public final void setLabel(String label) {
		m_label = label;
	}

	public final void setTooltipText(String tooltipText) {
		m_tooltipText = tooltipText;
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
		if (!(obj instanceof EdgeRef))	return false;
		
		EdgeRef e = (EdgeRef)obj;
		return getNamespace().equals(e.getNamespace()) && getId().equals(e.getId());
					
	}
	
	@Override
	public String toString() { return "Edge:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 

}
