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
package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class DefaultSelectionContext implements SelectionContext {
	private final Set<VertexRef> m_selectedVertices = Collections.synchronizedSet(new HashSet<VertexRef>());
	private final Set<EdgeRef> m_selectedEdges = Collections.synchronizedSet(new HashSet<EdgeRef>());
	private final GraphContainer m_graphContainer;

	public DefaultSelectionContext(GraphContainer graphContainer) {
		m_graphContainer = graphContainer;
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
	public Collection<EdgeRef> getSelectedEdgeRefs() {
		return Collections.unmodifiableSet(m_selectedEdges);
	}

	@Override
	public GraphContainer getGraphContainer() {
		return m_graphContainer;
	}

	@Override
	public boolean selectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> oldSet = new HashSet<>();
		oldSet.addAll(getSelectedVertexRefs());

		for (VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, true);
		}

		if (oldSet.equals(getSelectedVertexRefs())) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean deselectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> oldSet = new HashSet<>();
		oldSet.addAll(getSelectedVertexRefs());

		for (VertexRef vertexRef : vertexRefs) {
			setVertexRefSelected(vertexRef, false);
		}

		if (oldSet.equals(getSelectedVertexRefs())) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean deselectAll() {
		return (setSelectedVertexRefs(Collections.<VertexRef>emptySet())) || setSelectedEdgeRefs(Collections.<EdgeRef>emptySet());
	}

	private void doDeselectAll() {
		m_selectedEdges.clear();
		m_selectedVertices.clear();
	}

	@Override
	public boolean setSelectedVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> oldSet = new HashSet<>();
		oldSet.addAll(getSelectedVertexRefs());

		doDeselectAll();

		selectVertexRefs(vertexRefs);

		if (oldSet.equals(getSelectedVertexRefs())) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs) {
		Set<EdgeRef> oldSet = new HashSet<>();
		oldSet.addAll(getSelectedEdgeRefs());

		doDeselectAll();

		for(EdgeRef edgeRef : edgeRefs) {
			setEdgeRefSelected(edgeRef, true);
		}

		if (oldSet.equals(getSelectedEdgeRefs())) {
			return false;
		} else {
			return true;
		}
	}
}
