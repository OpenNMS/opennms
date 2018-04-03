/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.support;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GroupRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.IpInterfaceProvider;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

/**
 * This <Criteria> implementation supports the users selection of search results from an IPLIKE query
 * in the topology UI.
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class IpLikeHopCriteria extends VertexHopCriteria implements SearchCriteria {

	public static final String NAMESPACE = "iplike";
	private final String m_ipQuery;
	
	private boolean m_collapsed = false;
	private IPVertex m_collapsedVertex;

	private IpInterfaceProvider ipInterfaceProvider;

	@Override
	public String getSearchString() {
		return m_ipQuery;
	}

	public static class IPVertex extends AbstractVertex implements GroupRef {
		private Set<VertexRef> m_children = new HashSet<>();

        public IPVertex(String namespace, String id, String label) {
			super(namespace, id, label);
			setIconKey("group");
		}

		@Override
		public boolean isGroup() {
			return true;
		}

        @Override
        public Set<VertexRef> getChildren() {
            return m_children;
        }

        public void setChildren(Set<VertexRef> children) {
            m_children = children;
        }
    }

	public IpLikeHopCriteria(SearchResult searchResult, IpInterfaceProvider ipInterfaceProvider) {
    	super(searchResult.getQuery());
    	m_collapsed = searchResult.isCollapsed();
        m_ipQuery = searchResult.getQuery();
        this.ipInterfaceProvider = Objects.requireNonNull(ipInterfaceProvider);
        m_collapsedVertex = new IPVertex(NAMESPACE, NAMESPACE+":"+m_ipQuery, m_ipQuery);
        m_collapsedVertex.setChildren(getVertices());
        setId(searchResult.getId());
    }

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ipQuery == null) ? 0 : m_ipQuery.hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof IpLikeHopCriteria) {
            IpLikeHopCriteria ref = (IpLikeHopCriteria)obj;
			return ref.m_ipQuery.equals(m_ipQuery) && ref.getNamespace().equals(getNamespace());
        }
        
        return false;
    }

	@Override
	public Set<VertexRef> getVertices() {
		
		CriteriaBuilder bldr = new CriteriaBuilder(OnmsIpInterface.class);

		bldr.iplike("ipAddr", m_ipQuery);
		List<OnmsIpInterface> ips = ipInterfaceProvider.findMatching(bldr.toCriteria());

		Set<VertexRef> vertices = new TreeSet<VertexRef>(new RefComparator());
		for (OnmsIpInterface ip : ips) {
			OnmsNode node = ip.getNode();
			vertices.add(new DefaultVertexRef("nodes", String.valueOf(node.getId()), node.getLabel()));
		}
		
		return vertices;
	}

	@Override
	public boolean isCollapsed() {
		return m_collapsed;
	}

	@Override
	public void setCollapsed(boolean collapsed) {
		if (collapsed != isCollapsed()) {
			this.m_collapsed = collapsed;
			setDirty(true);
		}
	}

	@Override
	public Vertex getCollapsedRepresentation() {
		return m_collapsedVertex;
	}
	
}
