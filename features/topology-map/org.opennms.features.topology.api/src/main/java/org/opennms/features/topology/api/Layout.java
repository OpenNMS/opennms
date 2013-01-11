package org.opennms.features.topology.api;

import java.util.Collection;

import org.opennms.features.topology.api.topo.VertexRef;

public interface Layout {
	
	Point getLocation(VertexRef v);
	
	void setLocation(VertexRef vertex, int x, int y);

	void setLocation(VertexRef v, Point location);

	Point getInitialLocation(VertexRef v);
	
	BoundingBox getBounds();

    BoundingBox computeBoundingBox(Collection<VertexRef> vertRefs);
	
}
