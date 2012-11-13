package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.SelectionManager;

public class DefaultSelectionManager implements SelectionManager {

	private final SimpleGraphContainer m_simpleGraphContainer;
	private final Set<SelectionListener> m_listeners = new CopyOnWriteArraySet<SelectionListener>();

	public DefaultSelectionManager(SimpleGraphContainer simpleGraphContainer) {
		m_simpleGraphContainer = simpleGraphContainer;
	}

	@Override
	public boolean isVertexSelected(Object itemId) {
		return m_simpleGraphContainer.isVertexSelected(itemId);
	}

	@Override
	public void setVertexSelected(Object itemId, boolean selected) {
		m_simpleGraphContainer.setVertexSelected(itemId, selected);
	}

	@Override
	public boolean isEdgeSelected(Object edgeId) {
		return m_simpleGraphContainer.isEdgeSelected(edgeId);
	}

	@Override
	public void setEdgeSelected(Object edgeId, boolean selected) {
		m_simpleGraphContainer.setEdgeSelected(edgeId, selected);
	}

	@Override
	public List<Object> getSelectedVertices() {
		List<Object> selectedVertices = new ArrayList<Object>();
		
		for(Object itemId : m_simpleGraphContainer.getVertexIds()) {
			if (isVertexSelected(itemId)) {
				selectedVertices.add(itemId);
			}
		}
		
		return selectedVertices;
	}

	@Override
	public void selectVertexAndChildren(Object itemId) {
		selectVertexAndChildren(itemId, new HashSet<Object>());
		
	}

	private void selectVertexAndChildren(Object itemId, Set<Object> selected) {
		setVertexSelected(itemId, true);
		selected.add(itemId);

		if(m_simpleGraphContainer.hasChildren(itemId)) {
		    Collection<?> children = m_simpleGraphContainer.getChildren(itemId);
		    for( Object childId : children) {
		    	if (!selected.contains(childId)) {
		    		selectVertexAndChildren(childId, selected);
		    	}
		    }
		}
	}

	@Override
	public void toggleSelectedVertex(Object itemId) {
		boolean selected = isVertexSelected(itemId);
		setVertexSelected(itemId, !selected);
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
		for(Object vertexId : m_simpleGraphContainer.getVertexIds()) {
			setVertexSelected(vertexId, false);
		}
		
		for(Object edgeId : m_simpleGraphContainer.getEdgeIds()) {
			setEdgeSelected(edgeId, false);
		}
	}

	@Override
	public void toggleSelectedEdge(Object edgeId) {
		boolean selected = isEdgeSelected(edgeId);
		setEdgeSelected(edgeId, !selected);
	}

	@Override
	public void selectVerticesAndChildren(Set<Object> itemIds) {
		Set<Object> selected = new HashSet<Object>();

		for(Object itemId : itemIds) {
			selectVertexAndChildren(itemId, selected);
		}
	}
	
	@Override
	public void setSelectedVertices(Collection<?> vertexIds) {
		doDeselectAll();
		
		selectVertices(vertexIds);
	}
	
	void fireSelectionChanged() {
		for(SelectionListener listener : m_listeners) {
			listener.selectionChanged(this);
		}
	}
	
	@Override
	public void addSelectionListener(SelectionListener listener) {
		m_listeners.add(listener);
	}
	
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_listeners.remove(listener);
	}

	@Override
	public void setSelectedEdges(Collection<?> edgeIds) {
		doDeselectAll();
		
		for(Object edgeId : edgeIds) {
			setEdgeSelected(edgeId, true);
		}
		
		fireSelectionChanged();
	}

}
