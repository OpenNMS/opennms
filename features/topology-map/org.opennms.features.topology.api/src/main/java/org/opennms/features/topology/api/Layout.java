package org.opennms.features.topology.api;

import org.opennms.features.topology.api.topo.VertexRef;

public interface Layout {
	
	int getVertexX(VertexRef v);

	int getVertexY(VertexRef v);

	void setVertexX(VertexRef v, int x);

	void setVertexY(VertexRef v, int y);

}
