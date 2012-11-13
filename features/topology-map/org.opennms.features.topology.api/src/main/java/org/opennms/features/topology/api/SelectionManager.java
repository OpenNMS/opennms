package org.opennms.features.topology.api;

import java.util.Collection;

public interface SelectionManager {

	void deselectAll();

	boolean isVertexSelected(Object itemId);

	public void setVertexSelected(Object vertexId, boolean selected);
    
    public boolean isEdgeSelected(Object edgeId);
    
    public void setEdgeSelected(Object edgeId, boolean selected);
    
	public void toggleSelectForVertexAndChildren(Object itemId);

	public void toggleSelectedVertex(Object itemId);

	public void selectVertices(Collection<?> itemIds);

	public Collection<?> getSelectedVertices();

	public void toggleSelectedEdge(Object edgeItemId);
}
