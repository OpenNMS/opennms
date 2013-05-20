package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.DefaultSelectionContext;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public class DefaultSelectionManager implements SelectionManager {
	private Set<SelectionListener> m_listeners = new CopyOnWriteArraySet<SelectionListener>();
	private final Set<SelectionListener> m_addedListeners = new CopyOnWriteArraySet<SelectionListener>();
	private final SelectionContext m_context = new DefaultSelectionContext();

	@Override
	public boolean deselectAll() {
		boolean retval = m_context.deselectAll();
		if (retval) {
			selectionChanged(m_context);
		}
		return retval;
	}

	@Override
	public boolean deselectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		boolean retval = m_context.deselectVertexRefs(vertexRefs);
		if (retval) {
			selectionChanged(m_context);
		}
		return retval;
	}

	@Override
	public Collection<VertexRef> getSelectedVertexRefs() {
		return m_context.getSelectedVertexRefs();
	}

	@Override
	public boolean isEdgeRefSelected(EdgeRef edgeRef) {
		return m_context.isEdgeRefSelected(edgeRef);
	}

	@Override
	public boolean isVertexRefSelected(VertexRef vertexRef) {
		return m_context.isVertexRefSelected(vertexRef);
	}

	@Override
	public boolean selectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		boolean retval = m_context.selectVertexRefs(vertexRefs);
		if (retval) {
			selectionChanged(m_context);
		}
		return retval;
	}

	@Override
	public boolean setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs) {
		boolean retval = m_context.setSelectedEdgeRefs(edgeRefs);
		if (retval) {
			selectionChanged(m_context);
		}
		return retval;
	}

	@Override
	public boolean setSelectedVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		boolean retval = m_context.setSelectedVertexRefs(vertexRefs);
		if (retval) {
			selectionChanged(m_context);
		}
		return retval;
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		if (listener != null) {
			m_addedListeners.add(listener);
		}
	}
	
	@Override
	public void setSelectionListeners(Set<SelectionListener> listeners) {
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

	@Override
	public void selectionChanged(SelectionContext selectionContext) {
		for(SelectionListener listener : m_listeners) {
			LoggerFactory.getLogger(this.getClass()).debug("Invoking selectionChanged() on: {}, {}", listener.getClass().getName(), listener);
			listener.selectionChanged(selectionContext);
		}
		for(SelectionListener listener : m_addedListeners) {
			LoggerFactory.getLogger(this.getClass()).debug("Invoking selectionChanged() on: {}, {}", listener.getClass().getName(), listener);
			listener.selectionChanged(selectionContext);
		}
	}
}
