package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public class DefaultSelectionManager implements SelectionManager {
	private final Set<VertexRef> m_selectedVertices = new HashSet<VertexRef>();
	private final Set<EdgeRef> m_selectedEdges = new HashSet<EdgeRef>();
	private List<SelectionListener> m_listeners = new CopyOnWriteArrayList<SelectionListener>();
	private final Set<SelectionListener> m_addedListeners = new CopyOnWriteArraySet<SelectionListener>();

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
	    int selectionSize = getSelectedVertexRefs().size();
	    for(VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, true);
		}
	    int selectionSizeAfterRemoval = getSelectedVertexRefs().size();
	    
	    if(selectionSize != selectionSizeAfterRemoval) {
            fireSelectionChanged();
        }
	}

	@Override
	public void deselectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		int selectionSize = getSelectedVertexRefs().size();
	    for(VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, false);
		}
		int selectionSizeAfterRemoval = getSelectedVertexRefs().size();
		
		if(selectionSize != selectionSizeAfterRemoval) {
		    fireSelectionChanged();
		}
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
		m_addedListeners.add(listener);
	}
	
	@Override
	public void setSelectionListeners(List<SelectionListener> listeners) {
		m_addedListeners.clear();
		m_listeners = listeners;
	}
	
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		// Remove the listener from either the added list or the set collection
		if (!m_addedListeners.remove(listener)) {
			m_listeners.remove(listener);
		}
	}

	protected void fireSelectionChanged() {
		for(SelectionListener listener : m_listeners) {
			LoggerFactory.getLogger(this.getClass()).debug("Invoking selectionChanged() on {}", listener);
			listener.selectionChanged(this);
		}
		for(SelectionListener listener : m_addedListeners) {
			LoggerFactory.getLogger(this.getClass()).debug("Invoking selectionChanged() on {}", listener);
			listener.selectionChanged(this);
		}
	}

}
