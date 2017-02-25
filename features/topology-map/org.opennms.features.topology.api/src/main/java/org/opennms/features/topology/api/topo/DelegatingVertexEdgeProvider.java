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

package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;

public class DelegatingVertexEdgeProvider implements VertexProvider, EdgeProvider {
	protected SimpleVertexProvider m_vertexProvider;
	protected SimpleEdgeProvider m_edgeProvider;

	public DelegatingVertexEdgeProvider(String namespace) {
		this(namespace, namespace);
	}

	public DelegatingVertexEdgeProvider(String vertexNamespace, String edgeNamespace) {
		this(new SimpleVertexProvider(vertexNamespace), new SimpleEdgeProvider(edgeNamespace));
	}

	public DelegatingVertexEdgeProvider(SimpleVertexProvider vertexProvider, SimpleEdgeProvider edgeProvider) {
		m_vertexProvider = vertexProvider;
		m_edgeProvider = edgeProvider;
		if (!m_edgeProvider.getNamespace().equals(edgeProvider.getNamespace())) {
			throw new IllegalStateException("Namespace of edge and vertex provider must match");
		}
	}

	protected final SimpleVertexProvider getSimpleVertexProvider() {
		return m_vertexProvider;
	}

	protected final SimpleEdgeProvider getSimpleEdgeProvider() {
		return m_edgeProvider;
	}

	@Override
	public final void addVertexListener(VertexListener vertexListener) {
		m_vertexProvider.addVertexListener(vertexListener);
	}

	@Override
	public final void clearVertices() {
		m_vertexProvider.clearVertices();
	}

    @Override
    public int getVertexTotalCount() {
        return m_vertexProvider.getVertexTotalCount();
    }

	@Override
	public int getEdgeTotalCount() {
		return m_edgeProvider.getEdgeTotalCount();
	}

	@Override
	public final boolean contributesTo(String namespace) {
		return m_vertexProvider.contributesTo(namespace);
	}

	@Override
	public boolean containsVertexId(String id) {
		return m_vertexProvider.containsVertexId(id);
	}

	@Override
	public boolean containsVertexId(VertexRef id, Criteria... criteria) {
		return m_vertexProvider.containsVertexId(id, criteria);
	}

	@Override
	public final List<Vertex> getChildren(VertexRef group, Criteria... criteria) {
		return m_vertexProvider.getChildren(group, criteria);
	}

	@Override
	public final String getNamespace() {
		return m_vertexProvider.getNamespace();
	}

	@Override
	public final Vertex getParent(VertexRef vertex) {
		return m_vertexProvider.getParent(vertex);
	}

	@Override
	public final List<Vertex> getRootGroup() {
		return m_vertexProvider.getRootGroup();
	}

	@Override
	public final int getSemanticZoomLevel(VertexRef vertex) {
		return m_vertexProvider.getSemanticZoomLevel(vertex);
	}

	@Override
	public final Vertex getVertex(String namespace, String id) {
		return m_vertexProvider.getVertex(namespace, id);
	}

	@Override
	public final Vertex getVertex(VertexRef reference, Criteria... criteria) {
		return m_vertexProvider.getVertex(reference, criteria);
	}

	@Override
	public final List<Vertex> getVertices(Criteria... criteria) {
		return m_vertexProvider.getVertices(criteria);
	}

	@Override
	public final List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
		return m_vertexProvider.getVertices(references, criteria);
	}

	@Override
	public final boolean hasChildren(VertexRef group) {
		return m_vertexProvider.hasChildren(group);
	}

	@Override
	public final void removeVertexListener(VertexListener vertexListener) {
		m_vertexProvider.removeVertexListener(vertexListener);
	}

	@Override
	public final boolean setParent(VertexRef child, VertexRef parent) {
		return m_vertexProvider.setParent(child, parent);
	}

	@Override
	public final void addEdgeListener(EdgeListener listener) {
		m_edgeProvider.addEdgeListener(listener);
	}

	@Override
	public final void clearEdges() {
		m_edgeProvider.clearEdges();
	}

	@Override
	public final Edge getEdge(String namespace, String id) {
		return m_edgeProvider.getEdge(namespace, id);
	}

	@Override
	public final Edge getEdge(EdgeRef reference) {
		return m_edgeProvider.getEdge(reference);
	}

	@Override
	public final List<Edge> getEdges(Criteria... criteria) {
		return m_edgeProvider.getEdges(criteria);
	}

	@Override
	public final List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		return m_edgeProvider.getEdges(references);
	}

	@Override
	public final void removeEdgeListener(EdgeListener listener) {
		m_edgeProvider.removeEdgeListener(listener);
	}

}
