/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.DefaultSelectionContext;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
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
		// Before committing change, ensure that all selected vertices are shown
		// If not, add the missing vertex to focus
		if (!selectionContext.getSelectedVertexRefs().isEmpty()) {
			final Collection<Vertex> currentlyVisibleVertices = getGraphContainer().getGraph().getDisplayVertices();
			boolean fireGraphChanged = false;
			for (VertexRef eachSelectedVertex : getSelectedVertexRefs()) {
				if (!currentlyVisibleVertices.contains(eachSelectedVertex)) {
					final DefaultVertexHopCriteria focusCriteria = new DefaultVertexHopCriteria(eachSelectedVertex);
					fireGraphChanged = true;
					getGraphContainer().addCriteria(focusCriteria);
				}
			}
			// Only fire event if we actually changed the container (by adding criteria)
			if (fireGraphChanged) {
				getGraphContainer().fireGraphChanged();
			}
		}

		// Now notify
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
