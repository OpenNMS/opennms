package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class DefaultSelectionManager implements SelectionManager {
	private final Set<VertexRef> m_selectedVertices = new HashSet<VertexRef>();
	private final Set<EdgeRef> m_selectedEdges = new HashSet<EdgeRef>();
	private final Set<SelectionListener> m_listeners = new CopyOnWriteArraySet<SelectionListener>();

	public DefaultSelectionManager() {
	}

	@Override
	public boolean isVertexRefSelected(VertexRef vertexRef) {
		return m_selectedVertices.contains(vertexRef);
	}

	private void setVertexRefSelected(VertexRef ref, boolean selected) {
		if (selected) {
			m_selectedVertices.add(ref);
		} else {
			m_selectedVertices.remove(ref);
		}
	}

	@Override
	public boolean isEdgeRefSelected(EdgeRef edgeRef) {
		return m_selectedEdges.contains(edgeRef);
	}

	private void setEdgeRefSelected(EdgeRef edgeRef, boolean selected) {
		if (selected) {
			m_selectedEdges.add(edgeRef);
		} else {
			m_selectedEdges.remove(edgeRef);
		}
	}

	@Override
	public Collection<VertexRef> getSelectedVertexRefs() {
		return Collections.unmodifiableSet(m_selectedVertices);
	}
	
	@Override
	public void selectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		for(VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, true);
		}
		
		fireSelectionChanged();
	}

	@Override
	public void deselectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		for(VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, false);
		}
		
		fireSelectionChanged();
	}

	@Override
	public void deselectAll() {
		int vertSelectionSize = m_selectedVertices.size();
		int edgeSelectionSize = m_selectedEdges.size();
		
	    doDeselectAll();
		
	    if(vertSelectionSize > m_selectedVertices.size() || edgeSelectionSize > m_selectedEdges.size()) {
	        fireSelectionChanged();
	    }
	}

	private void doDeselectAll() {
		m_selectedEdges.clear();
		m_selectedVertices.clear();
	}

	@Override
	public void setSelectedVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		doDeselectAll();
		
		selectVertexRefs(vertexRefs);
	}

	@Override
	public void setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs) {
		doDeselectAll();
		
		for(EdgeRef edgeRef : edgeRefs) {
			setEdgeRefSelected(edgeRef, true);
		}
		
		fireSelectionChanged();
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		m_listeners.add(listener);
	}
	
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_listeners.remove(listener);
	}

	void fireSelectionChanged() {
		for(SelectionListener listener : m_listeners) {
			listener.selectionChanged(this);
		}
	}

}
