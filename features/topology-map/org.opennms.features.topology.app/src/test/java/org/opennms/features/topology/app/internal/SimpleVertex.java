package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.topo.AbstractVertex;

import com.vaadin.data.util.BeanItem;

public class SimpleVertex extends AbstractVertex {

	public SimpleVertex(String namespace, String id) {
		super(namespace, id);
		m_item = new BeanItem<SimpleVertex>(this);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 

}
