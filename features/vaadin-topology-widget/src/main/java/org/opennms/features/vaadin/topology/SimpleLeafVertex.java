package org.opennms.features.vaadin.topology;

public class SimpleLeafVertex extends SimpleVertex {

	public SimpleLeafVertex(String id, int x, int y) {
		super(id, x, y);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	

}
