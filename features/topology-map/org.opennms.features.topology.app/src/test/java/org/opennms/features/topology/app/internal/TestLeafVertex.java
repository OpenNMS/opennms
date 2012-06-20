package org.opennms.features.topology.app.internal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="vertex")
public class TestLeafVertex extends TestVertex {

	public TestLeafVertex() {}
	
	public TestLeafVertex(String id, int x, int y) {
		super(id, x, y);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	

}
