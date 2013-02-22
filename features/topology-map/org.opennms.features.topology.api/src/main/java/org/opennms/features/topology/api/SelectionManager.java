package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

public interface SelectionManager {
	
	public void deselectAll();

	public void setSelectedVertexRefs(Collection<? extends VertexRef> vertexRefs);

	public void selectVertexRefs(Collection<? extends VertexRef> vertexRefs);

	public void deselectVertexRefs(Collection<? extends VertexRef> vertexRefs);

	public void setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs);

	public boolean isVertexRefSelected(VertexRef vertexRef);

	public boolean isEdgeRefSelected(EdgeRef edgeRef);

	public Collection<VertexRef> getSelectedVertexRefs();

	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);

	void setSelectionListeners(List<SelectionListener> listeners);

}
