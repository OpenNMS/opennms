package org.opennms.features.topology.api;

import org.opennms.features.topology.api.topo.VertexRef;

public interface Layout {
	
	int getX(Object vertexId);

	void setX(Object vertexId, int x);

	int getY(Object vertexId);

	void setY(Object vertexId, int y);

}
