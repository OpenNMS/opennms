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
import java.util.HashSet;
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
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpSearchProvider extends AbstractSearchProvider implements SearchProvider {

    private IpInterfaceDao m_ipInterfaceDao;
    private IpLikeHopCriteriaFactory m_ipLikeHopFactory;

	public IpSearchProvider(IpInterfaceDao ipInterfaceDao) {
    	m_ipInterfaceDao = ipInterfaceDao;
    	m_ipLikeHopFactory = new IpLikeHopCriteriaFactory(m_ipInterfaceDao);
    }

    @Override
    public String getSearchProviderNamespace() {
        return IpLikeHopCriteria.NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }

    /**
     * This method processes the <SearchQuery> that the user has typed and returns a <SearchResult> list
     * of matching IP addresses as well as the query string itself, which is collapsible, to act
     * as a subnet container.
     * 
     */
    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
    	
		String queryString = searchQuery.getQueryString();
    	Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Query: '{}'", queryString);
    	
    	CriteriaBuilder bldr = new CriteriaBuilder(OnmsIpInterface.class);
    	
		bldr.iplike("ipAddress", queryString).orderBy("ipAddress", true);
		Criteria dbQueryCriteria = bldr.toCriteria();
		List<OnmsIpInterface> ips = m_ipInterfaceDao.findMatching(dbQueryCriteria);
		logger.info("Query found: '{}' IP interfaces.", ips.size());
		
        List<SearchResult> results = new ArrayList<SearchResult>();

		if (ips.size() == 0) {
			return results;
		} else {
			if (isIpLikeQuery(queryString)) {
				logger.debug("Adding iplike search spec to the search results.");
				SearchResult searchResult = new SearchResult(getSearchProviderNamespace(), queryString, queryString);
				searchResult.setCollapsed(false);
				searchResult.setCollapsible(true);
				results.add(searchResult);
			}
		}

		Set<String> ipAddrs = new HashSet<String>();
		
		logger.info("Creating IP address set.");
		for (OnmsIpInterface ip : ips) {
			logger.debug("Adding '{}' to set of IPs.");
			ipAddrs.add(ip.getIpAddress().getHostAddress());
		}
		
		logger.info("Building search result from set of IPs.");
		IPLOOP: for (String ip : ipAddrs) {
			
			if (findCriterion(ip, graphContainer) != null) {
				continue IPLOOP;

			} else {
				results.add(createSearchResult(ip));

			}
		}
		
        return results;
    }

	private SearchResult createSearchResult(String ip) {
		SearchResult result = new SearchResult(getSearchProviderNamespace(), ip, ip);
		result.setCollapsible(true);
		return result;
	}


    /**
     * Simple way to know if the query the returned ipinterface results from the DB was
     * based on an IPLIKE query.
     * 
     * @param queryString
     * @return true if the string contains a '*' or '-' or ',' ".
     */
    private boolean isIpLikeQuery(String queryString) {
    	
    	if (queryString.contains("*")) {
    		return true;
    	} else if (queryString.contains("-")) {
    		return true;
    	} else if (queryString.contains(",")) {
    		return true;
    	}
    	
		return false;
	}

	@Override
    public boolean supportsPrefix(String searchPrefix) {
        return false;
    }

    //FIXME so that the focus button works.
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
    	
    	org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);
    	
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
		String id = searchResult.getId();
		criteria.setId(id);
		container.addCriteria(criteria);
	}

	
	@Override
	public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
		
		org.opennms.features.topology.api.topo.Criteria criterian = findCriterion(searchResult.getId(), container);
		
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

	private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {
		
		org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
		for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
			if (criterion instanceof IpLikeHopCriteria) {
				
				String id = ((IpLikeHopCriteria) criterion).getId();
				
				if (id.equals(resultId)) {
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
