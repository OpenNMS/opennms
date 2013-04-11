/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
	public boolean selectVertexRefs(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> oldSet = new HashSet<VertexRef>();
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
		Set<VertexRef> oldSet = new HashSet<VertexRef>();
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
		doDeselectAll();

		selectVertexRefs(vertexRefs);

		// TODO: Can we return a more accurate value here?
		return true;
	}

	@Override
	public boolean setSelectedEdgeRefs(Collection<? extends EdgeRef> edgeRefs) {
		doDeselectAll();

		for(EdgeRef edgeRef : edgeRefs) {
			setEdgeRefSelected(edgeRef, true);
		}

		// TODO: Can we return a more accurate value here?
		return true;
	}
}
