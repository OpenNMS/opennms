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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

public class SimpleEdgeProvider implements EdgeProvider {
	
	private static class MatchingCriteria extends Criteria {
		
		private String m_namespace;
		private String m_regex;
		public MatchingCriteria(String namespace, String regex) {
			m_namespace = namespace;
            m_regex = regex;
		}

		@Override
		public ElementType getType() {
			return ElementType.EDGE;
		}

		@Override
		public String getNamespace() {
			return m_namespace;
		}

        @Override
        public int hashCode() {
            return Objects.hash(m_namespace, m_regex);
        }

        @Override
        public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
            if (obj instanceof MatchingCriteria) {
                MatchingCriteria c = (MatchingCriteria) obj;
                boolean equals = Objects.equals(c.m_namespace, m_namespace) && Objects.equals(c.m_regex, m_regex);
				return equals;
            }
			return false;
        }

        public  boolean matches(Edge edge){
            return edge.getLabel().matches(m_regex);
        }
		
	}
	
	public static Criteria labelMatches(String namespace, final String regex) {
        return new MatchingCriteria(namespace, regex);
    }

    private final String m_namespace;
	private final Map<String, Edge> m_edgeMap = new LinkedHashMap<String, Edge>();
	private final Set<EdgeListener> m_listeners = new CopyOnWriteArraySet<>();
	private final String m_contributesTo;
	
	public SimpleEdgeProvider(String namespace, String contributesTo) {
		m_namespace = namespace;
		m_contributesTo = contributesTo;
	}
	
	public SimpleEdgeProvider(String namespace) {
		this(namespace, null);
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}
	
	@Override
	public boolean contributesTo(String namespace) {
		return m_contributesTo != null && m_contributesTo.equals(namespace);
	}
	
	private Edge getEdge(String id) {
		return m_edgeMap.get(id);
	}
	
	@Override
	public Edge getEdge(String namespace, String id) {
		return getEdge(id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return getSimpleEdge(reference);
	}

	private Edge getSimpleEdge(EdgeRef reference) {
		if (getNamespace().equals(reference.getNamespace())) {
			if (reference instanceof Edge) {
				return Edge.class.cast(reference);
			} else {
				return m_edgeMap.get(reference.getId());
			}
		} 
		return null;
	}

	@Override
	public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<Edge> edges = new ArrayList<>();
		for(EdgeRef ref : references) {
			Edge edge = getSimpleEdge(ref);
			if (ref != null) {
				edges.add(edge);
			}
		}
		return Collections.unmodifiableList(edges);
	}

	private void fireEdgeSetChanged() {
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, null, null, null);
		}
	}

	private void fireEdgesAdded(List<Edge> edges) {
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, edges, null, null);
		}
	}

	private void fireEdgesRemoved(List<? extends EdgeRef> edges) {
		List<String> ids = new ArrayList<String>(edges.size());
		for(EdgeRef e : edges) {
			ids.add(e.getId());
		}
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, null, null, ids);
		}
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_listeners.add(edgeListener);
	}

	@Override
	public void removeEdgeListener(EdgeListener edgeListener) {
		m_listeners.remove(edgeListener);
	}
	
	private void removeEdges(List<? extends EdgeRef> edges) {
		for(EdgeRef edge : edges) {
			m_edgeMap.remove(edge.getId());
		}
	}
	
	private void addEdges(List<Edge> edges) {
		for(Edge edge : edges) {
			if (edge.getNamespace() == null || edge.getId() == null) {
				LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid edge: {}", edge);
				continue;
			}
			LoggerFactory.getLogger(this.getClass()).trace("Adding edge: {}", edge);
			m_edgeMap.put(edge.getId(), edge);
		}
	}
	
	public void setEdges(List<Edge> edges) {
		m_edgeMap.clear();
		addEdges(edges);
		fireEdgeSetChanged();
	}
	
	public void add(Edge...edges) {
		add(Arrays.asList(edges));
	}
	
	public void add(List<Edge> edges) {
		addEdges(edges);
		fireEdgesAdded(edges);
	}
	
	public void remove(List<EdgeRef> edges) {
		removeEdges(edges);
		fireEdgesRemoved(edges);
	}
	
	public void remove(EdgeRef... edges) {
		remove(Arrays.asList(edges));
	}

	@Override
	public List<Edge> getEdges(Criteria... criteria) {
		List<Edge> edges = new ArrayList<>();
		for (Edge edge : m_edgeMap.values()) {
			edges.add(edge.clone());
		}

		for (Criteria criterium : criteria) {
			try {
				MatchingCriteria matchingCriteria = (MatchingCriteria)criterium;
				for(Iterator<Edge> itr = edges.iterator(); itr.hasNext();) {
					Edge next = itr.next();
					if (
						matchingCriteria.getType() == Criteria.ElementType.EDGE &&
						matchingCriteria.getNamespace() == getNamespace() &&
						!matchingCriteria.matches(next)
					) {
						itr.remove();
					}
				}
			} catch (ClassCastException e) {}
		}

		return Collections.unmodifiableList(edges);
	}

	@Override
	public void clearEdges() {
		List<Edge> all = getEdges();
		removeEdges(all);
		fireEdgesRemoved(all);
	}

	@Override
	public int getEdgeTotalCount() {
		return m_edgeMap.size();
	}
}
