package org.opennms.features.topology.plugins.devutils.internal;

import javax.xml.bind.annotation.XmlRootElement;

import com.vaadin.data.Item;

@XmlRootElement(name="vertex")
public class WrappedLeafVertex extends WrappedVertex {

	public WrappedLeafVertex() {}
	
	public WrappedLeafVertex(Item vertex) {
		super(vertex);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	

}
