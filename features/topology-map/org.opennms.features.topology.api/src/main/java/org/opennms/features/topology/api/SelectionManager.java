package org.opennms.features.topology.api;

import java.util.Collection;

public interface SelectionManager {
	
	public interface SelectionListener {
		public void selectionChanged(SelectionManager selectionManager);
	}

	public void deselectAll();

	public void setSelectedVertices(Collection<?> vertexIds);
	
	public void selectVertices(Collection<?> vertexIds);

	public void setSelectedEdges(Collection<?> edgeIds);

	public boolean isVertexSelected(Object vertexId);
	
	public boolean isEdgeSelected(Object edgeId);
	
	public Collection<?> getSelectedVertices();

	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);

}
