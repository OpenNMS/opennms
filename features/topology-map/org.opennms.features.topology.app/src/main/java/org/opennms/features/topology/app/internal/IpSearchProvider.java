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

package org.opennms.features.topology.app.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.IpLikeHopCriteria;
import org.opennms.features.topology.app.internal.support.IpLikeHopCriteriaFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;

public class IpSearchProvider extends AbstractSearchProvider implements SearchProvider {

    private IpInterfaceDao m_ipInterfaceDao;
	private NodeDao m_nodeDao;
    private IpLikeHopCriteriaFactory m_ipLikeHopFactory;

	public IpSearchProvider(IpInterfaceDao ipInterfaceDao, NodeDao nodeDao) {
    	m_ipInterfaceDao = ipInterfaceDao;
    	m_nodeDao = nodeDao;
    	m_ipLikeHopFactory = new IpLikeHopCriteriaFactory(m_nodeDao);
    }

    @Override
    public String getSearchProviderNamespace() {
        return "IP";
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
    	
    	CriteriaBuilder bldr = new CriteriaBuilder(OnmsIpInterface.class);
		bldr.iplike("ipAddress", searchQuery.getQueryString()).orderBy("ipAddress", true);
		Criteria dbQueryCriteria = bldr.toCriteria();
		List<OnmsIpInterface> ips = m_ipInterfaceDao.findMatching(dbQueryCriteria);
		
		org.opennms.features.topology.api.topo.Criteria[] criteria = graphContainer.getCriteria();
		

        List<SearchResult> results = new ArrayList<SearchResult>();
        IPLOOP: for (OnmsIpInterface ip : ips) {
        	
        	for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
				if (criterion.getNamespace().equals(getSearchProviderNamespace())) {
					String ipInterfaceId = ((IpLikeHopCriteria) criterion).getId();
					if (ipInterfaceId.equals(String.valueOf(ip.getId()))) {
						continue IPLOOP;
					}
				}
			}
        	
        	//Important here that the search result use the OnmsIpInterface.id as the ID for the search result object.
        	String ipInterfaceId = ip.getId().toString();
			SearchResult result = new SearchResult("IP", ipInterfaceId, ip.getIpAddress().getHostAddress());

        	result.setCollapsible(true);
        	
        	results.add(result);
        }
        return results;
    }


    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return false;
    }

    //FIXME so that the focus button works.
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
    	
    	org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult, container);
    	
    	return ((IpLikeHopCriteria)criterion).getVertices();
    	
    }
    

    /**
     * Creates a criteria that provides <VertexRefs> matching the IPLIKE query from the users query
     * stored in the <SearchResult> created by this class during the query method.  The SearchResult 
     * and the Criterion use the OnmsIpinterfaceId as the ID for dereferencing in the container.
     */
	@Override
	public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
		
		IpLikeHopCriteria criteria = m_ipLikeHopFactory.createCriteria(searchResult.getLabel());
		String ipInterfaceId = searchResult.getId();
		criteria.setId(ipInterfaceId);
		container.addCriteria(criteria);
	}

	
	@Override
	public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
		
		org.opennms.features.topology.api.topo.Criteria criterian = findCriterion(searchResult, container);
		
		if (criterian != null) {
			container.removeCriteria(criterian);
		}
		
	}
	
	@Override
	public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        CollapsibleCriteria criteria = getMatchingCriteriaById(graphContainer, searchResult.getId());
        if (criteria != null) {
            criteria.setCollapsed(!criteria.isCollapsed());
            graphContainer.redoLayout();
        }
        
	}

	private org.opennms.features.topology.api.topo.Criteria findCriterion(SearchResult searchResult, GraphContainer container) {
		
		org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
		for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
			if (criterion instanceof IpLikeHopCriteria) {
				
				String ipInterfaceId = ((IpLikeHopCriteria) criterion).getId();
				
				if (ipInterfaceId.equals(searchResult.getId())) {
					return criterion;
				}
			}
		}
		return null;
	}
	

    private static CollapsibleCriteria getMatchingCriteriaById(GraphContainer graphContainer, String id) {
        CollapsibleCriteria[] criteria = VertexHopGraphProvider.getCollapsibleCriteriaForContainer(graphContainer);
        for (CollapsibleCriteria criterium : criteria) {
            if (criterium.getId().equals(id)) {
                return criterium;
            }
        }
        return null;
    }
	
	
}
