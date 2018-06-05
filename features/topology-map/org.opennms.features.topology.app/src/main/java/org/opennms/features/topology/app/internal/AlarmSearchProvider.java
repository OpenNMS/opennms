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

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.HistoryAwareSearchProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.AlarmHopCriteria;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
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
 * TODO: Add support for severity searching and collapsing by each <SearchResult>
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 *
 */
public class AlarmSearchProvider extends AbstractSearchProvider implements HistoryAwareSearchProvider {

	private final static String CONTRIBUTES_TO_NAMESPACE = "nodes";

	private static Logger LOG = LoggerFactory.getLogger(AlarmSearchProvider.class);

	private AlarmProvider alarmProvider;

	public AlarmSearchProvider(AlarmProvider alarmProvider) {
		this.alarmProvider = Objects.requireNonNull(alarmProvider);
	}

	@Override
    public String getSearchProviderNamespace() {
        return AlarmHopCriteria.NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return CONTRIBUTES_TO_NAMESPACE.equals(namespace);
    }

	@Override
	public Criteria buildCriteriaFromQuery(SearchResult input, GraphContainer container) {
		AlarmHopCriteria criteria = createCriteria(input);
		return criteria;
	}

	public class AlarmSearchResult extends SearchResult {
        
        private Integer m_alarmId;
        private String m_nodeLabel;
        private boolean m_severityQuery = false;
        
        
        public Integer getAlarmId() {
            return m_alarmId;
        }
        
        public void setAlarmId(Integer alarmId) {
            m_alarmId = alarmId;
        }
        
        public String getNodeLabel() {
            return m_nodeLabel;
        }
        
        public void setNodeLabel(String nodeLabel) {
            m_nodeLabel = nodeLabel;
        }
        
        public AlarmSearchResult (Integer alarmId, String nodeLabel, String query, boolean collapsed) {
            super(getSearchProviderNamespace(), String.valueOf(alarmId), nodeLabel, query, SearchResult.COLLAPSIBLE, collapsed);
            
            this.setAlarmId(alarmId);
            this.setNodeLabel(nodeLabel);
        }
        
        public AlarmSearchResult(String id) {
            super(getSearchProviderNamespace(), id, id, id, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
        }

        public AlarmSearchResult(SearchResult searchResult) {
            super(getSearchProviderNamespace(), searchResult.getId(), searchResult.getLabel(), searchResult.getQuery(),
					searchResult.isCollapsible(), searchResult.isCollapsed());
            this.setAlarmId(Integer.valueOf(searchResult.getId()));
            this.setNodeLabel(searchResult.getLabel());
            this.setSeverityQuery(false);
        }

        public boolean isSeverityQuery() {
            return m_severityQuery ;
        }
        
        public void setSeverityQuery(boolean isSeverityQuery) {
            m_severityQuery = isSeverityQuery;
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
    	LOG.info("SearchProvider.query: called with search query: '{}'", searchQuery);

        final List<SearchResult> results = new ArrayList<>();
		final String queryString = searchQuery.getQueryString();
		if (!isAlarmQuery(queryString)) {
			LOG.debug("SearchProvider.query: query not Alarm compatible.");
			return results;
		}
		
		List<OnmsAlarm> alarms = findAlarms(results, queryString);
		if (!alarms.isEmpty()) {
            try {
                LOG.debug("SearchProvider.query: building search result from set of alarms.");
                Set<AlarmSearchResult> queryResults = buildResults(queryString, alarms);

                //Put the alarm search results into a compatible result set for the graph container
                for (AlarmSearchResult result : queryResults) {
                    if (findCriterion(result, graphContainer) == null) { // if not already added, add
                        results.add(result);
                    }
                }
                LOG.debug("SearchProvider.query: found: '{}' alarms.", alarms.size());
            } catch (Exception e) {
                LOG.error("SearchProvider-query: caught exception during alarm query: {}", e);
            }
        }

		LOG.info("SearchProvider.query: built search result with {} results.", results.size());
		return results;
    }

    private Set<AlarmSearchResult> buildResults(final String queryString, List<OnmsAlarm> alarms) {
        Set<AlarmSearchResult> queryResults = new HashSet<>();

        LOG.debug("SearchProvider.query: creating alarm results set.");
        for (OnmsAlarm alarm : alarms) {
            String nodeLabel = alarm.getNodeLabel();
            LOG.debug("SearchProvider.query: adding '{}' to set of results.", nodeLabel);

            AlarmSearchResult result = new AlarmSearchResult(alarm.getId(), alarm.getNodeLabel(), queryString, !SearchResult.COLLAPSED);
            queryResults.add(result);
        }
        return queryResults;
    }

    private List<OnmsAlarm> findAlarms(final List<SearchResult> results, final String queryString) {
        CriteriaBuilder bldr = new CriteriaBuilder(OnmsAlarm.class);
        
        OnmsSeverity severity = OnmsSeverity.get(queryString);
        
        List<OnmsAlarm> alarms;
        
        if (!OnmsSeverity.INDETERMINATE.equals(severity)) {
            bldr = new CriteriaBuilder(OnmsAlarm.class);
            bldr.eq("severity", severity);
            alarms = alarmProvider.findMatchingAlarms(bldr.toCriteria());

            if (alarms.size() > 0) {
                AlarmSearchResult result = new AlarmSearchResult(queryString);
                result.setSeverityQuery(true);
                results.add(result);
            }
        } else {
            
            bldr.isNotNull("node").ilike("uei", "%"+queryString+"%").orderBy("node");
            alarms = alarmProvider.findMatchingAlarms(bldr.toCriteria());

        }
        return alarms;
    }
    

    /**
     * Validate if the query string has any chance of being an Alarm query.
     * 
     * @param queryString
     * @return true if the string is found in logmsg, description, UEI or severity
     */
    private boolean isAlarmQuery(String queryString) {
        //TODO: find a way to throttle the queries
        if (queryString.length() > 3) {
            return true;
        } else {
            return false;
        }
	}

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix(AlarmHopCriteria.NAMESPACE+"=", searchPrefix);
    }

    //FIXME so that the focus button works.???
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
    	LOG.debug("SearchProvider.getVertexRefsBy: called with search result: '{}'", searchResult);
    	Criteria criterion = findCriterion(searchResult, container);
    	
    	Set<VertexRef> vertices = ((AlarmHopCriteria)criterion).getVertices();
    	LOG.debug("SearchProvider.getVertexRefsBy: found '{}' vertices.", vertices.size());
		return vertices;
    	
    }
    
    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
    	LOG.trace("SearchProvider.onCenterSearchResult: called with search result: '{}'", searchResult);
    	super.onCenterSearchResult(searchResult, graphContainer);
    }
    
    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	LOG.trace("SearchProvider.onFocusSearchResult: called with search result: '{}'", searchResult);
    	super.onFocusSearchResult(searchResult, operationContext);

    }
    
    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
    	LOG.debug("SearchProvider.onDefocusSearchResult: called with search result: '{}'", searchResult);
    	super.onDefocusSearchResult(searchResult, operationContext);

    }
    
    /**
     * Creates a criteria that provides <VertexRefs> matching the Alarm query from the users query
     * stored in the <SearchResult> that was created by this class during the query method.  The SearchResult 
     * and the Criterion use the AlarmID as the ID for dereferencing in the container.
     */
	@Override
	public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider.addVertexHopCriteria: called with search result: '{}'", searchResult);
        
        AlarmSearchResult aResult = new AlarmSearchResult(searchResult);
        
        String id = searchResult.getId();
        String query = searchResult.getQuery();
        
        if (!OnmsSeverity.get(query).equals(OnmsSeverity.INDETERMINATE)) {
            aResult.setSeverityQuery(true);
        } else {
            aResult.setAlarmId(Integer.valueOf(id));
            aResult.setSeverityQuery(false);
        }

        container.addCriteria(new AlarmHopCriteria(aResult, alarmProvider));

        LOG.debug("SearchProvider.addVertexHop: adding hop criteria {}.", new AlarmHopCriteria(aResult, alarmProvider));
	}

	@Override
	public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
    	LOG.debug("SearchProvider.removeVertexHopCriteria: called with search result: '{}'", searchResult);
		Criteria criterion = findCriterion(searchResult, container);

		if (criterion != null) {
			container.removeCriteria(criterion);
		}
	}
	
	@Override
	public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
    	LOG.debug("SearchProvider.onToggleCollapse: called with search result: '{}'", searchResult);
    	
        CollapsibleCriteria criteria = getMatchingCriteriaById(graphContainer, searchResult.getId());
        if (criteria != null) {
            criteria.setCollapsed(!criteria.isCollapsed());
            graphContainer.redoLayout();
        }
	}

	//FIXME: Should only allow there to be one criteria in the container that matches a single node
	private AlarmHopCriteria findCriterion(SearchResult result, GraphContainer container) {
		org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
		for (Criteria criterion : criteria) {
			if (criterion instanceof AlarmHopCriteria) {
				AlarmSearchResult searchResult = ((AlarmHopCriteria) criterion).getSearchResult();
				if (searchResult.equals(result)) {
					return (AlarmHopCriteria) criterion;
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

    private AlarmHopCriteria createCriteria(SearchResult searchResult) {
		return new AlarmHopCriteria(new AlarmSearchResult(searchResult), alarmProvider);
	}
}