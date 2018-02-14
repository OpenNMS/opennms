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

package org.opennms.features.topology.app.internal;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.HistoryAwareSearchProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.IpLikeHopCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <SearchProvider> API that allows the user to specify
 * an IPLIKE query.  The query itself is returned in the <SearchResult> set as
 * a collapsible selection as well as the "distinct" list of IPs from the DB
 * that are also collapsible since multiple nodes may have the same IP.  The
 * <IpLikeHopCriteria> supports adding all the nodes having the selected IP.
 * 
 * FIXME: Improve the label to support showing how many vertices are supported
 * by each <SearchResult>
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 *
 */
public class IpLikeSearchProvider extends AbstractSearchProvider implements HistoryAwareSearchProvider {

	private final static String CONTRIBUTES_TO_NAMESPACE = "nodes";

	private static Logger LOG = LoggerFactory.getLogger(IpLikeSearchProvider.class);
	
	private IpInterfaceProvider ipInterfaceProvider;

    private static final Pattern m_iplikePattern = Pattern.compile("^[0-9?,\\-*]*\\.[0-9?,\\-*]*\\.[0-9?,\\-*]*\\.[0-9?,\\-*]*$");

    public IpLikeSearchProvider(IpInterfaceProvider ipInterfaceProvider) {
    	this.ipInterfaceProvider = Objects.requireNonNull(ipInterfaceProvider);
	}

    @Override
    public String getSearchProviderNamespace() {
        return IpLikeHopCriteria.NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return CONTRIBUTES_TO_NAMESPACE.equals(namespace);
    }

    /**
     * This method processes the <SearchQuery> that the user has typed and returns a <SearchResult> list
     * of matching IP addresses as well as the query string itself, which is collapsible, to act
     * as a subnet container.
     * 
     */
    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
    	LOG.info("SearchProvider->query: called with search query: '{}'", searchQuery);

        List<SearchResult> results = new ArrayList<>();
        
		String queryString = searchQuery.getQueryString();
		
		if (!isIpLikeQuery(queryString)) {
			LOG.debug("SearchProvider->query: query not IPLIKE compatible.");
			return results;
		}
    	
    	CriteriaBuilder bldr = new CriteriaBuilder(OnmsIpInterface.class);
    	
		bldr.iplike("ipAddr", queryString).orderBy("ipAddress", true);
		Criteria dbQueryCriteria = bldr.toCriteria();
		List<OnmsIpInterface> ips;
		
		//Here to catch query exceptions and avoid unnecessary exception messages in the Vaadin UI
		//Mainly do to not having yet found the perfect iplike regex that supports IPLIKE syntax 
		//for both IPv4 and IPv6 IPLIKE expressions in the isIpLikeQuery method.  The current Pattern
		//test currently fails not catching octets ending in '-' such as '10.7-.*.*' and only supports
		//IPv4 addresses.  This just lets us fail to the underlying IPLIKE stored procedure.  The IPLIKE
		//Utils class might be a good place to have a static method that validates the query string
		//since it seems to do something very similar in its matches method.
        try {
			ips = ipInterfaceProvider.findMatching(dbQueryCriteria);
            if (ips.size() == 0) {
                return results;
            } else {
                if (isIpLikeQuery(queryString)) {
                    LOG.debug("SearchProvider->query: adding IPLIKE search spec '{}' to the search results.", queryString);
                    SearchResult searchResult = new SearchResult(getSearchProviderNamespace(), queryString, queryString,
							queryString, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
                    if (!results.contains(searchResult)) {
						results.add(searchResult);
					}
                }
            }

            Set<String> ipAddrs = new HashSet<>();
            
            LOG.info("SearchProvider->query: creating IP address set.");
            for (OnmsIpInterface ip : ips) {
                String hostAddress = ip.getIpAddress().getHostAddress();
                LOG.debug("SearchProvider->query: adding '{}' to set of IPs.", hostAddress);
                ipAddrs.add(hostAddress);
            }
            
            LOG.info("SearchProvider->query: building search result from set of IPs.");
            IPLOOP: for (String ip : ipAddrs) {
                
                if (findCriterion(ip, graphContainer) != null) {
                    continue IPLOOP;

                } else {
                	SearchResult searchResult = createSearchResult(ip, queryString);
                	if (!results.contains(searchResult)) {
						results.add(searchResult);
					}
                }
            }
            LOG.info("SearchProvider->query: found: '{}' IP interfaces.", ips.size());
            
        } catch (Exception e) {
            LOG.error("SearchProvider-query: caught exception during iplike query: {}", e);
            
        }
		
		LOG.info("SearchProvider->query: built search result with {} results.", results.size());

        return results;
    }

	private SearchResult createSearchResult(String ip, String queryString) {
		SearchResult result = new SearchResult(getSearchProviderNamespace(), ip, ip, queryString, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
		return result;
	}


    /**
     * Validate if the query string has any chance of being an IPLIKE query.
     * 
     * FIXME: validates iplike strings that have an octet ending in '-' such as:
     * 10.7-.*.*
     * 
     * @param queryString
     * @return true if the string contains a '*' or '-' or ',' ".
     */
    private boolean isIpLikeQuery(String queryString) {
        
//        Matcher iplikeMatcher = m_iplikePattern.matcher(queryString);
//        return iplikeMatcher.matches();
        
        boolean validity = false;
        
        int ipv4delimCnt = StringUtils.countMatches(queryString, ".");
        int ipv6delimCnt = StringUtils.countMatches(queryString, ":");
        
        if ((ipv4delimCnt == 3 || ipv6delimCnt == 7) && !StringUtils.endsWith(queryString, "-")) {
            validity = true;
        } else {
            validity = false;
        }
        
        return validity;
    	 
	}

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix(IpLikeHopCriteria.NAMESPACE+"=", searchPrefix);
    }

    //FIXME so that the focus button works.???
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
    	
    	LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
    	org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);
    	
    	Set<VertexRef> vertices = ((IpLikeHopCriteria)criterion).getVertices();
    	LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());
    	
		return vertices;
    	
    }
    
    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
    	LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    	super.onCenterSearchResult(searchResult, graphContainer);
    }
    
    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	LOG.debug("SearchProvider->onFocusSearchResult: called with search result: '{}'", searchResult);
    	super.onFocusSearchResult(searchResult, operationContext);

    }
    
    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	LOG.debug("SearchProvider->onDefocusSearchResult: called with search result: '{}'", searchResult);
    	super.onDefocusSearchResult(searchResult, operationContext);

    }
    
    /**
     * Creates a criteria that provides <VertexRefs> matching the IPLIKE query from the users query
     * stored in the <SearchResult> that was created by this class during the query method.  The SearchResult 
     * and the Criterion use the OnmsIpinterfaceId as the ID for dereferencing in the container.
     */
	@Override
	public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
    	LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

		IpLikeHopCriteria criterion = createCriteria(searchResult);
		container.addCriteria(criterion);
		
        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        
        logCriteriaInContainer(container);

	}

    private void logCriteriaInContainer(GraphContainer container) {
        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (org.opennms.features.topology.api.topo.Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }

	
	@Override
	public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
    	LOG.debug("SearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);
		org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);
		
		if (criterion != null) {
			container.removeCriteria(criterion);
		}
		logCriteriaInContainer(container);
	}
	
	@Override
	public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
    	LOG.debug("SearchProvider->onToggleCollapse: called with search result: '{}'", searchResult);
    	
        CollapsibleCriteria criteria = getMatchingCriteriaById(graphContainer, searchResult.getId());
        if (criteria != null) {
            criteria.setCollapsed(!criteria.isCollapsed());
            graphContainer.redoLayout();
        }
        
	}

	@Override
	public org.opennms.features.topology.api.topo.Criteria buildCriteriaFromQuery(SearchResult input, GraphContainer container) {
		IpLikeHopCriteria criteria = createCriteria(input);
		return criteria;
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
        for (CollapsibleCriteria criterion : criteria) {
            if (criterion.getId().equals(id)) {
                return criterion;
            }
        }
        return null;
    }

	private IpLikeHopCriteria createCriteria(SearchResult searchResult) {
		return new IpLikeHopCriteria(searchResult, this.ipInterfaceProvider);
	}
}
