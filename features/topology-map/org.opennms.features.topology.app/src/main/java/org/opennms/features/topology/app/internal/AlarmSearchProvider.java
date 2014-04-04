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
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.AlarmHopCriteria;
import org.opennms.features.topology.app.internal.support.AlarmHopCriteriaFactory;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <SearchProvider> API that allows the user to specify
 * an Alarm query.  The query itself is returned in the <SearchResult> set as
 * a collapsible selection as well as the "distinct" list of Alarms from the DB
 * that are also collapsible since multiple nodes may have the same Severity or UEI.  The
 * <AlarmHopCriteria> supports adding all the nodes having the selected UEI or Severity.
 * 
 * TODO: Improve the label to support showing how many vertices are supported
 * TODO: Add support for severity searching and collapsing
 * 
 * FIXME:  
 * by each <SearchResult>
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 *
 */
public class AlarmSearchProvider extends AbstractSearchProvider implements SearchProvider {

	private static Logger LOG = LoggerFactory.getLogger(AlarmSearchProvider.class);
	
    private AlarmDao m_AlarmDao;
    private AlarmHopCriteriaFactory m_alarmHopFactory;

	public AlarmSearchProvider(AlarmDao dao) {
    	m_AlarmDao = dao;
    	m_alarmHopFactory = new AlarmHopCriteriaFactory(m_AlarmDao);
    }

    @Override
    public String getSearchProviderNamespace() {
        return AlarmHopCriteria.NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }
    
    public class AlarmSearchResult extends SearchResult {
        
        private Integer m_alarmId;
        private Integer m_nodeId;
        private String m_alarmQuery;
        private String m_nodeLabel;
        
        
        public Integer getAlarmId() {
            return m_alarmId;
        }
        
        public void setAlarmId(Integer alarmId) {
            m_alarmId = alarmId;
        }
        
        public Integer getNodeId() {
            return m_nodeId;
        }
        
        public void setNodeId(Integer nodeId) {
            m_nodeId = nodeId;
        }
        
        public String getAlarmQuery() {
            return m_alarmQuery;
        }
        
        public void setAlarmQuery(String query) {
            m_alarmQuery = query;
        }
        
        public String getNodeLabel() {
            return m_nodeLabel;
        }
        
        public void setNodeLabel(String nodeLabel) {
            m_nodeLabel = nodeLabel;
        }
        
        public AlarmSearchResult(SearchResult result) {
            this(Integer.valueOf(result.getId()), null, result.getLabel(), result.getLabel());
        }
        
        public AlarmSearchResult (Integer alarmId, Integer nodeId, String alarmQuery, String nodeLabel) {
            
            //TODO: Verify that every search result should have the nodeId and the nodeLabel used
            //to set the id and label on the base <SearchResult> class.
            super(getSearchProviderNamespace(), String.valueOf(alarmId), nodeLabel);
            
            this.setAlarmId(alarmId);
            this.setNodeId(nodeId);
            this.setAlarmQuery(alarmQuery);
            this.setNodeLabel(nodeLabel);
        }
        
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

        List<SearchResult> results = new ArrayList<SearchResult>();
        
		String queryString = searchQuery.getQueryString();
		
		if (!isAlarmQuery(queryString)) {
			LOG.debug("SearchProvider->query: query not Alarm compatible.");
			return results;
		}
    	
    	CriteriaBuilder bldr = new CriteriaBuilder(OnmsAlarm.class);
    	
    	bldr.ilike("uei", "%"+queryString+"%").orderBy("node");
    	
		Criteria dbQueryCriteria = bldr.toCriteria();
		List<OnmsAlarm> alarms;
		
		//Here to catch query exceptions and avoid unnecessary exception messages in the Vaadin UI
		//Mainly do to not having yet found the perfect iplike regex that supports IPLIKE syntax 
		//for both IPv4 and IPv6 IPLIKE expressions in the isIpLikeQuery method.  The current Pattern
		//test currently fails not catching octets ending in '-' such as '10.7-.*.*' and only supports
		//IPv4 addresses.  This just lets us fail to the underlying IPLIKE stored procedure.  The IPLIKE
		//Utils class might be a good place to have a static method that validates the query string
		//since it seems to do something very similar in its matches method.
        try {
            alarms = m_AlarmDao.findMatching(dbQueryCriteria);
            if (alarms.size() == 0) {
                return results;
                
            //FIXME: make this work the criteria is going to have to support alarmId and alarm severity
//            } else {
//                if (isAlarmQuery(queryString)) {
//                    LOG.debug("SearchProvider->query: adding Alarm search spec '{}' to the search results.", queryString);
//                    SearchResult searchResult = new SearchResult(getSearchProviderNamespace(), queryString, queryString);
//                    searchResult.setCollapsed(true);
//                    searchResult.setCollapsible(true);
//                    results.add(searchResult);
//                }
            }

            Set<AlarmSearchResult> queryResults = new HashSet<AlarmSearchResult>();
            
            LOG.info("SearchProvider->query: creating IP address set.");
            for (OnmsAlarm alarm : alarms) {
                String nodeLabel = alarm.getNodeLabel();
                LOG.debug("SearchProvider->query: adding '{}' to set of nodes.", nodeLabel);
                
                AlarmSearchResult result = new AlarmSearchResult(alarm.getId(), alarm.getNodeId(), queryString, alarm.getNodeLabel());
                queryResults.add(result);
                
            }
            
            LOG.info("SearchProvider->query: building search result from set of alarms.");
            IPLOOP: for (AlarmSearchResult result : queryResults) {
                
                if (findCriterion(String.valueOf(result.getAlarmId()), graphContainer) != null) {
                    continue IPLOOP;

                } else {
                    results.add(result);
                }
            }
            
            LOG.info("SearchProvider->query: found: '{}' alarms.", alarms.size());
            
        } catch (Exception e) {
            LOG.error("SearchProvider-query: caught exception during alarm query: {}", e);
            
        }
		
		LOG.info("SearchProvider->query: built search result with {} results.", results.size());
        return results;
    }
    

    /**
     * Validate if the query string has any chance of being an Alarm query.
     * 
     * @param queryString
     * @return true if the string is found in logmsg, description, UEI or severity
     */
    private boolean isAlarmQuery(String queryString) {
        
        return true;
        
	}

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix("alarm=", searchPrefix);
    }

    //FIXME so that the focus button works.???
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
    	
    	LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
    	org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);
    	
    	Set<VertexRef> vertices = ((AlarmHopCriteria)criterion).getVertices();
    	LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());
    	
		return vertices;
    	
    }
    
    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
    	// TODO Auto-generated method stub
    	LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    	super.onCenterSearchResult(searchResult, graphContainer);
    }
    
    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	// TODO Auto-generated method stub
    	LOG.debug("SearchProvider->onFocusSearchResult: called with search result: '{}'", searchResult);
    	super.onFocusSearchResult(searchResult, operationContext);

    }
    
    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	LOG.debug("SearchProvider->onDefocusSearchResult: called with search result: '{}'", searchResult);
    	super.onDefocusSearchResult(searchResult, operationContext);

    }
    
    /**
     * Creates a criteria that provides <VertexRefs> matching the Alarm query from the users query
     * stored in the <SearchResult> that was created by this class during the query method.  The SearchResult 
     * and the Criterion use the AlarmID as the ID for dereferencing in the container.
     */
	@Override
	public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
	    LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

	    CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
	    builder.eq("id", Integer.valueOf(searchResult.getId()));
	    List<OnmsAlarm> alarms = m_AlarmDao.findMatching(builder.toCriteria());
	    
	    OnmsAlarm alarm = alarms.get(0);

	    AlarmSearchResult result = new AlarmSearchResult(searchResult);
	    
	    result.setAlarmId(Integer.valueOf(searchResult.getId()));
	    result.setNodeId(alarm.getNodeId());
	    result.setAlarmQuery(searchResult.getLabel());
	    result.setNodeLabel(searchResult.getLabel());

	    AlarmHopCriteria criterion = m_alarmHopFactory.createCriteria(result);

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
		org.opennms.features.topology.api.topo.Criteria criterian = findCriterion(searchResult.getId(), container);
		
		if (criterian != null) {
			container.removeCriteria(criterian);
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

	private org.opennms.features.topology.api.topo.Criteria findCriterion(String alarmId, GraphContainer container) {
		
		org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
		for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
			if (criterion instanceof AlarmHopCriteria) {
				
				String id = ((AlarmHopCriteria) criterion).getId();
				
				if (id.equals(alarmId)) {
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
