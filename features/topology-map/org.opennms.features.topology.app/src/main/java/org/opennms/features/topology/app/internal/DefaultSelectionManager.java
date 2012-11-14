package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.SelectionManager;

public class DefaultSelectionManager implements SelectionManager {

	private final Set<Object> m_selectedVertices = new HashSet<Object>();
	private final Set<Object> m_selectedEdges = new HashSet<Object>();
	private final Set<SelectionListener> m_listeners = new CopyOnWriteArraySet<SelectionListener>();

	public DefaultSelectionManager() {
	}

	@Override
	public boolean isVertexSelected(Object itemId) {
		return m_selectedVertices.contains(itemId);
	}

	private void setVertexSelected(Object itemId, boolean selected) {
		if (selected) {
			m_selectedVertices.add(itemId);
		} else {
			m_selectedVertices.remove(itemId);
		}
			
	}

	@Override
	public boolean isEdgeSelected(Object edgeId) {
		return m_selectedEdges.contains(edgeId);
	}

	public void setEdgeSelected(Object edgeId, boolean selected) {
		if (selected) {
			m_selectedEdges.add(edgeId);
		} else {
			m_selectedEdges.remove(edgeId);
		}
	}

	@Override
	public List<Object> getSelectedVertices() {
		return new ArrayList<Object>(m_selectedVertices);
	}

	@Override
	public void selectVertices(Collection<?> itemIds) {
		for(Object itemId : itemIds) {
			setVertexSelected(itemId, true);
		}
		
		fireSelectionChanged();
	}

	@Override
	public void deselectAll() {
		doDeselectAll();
		
		fireSelectionChanged();
	}

	private void doDeselectAll() {
		m_selectedEdges.clear();
		m_selectedVertices.clear();
	}

	@Override
	public void setSelectedVertices(Collection<?> vertexIds) {
		doDeselectAll();
		
		selectVertices(vertexIds);
	}
	
	@Override
	public void setSelectedEdges(Collection<?> edgeIds) {
		doDeselectAll();
		
		for(Object edgeId : edgeIds) {
			setEdgeSelected(edgeId, true);
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
