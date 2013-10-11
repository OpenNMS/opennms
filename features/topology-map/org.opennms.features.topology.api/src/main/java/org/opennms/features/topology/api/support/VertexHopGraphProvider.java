/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will be used to filter a topology so that the semantic zoom level is
 * interpreted as a hop distance away from a set of selected vertices. The vertex 
 * selection is specified using a {@link Criteria} filter.
 * 
 * @author Seth
 */
public class VertexHopGraphProvider implements GraphProvider {
	private static final Logger LOG = LoggerFactory.getLogger(VertexHopGraphProvider.class);

	public static FocusNodeHopCriteria getFocusNodeHopCriteriaForContainer(GraphContainer graphContainer) {
		return getFocusNodeHopCriteriaForContainer(graphContainer, true);
	}

	public static FocusNodeHopCriteria getFocusNodeHopCriteriaForContainer(GraphContainer graphContainer, boolean createIfAbsent) {
		Criteria[] criteria = graphContainer.getCriteria();
		if (criteria != null) {
			for (Criteria criterium : criteria) {
				try {
					FocusNodeHopCriteria hopCriteria = (FocusNodeHopCriteria)criterium;
					return hopCriteria;
				} catch (ClassCastException e) {}
			}
		}

		if (createIfAbsent) {
			FocusNodeHopCriteria hopCriteria = new FocusNodeHopCriteria();
			graphContainer.setCriteria(hopCriteria);
			return hopCriteria;
		} else {
			return null;
		}
	}

	public abstract static class VertexHopCriteria implements Criteria {
		private String label = "";

		@Override
		public ElementType getType() {
			return ElementType.VERTEX;
		}

		public abstract Set<VertexRef> getVertices();

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	public static class FocusNodeHopCriteria extends VertexHopCriteria {

		private static final long serialVersionUID = 2904432878716561926L;

		private static final Set<VertexRef> m_vertices = new TreeSet<VertexRef>(new RefComparator());

		public FocusNodeHopCriteria() {
			super();
		}

		public FocusNodeHopCriteria(Collection<VertexRef> objects) {
			m_vertices.addAll(objects);
		}

		/**
		 * TODO: This return value doesn't matter since we just delegate
		 * to the m_delegate provider.
		 */
		@Override
		public String getNamespace() {
			return "nodes";
		}

		public void add(VertexRef ref) {
			m_vertices.add(ref);
		}

		public void remove(VertexRef ref) {
			m_vertices.remove(ref);
		}

		public void clear() {
			m_vertices.clear();
		}

		public boolean contains(VertexRef ref) {
			return m_vertices.contains(ref);
		}

		public int size() {
			return m_vertices.size();
		}

		@Override
		public Set<VertexRef> getVertices() {
			return Collections.unmodifiableSet(m_vertices);
		}

		public void addAll(Collection<VertexRef> refs) {
			m_vertices.addAll(refs);
		}
	}

	/*

	We don't need this... we'll just use the {@link FocusNodeHopCriteria}

	public static class NcsHopCriteria extends VertexHopCriteria {
		
		private final long m_ncsServiceId;	
		
		public NcsHopCriteria(long ncsServiceId) {
			m_ncsServiceId = ncsServiceId;
		}

		/ **
		 * TODO: This return value doesn't matter since we just delegate
		 * to the m_delegate provider.
		 * /
		@Override
		public String getNamespace() {
			return "nodes";
		}

		public Set<VertexRef> getVertices() {
			// TODO Use NCSEdgeProvider to query for vertices that are attached
			// to the NCS edges for the service ID
			return Collections.emptySet();
		}
	}
	*/

	private final GraphProvider m_delegate;

	private final Map<VertexRef,Integer> m_semanticZoomLevels = new LinkedHashMap<VertexRef,Integer>();

	public VertexHopGraphProvider(GraphProvider delegate) {
		m_delegate = delegate;
	}

	@Override
	public void save() {
		m_delegate.save();
	}

	@Override
	public void load(String filename) throws MalformedURLException, JAXBException {
		m_delegate.load(filename);
	}

	@Override
	public void refresh() {
		m_delegate.refresh();
	}

	@Override
	public String getVertexNamespace() {
		return m_delegate.getVertexNamespace();
	}

	@Override
	public boolean contributesTo(String namespace) {
		return m_delegate.contributesTo(namespace);
	}

	@Override
	public boolean containsVertexId(String id) {
		return m_delegate.containsVertexId(id);
	}

	@Override
	public boolean containsVertexId(VertexRef id) {
		return m_delegate.containsVertexId(id);
	}

	@Override
	public Vertex getVertex(String namespace, String id) {
		return m_delegate.getVertex(namespace, id);
	}

	@Override
	public Vertex getVertex(VertexRef reference) {
		return m_delegate.getVertex(reference);
	}

	@Override
	public int getSemanticZoomLevel(VertexRef vertex) {
		Integer szl = m_semanticZoomLevels.get(vertex);
		return szl == null ? 0 : szl;
	}

	@Override
	public List<Vertex> getVertices(Criteria... criteria) {
		List<Vertex> retval = new ArrayList<Vertex>();
		List<VertexRef> currentHops = new ArrayList<VertexRef>();
		List<VertexRef> nextHops = new ArrayList<VertexRef>();
		List<Vertex> allVertices = new ArrayList<Vertex>();

		// Get the entire list of vertices
		allVertices.addAll(m_delegate.getVertices(criteria));

		// Find the vertices that match a required HopDistanceCriteria
		for (Criteria criterium : criteria) {
			try {
				FocusNodeHopCriteria hdCriteria = (FocusNodeHopCriteria)criterium;
				for (Iterator<Vertex> itr = allVertices.iterator();itr.hasNext();) {
					Vertex vertex = itr.next();
					if (hdCriteria.contains(vertex)) {
						// Put the vertex into the return value and remove it
						// from the list of all eligible vertices
						retval.add(vertex);
						nextHops.add(vertex);
						itr.remove();
						LOG.debug("Added {} to selected vertex list", vertex);
					}
				}
			} catch (ClassCastException e) {}
		}

		// Clear the existing semantic zoom level values
		m_semanticZoomLevels.clear();
		int semanticZoomLevel = 0;

		// If we didn't find any matching nodes among the focus nodes...
		if (nextHops.size() < 1) {
			// ...then return an empty list of vertices
			return Collections.emptyList();
		}

		// Put a limit on the SZL in case we infinite loop for some reason
		while (semanticZoomLevel < 100 && nextHops.size() > 0) {
			currentHops.addAll(nextHops);
			nextHops.clear();

			Map<VertexRef,Set<EdgeRef>> edges = m_delegate.getEdgeIdsForVertices(currentHops.toArray(new VertexRef[0]));

			for (VertexRef vertex : currentHops) {

				// Mark the current vertex as belonging to a particular SZL
				if (m_semanticZoomLevels.get(vertex) != null) {
					throw new IllegalStateException("Calculating semantic zoom level for vertex that has already been calculated: " + vertex.toString());
				}
				m_semanticZoomLevels.put(vertex, semanticZoomLevel);
				// Put the vertex into the full list of vertices that is returned
				retval.add(getVertex(vertex));

				// Fetch all edges attached to this vertex
				for (EdgeRef edgeRef : edges.get(vertex)) {
					Edge edge = m_delegate.getEdge(edgeRef);

					// Find everything attached to those edges
					VertexRef nextVertex = null;
					if (vertex.equals(edge.getSource().getVertex())) {
						nextVertex = edge.getTarget().getVertex();
					} else if (vertex.equals(edge.getTarget().getVertex())) {
						nextVertex = edge.getSource().getVertex();
					} else {
						throw new IllegalStateException(String.format("Vertex %s was not the source or target of edge %s", vertex.toString(), edge.toString()));
					}

					// If we haven't assigned a SZL to the vertices that were located,
					// then put the vertex into the next collection of hops
					if (allVertices.contains(nextVertex)) {
						nextHops.add(nextVertex);
						allVertices.remove(nextVertex);
					}
				}
			}

			// Clear the temp list of current hops
			currentHops.clear();

			// Increment the semantic zoom level
			semanticZoomLevel++;
		}

		return retval;
	}

	/**
	 * TODO: OVERRIDE THIS FUNCTION?
	 */
	@Override
	public List<Vertex> getVertices(Collection<? extends VertexRef> references) {
		return m_delegate.getVertices(references);
	}

	/**
	 * TODO: Is this correct?
	 */
	@Override
	public List<Vertex> getRootGroup() {
		return getVertices();
	}

	@Override
	public boolean hasChildren(VertexRef group) {	
		return false;
	}

	@Override
	public Vertex getParent(VertexRef vertex) {
		// throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
		return null;
	}

	@Override
	public boolean setParent(VertexRef child, VertexRef parent) {
		// throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
		return false;
	}

	@Override
	public List<Vertex> getChildren(VertexRef group) {
		return Collections.emptyList();
	}

	@Override
	public void addVertexListener(VertexListener vertexListener) {
		m_delegate.addVertexListener(vertexListener);
	}

	@Override
	public void removeVertexListener(VertexListener vertexListener) {
		m_delegate.removeVertexListener(vertexListener);
	}

	@Override
	public void clearVertices() {
		m_delegate.clearVertices();
	}

	@Override
	public String getEdgeNamespace() {
		return m_delegate.getEdgeNamespace();
	}

	@Override
	public Edge getEdge(String namespace, String id) {
		return m_delegate.getEdge(namespace, id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return m_delegate.getEdge(reference);
	}

	/**
	 * TODO OVERRIDE THIS FUNCTION?
	 */
	@Override
	public List<Edge> getEdges(Criteria... criteria) {
		return m_delegate.getEdges(criteria);
	}

	@Override
	public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		return m_delegate.getEdges(references);
	}

	@Override
	public void addEdgeListener(EdgeListener listener) {
		m_delegate.addEdgeListener(listener);
	}

	@Override
	public void removeEdgeListener(EdgeListener listener) {
		m_delegate.removeEdgeListener(listener);
	}

	@Override
	public void clearEdges() {
		m_delegate.clearEdges();
	}

	@Override
	public void resetContainer() {
		m_delegate.resetContainer();
	}

	@Override
	public void addVertices(Vertex... vertices) {
		m_delegate.addVertices(vertices);
	}

	@Override
	public void removeVertex(VertexRef... vertexId) {
		m_delegate.removeVertex(vertexId);
	}

	@Override
	public Vertex addVertex(int x, int y) {
		return m_delegate.addVertex(x, y);
	}

	@Override
	public Vertex addGroup(String label, String iconKey) {
		throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
	}

	@Override
	public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
		return m_delegate.getEdgeIdsForVertex(vertex);
	}

	/**
	 * TODO This will miss edges provided by auxiliary edge providers
	 */
	@Override
	public Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertices) {
		return m_delegate.getEdgeIdsForVertices(vertices);
	}

	@Override
	public void addEdges(Edge... edges) {
		m_delegate.addEdges(edges);
	}

	@Override
	public void removeEdges(EdgeRef... edges) {
		m_delegate.removeEdges(edges);
	}

	@Override
	public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
		return m_delegate.connectVertices(sourceVertextId, targetVertextId);
	}
}
