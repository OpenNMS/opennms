package org.opennms.features.topology.api;

import java.util.Collection;

public interface SelectionManager {
	
	public interface SelectionListener {
		public void selectionChanged(SelectionManager selectionManager);
	}

	public void deselectAll();

	public void setSelectedVertices(Collection<?> vertices);
	
	public void selectVertices(Collection<?> itemIds);

	public void setSelectedEdges(Collection<?> singleton);

	public boolean isVertexSelected(Object itemId);
	
	public boolean isEdgeSelected(Object itemId);
	
	public Collection<?> getSelectedVertices();

	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);

}
