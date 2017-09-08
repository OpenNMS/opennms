/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.DefaultSelectionContext;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public class DefaultSelectionManager implements SelectionManager {
	private Set<SelectionListener> m_listeners = new CopyOnWriteArraySet<>();
	private final Set<SelectionListener> m_addedListeners = new CopyOnWriteArraySet<>();
	private final SelectionContext m_context;

	public DefaultSelectionManager(GraphContainer graphContainer) {
		m_context = new DefaultSelectionContext(graphContainer);
	}

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
	public Collection<EdgeRef> getSelectedEdgeRefs() {
		return m_context.getSelectedEdgeRefs();
	}

	@Override
	public GraphContainer getGraphContainer() {
		return m_context.getGraphContainer();
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
