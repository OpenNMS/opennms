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
package org.opennms.features.topology.app.internal.support;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractCollapsibleVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.IpInterfaceProvider;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private GraphContainer m_graphContainer;

	private final Logger LOG = LoggerFactory.getLogger(IpLikeHopCriteria.class);

	@Override
	public String getSearchString() {
		return m_ipQuery;
	}

	public static class IPVertex extends AbstractCollapsibleVertex {

        public IPVertex(String id) {
			super(NAMESPACE, NAMESPACE + ":" + id, id);
			setIconKey("group");
		}
    }

	public IpLikeHopCriteria(SearchResult searchResult, IpInterfaceProvider ipInterfaceProvider, GraphContainer graphContainer) {
    	super(searchResult.getQuery());
    	m_collapsed = searchResult.isCollapsed();
        m_ipQuery = searchResult.getQuery();
        this.ipInterfaceProvider = Objects.requireNonNull(ipInterfaceProvider);
        m_collapsedVertex = new IPVertex(m_ipQuery);
	m_graphContainer = Objects.requireNonNull(graphContainer);
	Objects.requireNonNull(graphContainer.getTopologyServiceClient());
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
		final Set<Integer> nodeids = ipInterfaceProvider.findMatching(bldr.toCriteria()).stream().map(ip -> ip.getNode().getId()).collect(Collectors.toSet());
		LOG.debug("getVertices: nodeids: {}", nodeids);
		final GraphProvider graphProvider = m_graphContainer.getTopologyServiceClient().getGraphProviderBy(m_graphContainer.getTopologyServiceClient().getNamespace());
		final BackendGraph currentGraph = graphProvider.getCurrentGraph();
		return currentGraph.getVertices().stream().filter(v -> v.getNodeID() != null && nodeids.contains(v.getNodeID())).collect(Collectors.toSet());
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
