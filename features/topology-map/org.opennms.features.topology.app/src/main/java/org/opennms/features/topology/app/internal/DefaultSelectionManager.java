package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.SelectionManager;

public class DefaultSelectionManager implements SelectionManager {

	private final SimpleGraphContainer m_simpleGraphContainer;

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
	public void toggleSelectForVertexAndChildren(Object itemId) {
		toggleSelectedVertex(itemId);

		if(m_simpleGraphContainer.hasChildren(itemId)) {
		    Collection<?> children = m_simpleGraphContainer.getChildren(itemId);
		    for( Object childId : children) {
		        setVertexSelected(childId, true);
		    }
		}
		
		// TODO: this is a selection change
		m_simpleGraphContainer.fireChange();
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
	}

	@Override
	public void deselectAll() {
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

}
