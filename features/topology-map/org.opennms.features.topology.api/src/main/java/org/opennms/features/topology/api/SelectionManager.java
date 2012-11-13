package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Set;

public interface SelectionManager {
	
	public interface SelectionListener {
		public void selectionChanged(SelectionManager selectionManager);
	}

    public boolean isVertexSelected(Object vertexId);
    
    public void setVertexSelected(Object vertexId, boolean selected);
    
    public boolean isEdgeSelected(Object edgeId);
    
    public void setEdgeSelected(Object edgeId, boolean selected);

	public void selectVertexAndChildren(Object itemId);

	public void toggleSelectedVertex(Object itemId);

	public void selectVertices(Collection<?> itemIds);

	public Collection<?> getSelectedVertices();

	public void deselectAll();

	public void toggleSelectedEdge(Object edgeId);

	public void selectVerticesAndChildren(Set<Object> value);

	public void setSelectedVertices(Collection<?> vertices);
	
	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);
}
