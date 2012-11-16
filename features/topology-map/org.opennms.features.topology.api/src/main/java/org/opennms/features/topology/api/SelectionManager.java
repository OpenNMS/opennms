package org.opennms.features.topology.api;

import java.util.Collection;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

public interface SelectionManager {
	
	public interface SelectionListener {
		public void selectionChanged(SelectionManager selectionManager);
	}

	public void deselectAll();

	public void setSelectedVertices(Collection<?> vertexIds);
	
	//public void setSelectedVertexRefs(Collection<? extends VertexRef> vertexRefs);

	public void selectVertices(Collection<?> vertexIds);

	public void deselectVertices(Collection<?> vertexIds);

	//public void selectVertexRefs(Collection<? extends VertexRef> vertexRefs);

	public void setSelectedEdges(Collection<?> edgeIds);

	//public void setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs);

	public boolean isVertexSelected(Object vertexId);

	//public boolean isVertexRefSelected(VertexRef vertexRef);

	public boolean isEdgeSelected(Object edgeId);
	
	//public boolean isEdgeRefSelected(EdgeRef edgeRef);

	public Collection<?> getSelectedVertices();
	
	//public Collection<? extends VertexRef> getSelectedVertexRefs();

	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);


}
