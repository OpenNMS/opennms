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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.ProviderManager.ProviderListener;
import org.slf4j.LoggerFactory;

public class MergingGraphProvider implements GraphProvider, VertexListener, EdgeListener, ProviderListener {
	
	private static final GraphProvider NULL_PROVIDER = new NullProvider();
	
	/**
	 * This provider is the bottom-level provider that we delegate to.
	 */
	private GraphProvider m_baseGraphProvider;
	
	/**
	 * This object relays registration events to update the lists of vertex and edge providers.
	 */
	private final Map<String, VertexProvider> m_vertexProviders = new HashMap<String, VertexProvider>();
	private final Map<String, EdgeProvider> m_edgeProviders = new HashMap<String, EdgeProvider>();
	private final Set<VertexListener> m_vertexListeners = new CopyOnWriteArraySet<VertexListener>();
	private final Set<EdgeListener> m_edgeListeners = new CopyOnWriteArraySet<EdgeListener>();
	
	public MergingGraphProvider(GraphProvider baseGraphProvider, ProviderManager providerManager) {
		m_baseGraphProvider = baseGraphProvider;

		for(VertexProvider vertexProvider : providerManager.getVertexListeners()) {
			addVertexProvider(vertexProvider);
		}
		
		for(EdgeProvider edgeProvider : providerManager.getEdgeListeners()) {
			addEdgeProvider(edgeProvider);
		}
		
		providerManager.addProviderListener(this);
	}
	
	@Override
	public String getVertexNamespace() {
		return m_baseGraphProvider.getVertexNamespace();
	}
	
	@Override
	public String getEdgeNamespace() {
		return m_baseGraphProvider.getEdgeNamespace();
	}
	
	@Override
	public boolean contributesTo(String namespace) {
		return false;
	}

	protected GraphProvider getBaseGraphProvider() {
		return m_baseGraphProvider;
	}

	protected void setBaseGraphProvider(GraphProvider baseGraphProvider) {

		m_baseGraphProvider.removeEdgeListener(this);
		m_baseGraphProvider.removeVertexListener(this);
		
		m_baseGraphProvider = baseGraphProvider;
		
		m_baseGraphProvider.addVertexListener(this);
		m_baseGraphProvider.addEdgeListener(this);
		
		fireVertexChanged();
		fireEdgeChanged();
	}
	
	protected void addVertexProvider(VertexProvider vertexProvider) {
		VertexProvider oldProvider = m_vertexProviders.put(vertexProvider.getVertexNamespace(), vertexProvider);
	
		if (oldProvider != null) { oldProvider.removeVertexListener(this); }
		vertexProvider.addVertexListener(this);

		String ns = m_baseGraphProvider.getVertexNamespace();
		if ((oldProvider != null && oldProvider.contributesTo(ns)) || vertexProvider.contributesTo(ns)) {
			fireVertexChanged();
		}

	}

	protected void removeVertexProvider(VertexProvider vertexProvider) {
		VertexProvider oldProvider = m_vertexProviders.remove(vertexProvider.getVertexNamespace());
		
		if (oldProvider == null) { return; }
		
		oldProvider.removeVertexListener(this);
		if (oldProvider.contributesTo(m_baseGraphProvider.getVertexNamespace())) {
			fireVertexChanged();
		}
	}
	
	protected void addEdgeProvider(EdgeProvider edgeProvider) {
		EdgeProvider oldProvider = m_edgeProviders.put(edgeProvider.getEdgeNamespace(), edgeProvider);

		if (oldProvider != null) { oldProvider.removeEdgeListener(this); }

		edgeProvider.addEdgeListener(this);

		String ns = m_baseGraphProvider.getVertexNamespace();
		if ((oldProvider != null && oldProvider.contributesTo(ns)) || edgeProvider.contributesTo(ns)) {
			fireEdgeChanged();
		}
	}
	
	protected void removeEdgeProvider(EdgeProvider edgeProvider) {
		EdgeProvider oldProvider = m_edgeProviders.remove(edgeProvider.getEdgeNamespace());

		if (oldProvider == null) { return; }
		
		oldProvider.removeEdgeListener(this);
		// TODO: This seems a little weird because it is matching the vertex namespace of the
		// base provider with the edge namespace of this EdgeProvider
		if (oldProvider.contributesTo(m_baseGraphProvider.getVertexNamespace())) {
			fireEdgeChanged();
		}
	}

	protected void assertNotNull(Object o, String msg) {
		if (o == null) throw new NullPointerException(msg);
	}
	
	protected VertexProvider vProvider(String namespace) {
		assertNotNull(namespace, "namespace may not be null");

		if (namespace.equals(m_baseGraphProvider.getVertexNamespace())) {
			return m_baseGraphProvider;
		}
		
		for(VertexProvider provider : m_vertexProviders.values()) {
			if (namespace.equals(provider.getVertexNamespace())) {
				return provider;
			}
		}

		return NULL_PROVIDER;
	}	
	
	protected VertexProvider vProvider(VertexRef vertexRef) {
		assertNotNull(vertexRef, "vertexRef may not be null");
		return vProvider(vertexRef.getNamespace());
	}
	
	protected EdgeProvider eProvider(EdgeRef edgeRef) {
		assertNotNull(edgeRef, "edgeRef may not be null");
		return eProvider(edgeRef.getNamespace());
	}

	protected EdgeProvider eProvider(String namespace) {
		assertNotNull(namespace, "namespace may not be null");

		if (namespace.equals(m_baseGraphProvider.getVertexNamespace())) {
			return m_baseGraphProvider;
		}

		for(EdgeProvider provider : m_edgeProviders.values()) {
			if (namespace.equals(provider.getEdgeNamespace())) {
				return provider;
			}
		}

		return NULL_PROVIDER;
	}

	@Override
	public Vertex getVertex(String namespace, String id) {
		return vProvider(namespace).getVertex(namespace, id);
	}

	@Override
	public Vertex getVertex(VertexRef reference, Criteria... criteria) {
		return vProvider(reference).getVertex(reference, criteria);
	}

	@Override
	public int getSemanticZoomLevel(VertexRef vertex) {
		return vProvider(vertex).getSemanticZoomLevel(vertex);
	}
	
	@Override
	public List<Vertex> getVertices(Criteria... criteria) {
		List<Vertex> vertices = new ArrayList<Vertex>(filteredVertices(m_baseGraphProvider, criteria, true));
		
		for(VertexProvider vertexProvider : m_vertexProviders.values()) {
			if (vertexProvider.contributesTo(m_baseGraphProvider.getVertexNamespace())) {
				vertices.addAll(filteredVertices(vertexProvider, criteria, false));
			}
		}
		
		return vertices;
	}

	protected List<? extends Vertex> filteredVertices(VertexProvider vertexProvider, Criteria[] criteria, boolean returnAllOnNullFilter) {
		// Set<Criteria> criteria = m_criteria.get(vertexProvider.getVertexNamespace());
		if (criteria != null && criteria.length > 0) {
			return vertexProvider.getVertices(criteria);
		} else {
			if (returnAllOnNullFilter) {
				return vertexProvider.getVertices();
			} else {
				return Collections.<Vertex>emptyList();
			}
		}
	}

	@Override
	public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
		List<Vertex> vertices = new ArrayList<Vertex>(references.size());
		
		for(VertexRef vertexRef : references) {
			Vertex vertex = getVertex(vertexRef, criteria);
			if (vertex != null) {
				vertices.add(vertex);
			}
		}
		
		return vertices;
	}

	@Override
	public List<Vertex> getRootGroup() {
		return m_baseGraphProvider.getRootGroup();
	}

	@Override
	public boolean hasChildren(VertexRef group) {
		return vProvider(group).hasChildren(group);
	}

	@Override
	public Vertex getParent(VertexRef vertex) {
		return vProvider(vertex).getParent(vertex);
	}

	@Override
	public List<Vertex> getChildren(VertexRef group, Criteria... criteria) {
		return vProvider(group).getChildren(group, criteria);
	}

	@Override
	public Edge getEdge(String namespace, String id) {
		return eProvider(namespace).getEdge(namespace, id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return eProvider(reference).getEdge(reference);
	}

	@Override
	public List<Edge> getEdges(Criteria... criteria) {
		List<Edge> edges = new ArrayList<Edge>(filteredEdges(m_baseGraphProvider, criteria, true));
		
		for(EdgeProvider edgeProvider : m_edgeProviders.values()) {
			if (edgeProvider.contributesTo(m_baseGraphProvider.getEdgeNamespace())) {
				edges.addAll(filteredEdges(edgeProvider, criteria, false));
			}
		}
		
		return edges;
	}

	protected List<? extends Edge> filteredEdges(EdgeProvider edgeProvider, Criteria[] criteria, boolean returnAllOnNullFilter) {
		// Set<Criteria> criteria = m_criteria.get(edgeProvider.getEdgeNamespace());
		if (criteria != null && criteria.length > 0) {
			return edgeProvider.getEdges(criteria);
		} else {
			if (returnAllOnNullFilter) {
				return edgeProvider.getEdges();
			} else {
				return Collections.<Edge>emptyList();
			}
		}
	}

	@Override
	public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<Edge> edges = new ArrayList<Edge>(references.size());
		
		for(EdgeRef edgeRef : references) {
			edges.add(getEdge(edgeRef));
		}
		
		return edges;
	}
	
	@Override
	public void clearEdges() {
		m_baseGraphProvider.clearEdges();
		for(EdgeProvider edgeProvider : m_edgeProviders.values()) {
			try {
				edgeProvider.clearEdges();
			} catch (Throwable e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception caught while calling clearEdges()", e);
			}
		}
	}
	
	@Override
	public void addEdges(Edge... edges) {
		for (Edge edge : edges) {
			((SimpleEdgeProvider)eProvider(edge)).add(edge);
		}
	}

	/**
	 * TODO Is this going to work properly?
	 */
	@Override
	public boolean groupingSupported() {
		return m_baseGraphProvider.groupingSupported();
	}

	/**
	 * TODO Is this going to work properly?
	 */
	@Override
	public Vertex addGroup(String label, String iconKey) {
		return m_baseGraphProvider.addGroup(label, iconKey);
	}

	/**
	 * TODO Is this going to work properly?
	 */
	@Override
	public Vertex addVertex(int x, int y) {
		return m_baseGraphProvider.addVertex(x, y);
	}

	@Override
	public void addVertices(Vertex... vertices) {
		for (Vertex vertex : vertices) {
			((SimpleVertexProvider)vProvider(vertex)).add(vertex);
		}
	}

	/**
	 * TODO Is this going to work properly?
	 */
	@Override
	public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
		return m_baseGraphProvider.connectVertices(sourceVertextId, targetVertextId);
	}

    @Override
    public Criteria getDefaultCriteria() {
        return m_baseGraphProvider.getDefaultCriteria();
    }

    /**
	 * TODO This will miss edges provided by auxiliary edge providers
	 */
	@Override
	public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
		return m_baseGraphProvider.getEdgeIdsForVertex(vertex);
	}

	/**
	 * TODO This will miss edges provided by auxiliary edge providers
	 */
	@Override
	public Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertices) {
		return m_baseGraphProvider.getEdgeIdsForVertices(vertices);
	}

	@Override
	public void load(String filename) {
		// Do nothing
	}

    @Override
    public void refresh() {
        m_baseGraphProvider.refresh();
        // TODO: Should we refresh the vertex and edge providers also??
    }

	@Override
	public void removeEdges(EdgeRef... edges) {
		for (EdgeRef edge : edges) {
			((SimpleEdgeProvider)eProvider(edge)).remove(edge);
		}
	}

	@Override
	public void removeVertex(VertexRef... vertexId) {
		for (VertexRef vertex : vertexId) {
			((SimpleVertexProvider)vProvider(vertex)).remove(vertex);
		}
	}

	@Override
	public void resetContainer() {
		m_baseGraphProvider.resetContainer();
		for (EdgeProvider provider : m_edgeProviders.values()) {
			provider.clearEdges();
		}
		for (VertexProvider provider : m_vertexProviders.values()) {
			provider.clearVertices();
		}
	}

	@Override
	public void save() {
		// TODO Do nothing?
	}

	/**
	 * This function will always delegate to the base graph provider. It must be responsible for
	 * grouping.
	 */
	@Override
	public boolean setParent(VertexRef vertexId, VertexRef parentId) {
		if (vertexId.equals(parentId)) return false;
		return m_baseGraphProvider.setParent(vertexId, parentId);
	}

	@Override
	public void clearVertices() {
		m_baseGraphProvider.clearVertices();
		for (VertexProvider provider : m_vertexProviders.values()) {
			try {
				provider.clearVertices();
			} catch (Throwable e) {
				LoggerFactory.getLogger(this.getClass()).warn("Exception caught while calling clearVertices()", e);
			}
		}
	}

    @Override
    public int getVertexTotalCount() {
        return m_baseGraphProvider.getVertexTotalCount();
    }

    /**
	 * @deprecated Use {@link #containsVertexId(VertexRef id)} instead.
	 */
	@Override
	public boolean containsVertexId(String id) {
		if (containsVertexId(new DefaultVertexRef(getVertexNamespace(), id))) {
			return true;
		}
		for (VertexProvider provider : m_vertexProviders.values()) {
			if (containsVertexId(new DefaultVertexRef(provider.getVertexNamespace(), id))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsVertexId(VertexRef id, Criteria... criteria) {
		if (m_baseGraphProvider.containsVertexId(id, criteria)) {
			return true;
		} else {
			for (VertexProvider provider : m_vertexProviders.values()) {
				if (provider.containsVertexId(id, criteria)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void fireVertexChanged() {
		for(VertexListener listener : m_vertexListeners) {
			listener.vertexSetChanged(this);
		}
	}
	
	

	@Override
	public void vertexSetChanged(VertexProvider provider) {
		fireVertexChanged();
	}

	@Override
	public void vertexSetChanged(VertexProvider provider, Collection<? extends Vertex> added, Collection<? extends Vertex> update,
			Collection<String> removedVertexIds) {
		fireVertexChanged();
	}

	@Override
	public void addVertexListener(VertexListener vertexListener) {
		m_vertexListeners.add(vertexListener);
	}

	@Override
	public void removeVertexListener(VertexListener vertexListener) {
		m_vertexListeners.remove(vertexListener);
	}
	
	protected void fireEdgeChanged() {
		for(EdgeListener listener : m_edgeListeners) {
			listener.edgeSetChanged(this);
		}
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider) {
		fireEdgeChanged();
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider,
			Collection<? extends Edge> added, Collection<? extends Edge> updated,
			Collection<String> removedEdgeIds) {
		fireEdgeChanged();
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_edgeListeners.add(edgeListener);
	}

	@Override
	public void removeEdgeListener(EdgeListener edgeListener) {
		m_edgeListeners.remove(edgeListener);
	}
	
	private static class NullProvider implements GraphProvider {

		@Override
		public String getVertexNamespace() {
			return null;
		}

		@Override
		public String getEdgeNamespace() {
			return null;
		}

		@Override
		public Vertex getVertex(String namespace, String id) {
			return null;
		}

		@Override
		public Vertex getVertex(VertexRef reference, Criteria... criteria) {
			return null;
		}

		@Override
		public int getSemanticZoomLevel(VertexRef vertex) {
			return 0;
		}

		@Override
		public List<Vertex> getVertices(Criteria... criteria) {
			return Collections.emptyList();
		}

		@Override
		public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
			return Collections.emptyList();
		}

		@Override
		public List<Vertex> getRootGroup() {
			return Collections.emptyList();
		}

		@Override
		public boolean hasChildren(VertexRef group) {
			return false;
		}

		@Override
		public Vertex getParent(VertexRef vertex) {
			return null;
		}

		@Override
		public List<Vertex> getChildren(VertexRef group, Criteria... criteria) {
			return Collections.emptyList();
		}

		@Override
		public void addVertexListener(VertexListener vertexListener) {
		}

		@Override
		public void removeVertexListener(VertexListener vertexListener) {
		}

		@Override
		public Edge getEdge(String namespace, String id) {
			return null;
		}

		@Override
		public Edge getEdge(EdgeRef reference) {
			return null;
		}

		@Override
		public List<Edge> getEdges(Criteria... criteria) {
			return Collections.emptyList();
		}

		@Override
		public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
			return Collections.emptyList();
		}

		@Override
		public void addEdgeListener(EdgeListener vertexListener) {
		}

		@Override
		public void removeEdgeListener(EdgeListener vertexListener) {
		}

		@Override
		public boolean contributesTo(String namespace) {
			return false;
		}

		@Override
		public void clearVertices() {
			// Do nothing
		}

        @Override
        public int getVertexTotalCount() {
            return 0;
        }

        @Override
		public boolean setParent(VertexRef child, VertexRef parent) {
			return false;
		}

		@Override
		public boolean groupingSupported() {
			return false;
		}

		@Override
		public Vertex addGroup(String label, String iconKey) {
			return null;
		}

		@Override
		public Vertex addVertex(int x, int y) {
			return null;
		}

		@Override
		public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
			return null;
		}

        @Override
        public Criteria getDefaultCriteria() {
            return null;
        }

        @Override
		public void load(String filename) {
			// Do nothing
		}

        @Override
        public void refresh() {
            // Do nothing
        }

		@Override
		public void removeVertex(VertexRef... vertexId) {
			// Do nothing
		}

		@Override
		public void resetContainer() {
			// Do nothing
		}

		@Override
		public void save() {
			// Do nothing
		}

		@Override
		public void addEdges(Edge... edges) {
			// Do nothing
		}

		@Override
		public void addVertices(Vertex... vertices) {
			// Do nothing
		}

		@Override
		public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
			return new EdgeRef[0];
		}

		@Override
		public Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertex) {
			return Collections.emptyMap();
		}

		@Override
		public void removeEdges(EdgeRef... edges) {
			// Do nothing
		}

		@Override
		public boolean containsVertexId(String id) {
			return false;
		}

		@Override
		public boolean containsVertexId(VertexRef id, Criteria... criteria) {
			return false;
		}

		@Override
		public void clearEdges() {
			// Do nothing
		}
	}

	@Override
	public void edgeProviderAdded(EdgeProvider oldProvider,	EdgeProvider newProvider) {
		addEdgeProvider(newProvider);
	}

	@Override
	public void edgeProviderRemoved(EdgeProvider removedProvider) {
		removeEdgeProvider(removedProvider);
	}

	@Override
	public void vertexProviderAdded(VertexProvider oldProvider,	VertexProvider newProvider) {
		addVertexProvider(newProvider);
	}

	@Override
	public void vertexProviderRemoved(VertexProvider removedProvider) {
		removeVertexProvider(removedProvider);
	}

}
