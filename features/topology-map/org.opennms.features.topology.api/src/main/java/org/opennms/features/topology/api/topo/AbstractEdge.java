package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public abstract class AbstractEdge implements Edge {

	private final String m_namespace;
	private final String m_id;
	private final BeanItem<Edge> m_item;
	
	public AbstractEdge(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
		m_item = new BeanItem<Edge>(this);
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getKey() {
		return m_namespace + ":" + m_id;
	}

	@Override
	public Item getItem() {
		return m_item;
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
