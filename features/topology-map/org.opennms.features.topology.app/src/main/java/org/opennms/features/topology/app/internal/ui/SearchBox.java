/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui;


import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchQuery;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxServerRpc;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxState;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;
import org.opennms.osgi.OnmsServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.vaadin.ui.AbstractComponent;


public class SearchBox extends AbstractComponent implements SelectionListener, GraphContainer.ChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SearchBox.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Multimap<SearchProvider, SearchResult> m_suggestionMap;
    private OperationContext m_operationContext;
    private OnmsServiceManager m_serviceManager;
    SearchBoxServerRpc m_rpc = new SearchBoxServerRpc() {

        private static final long serialVersionUID = 6945103738578953390L;
        
        @Override
        public void querySuggestions(String query, int indexFrom, int indexTo) {
            LOG.debug("SearchBox->querySuggestions: called with query: {}", query);
            if (m_serviceManager != null) {
                getState().setSuggestions(getQueryResults(query));
            }
        }

        @Override
        public void selectSuggestion(SearchSuggestion searchSuggestion) {
            
            LOG.debug("SearchBox->selectSuggestion: called with searchSuggestion: {}", searchSuggestion);

            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for (SearchProvider key : keys ) {
                if (m_suggestionMap.get(key).contains(searchResult)) {
                    //key.onFocusSearchResult(searchResult, m_operationContext);

                    break;
                }
            }
        }

        @Override
        public void removeSelected(SearchSuggestion searchSuggestion) {
            LOG.debug("SearchBox->removeSelected: called with searchSuggestion: {}", searchSuggestion);

            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                if(m_suggestionMap.get(key).contains(searchResult)){
                    break;
                }
            }

            SelectionManager selectionManager = m_operationContext.getGraphContainer().getSelectionManager();
            selectionManager.deselectVertexRefs(Lists.newArrayList(mapToVertexRef(searchSuggestion)));
        }

        @Override
        public void addToFocus(SearchSuggestion searchSuggestion) {
            LOG.debug("SearchBox->addToFocus: called with searchSuggestion: {}", searchSuggestion);
            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());

            Multiset<SearchProvider> searchProviders = m_suggestionMap.keys();
            for(SearchProvider eachSearchProvider : searchProviders){
                Collection<SearchResult> searchResults = m_suggestionMap.get(eachSearchProvider);
                if(searchResults.contains(searchResult)){
                    eachSearchProvider.onFocusSearchResult(searchResult, m_operationContext);
                    eachSearchProvider.addVertexHopCriteria(searchResult, m_operationContext.getGraphContainer());
                    break;
                }
            }

            m_operationContext.getGraphContainer().redoLayout();
        }

        @Override
        public void removeFocused(SearchSuggestion searchSuggestion) {
            LOG.debug("SearchBox->removeFocused: called with searchSuggestion: {}", searchSuggestion);
            
            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());

            Multiset<SearchProvider> providers = m_suggestionMap.keys();
            
            LOG.debug("SearchBox->removeFocused: suggestionMap size is: {}", m_suggestionMap.size());
            
            for (SearchProvider key : providers) {
                //LOG.debug("SearchBox->removeFocused: iterating keys with key: {}", key);
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if (searchResults.contains(searchResult)) {
                    //LOG.debug("SearchBox->removeFocused: found searchResult '{}' in searchResults.", searchResult);
                    key.onDefocusSearchResult(searchResult, m_operationContext);
                    key.removeVertexHopCriteria(searchResult, m_operationContext.getGraphContainer());

                    break;
                }
            }

            removeIfSuggMapEmpty(searchResult, m_operationContext.getGraphContainer());
            removeIfSpecialURLCase(searchResult, m_operationContext.getGraphContainer());
            m_operationContext.getGraphContainer().redoLayout();
        }

        @Override
        public void centerSearchSuggestion(SearchSuggestion searchSuggestion) {
            LOG.debug("SearchBox->centerSearchSuggestion: called with searchSuggestion: {}", searchSuggestion);

            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());

            Set<VertexRef> vRefs = new TreeSet<>();
            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if(searchResults.contains(searchResult)) {
                    key.onCenterSearchResult(searchResult, m_operationContext.getGraphContainer());
                    vRefs.addAll(key.getVertexRefsBy(searchResult, m_operationContext.getGraphContainer()));
                    break;
                }
            }

            // Hack for now, change to a better way.
            VertexHopCriteria criteria = VertexHopGraphProvider.getWrappedVertexHopCriteria(m_operationContext.getGraphContainer());
            DefaultVertexRef vertexRef = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
            if(criteria.getVertices().contains(vertexRef)){
                vRefs.add(vertexRef);
            }
            GraphContainer graphContainer = m_operationContext.getGraphContainer();
            MapViewManager mapViewManager = graphContainer.getMapViewManager();
            mapViewManager.setBoundingBox(graphContainer.getGraph().getLayout().computeBoundingBox(vRefs));
        }

        @Override
        public void toggleSuggestionCollapse(SearchSuggestion searchSuggestion) {
            LOG.debug("SearchBox->toggleSuggestionCollapse: called with searchSuggestion: {}", searchSuggestion);
            
            SearchResult searchResult = new SearchResult(searchSuggestion.getNamespace(), searchSuggestion.getId(), searchSuggestion.getLabel(),
                    searchSuggestion.getQuery(), searchSuggestion.isCollapsible(), searchSuggestion.isCollapsed());
            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if(searchResults.contains(searchResult)) {
                    key.onToggleCollapse(searchResult, m_operationContext.getGraphContainer());
                    break;
                }
            }

            if(m_suggestionMap.size() == 0){
                collapseIfSuggMapEmpty(searchResult, m_operationContext.getGraphContainer());
            }
        }
    };

    public SearchBox(OnmsServiceManager serviceManager, OperationContext operationContext) {
        m_serviceManager = serviceManager;
        m_operationContext = operationContext;
        setImmediate(true);
        init();
    }

    private void removeIfSuggMapEmpty(SearchResult searchResult, GraphContainer graphContainer) {
        Criteria[] criterias = graphContainer.getCriteria();
        for (Criteria criteria : criterias) {
            if (criteria == null || !(criteria instanceof VertexHopCriteria)) {
                continue;
            }
            VertexHopCriteria crit = (VertexHopCriteria) criteria;

            String critNameSpace = crit.getNamespace();
            if (critNameSpace == null) {
                continue;
            }

            String critId = crit.getId();
            if (critId == null) {
                continue;
            }

            String critLabel = crit.getLabel();
            if (critLabel == null) {
                continue;
            }

            String resultNameSpace = searchResult.getNamespace();
            String resultId = searchResult.getId();
            String resultLabel = searchResult.getLabel();
            if (critNameSpace.equals(resultNameSpace) && critId.equals(resultId) && critLabel.equals(resultLabel)) {
                graphContainer.removeCriteria(crit);
            }
        }
    }

    private void collapseIfSuggMapEmpty(SearchResult searchResult, GraphContainer graphContainer) {
        //A special check for categories that were added then after re-login can't collapse
        boolean isDirty = false;
        Criteria[] criteria = graphContainer.getCriteria();
        for (Criteria criterion : criteria) {
            if (criterion instanceof CollapsibleCriteria) {
                if (((CollapsibleCriteria) criterion).getLabel().equals(searchResult.getLabel())) {
                    ((CollapsibleCriteria) criterion).setCollapsed(!((CollapsibleCriteria) criterion).isCollapsed());
                    isDirty = true;
                }
            }
        }
        if (isDirty) {
            graphContainer.redoLayout();
        }
    }

    private void removeIfSpecialURLCase(SearchResult searchResult, GraphContainer graphContainer) {
        DefaultVertexRef vertexRef = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        DefaultVertexHopCriteria criteria = new DefaultVertexHopCriteria(vertexRef);
        graphContainer.removeCriteria(criteria);
    }


    public List<SearchSuggestion> getQueryResults(final String query) {
        LOG.debug("SearchBox->getQueryResults: called with query {}", query);

        String namespace = m_operationContext.getGraphContainer().getTopologyServiceClient().getNamespace();

        List<SearchResult> results = Lists.newArrayList();

        if (m_serviceManager == null) {
            return mapToSuggestions(results);
        }

        List<SearchProvider> providers = m_serviceManager.getServices(SearchProvider.class, null, new Hashtable<>());
        LOG.debug("SearchBox->getQueryResults: service manager reports {} SearchProviders.", providers.size());


        for (SearchProvider provider : providers) {

            if (provider.getSearchProviderNamespace().equals(namespace) || provider.contributesTo(namespace)) {

                if (provider.supportsPrefix(query)) {
                    // If there is an '=' divider, strip it off. Otherwise, use an empty query string
                    String queryOnly = query.indexOf('=') > 0 ? query.substring(query.indexOf('=') + 1) : "";
                    List<SearchResult> q = provider.query(getSearchQuery(queryOnly), m_operationContext.getGraphContainer());
                    results.addAll(q);

                    if (m_suggestionMap.containsKey(provider)) {
                        m_suggestionMap.get(provider).addAll(q);

                    } else {
                        m_suggestionMap.putAll(provider, q);
                    }

                } else {
                    List<SearchResult> q = provider.query(getSearchQuery(query), m_operationContext.getGraphContainer());
                    results.addAll(q);
                    if (m_suggestionMap.containsKey(provider)) {
                        m_suggestionMap.get(provider).addAll(q);
                    } else {
                        m_suggestionMap.putAll(provider, q);
                    }
                }
            }

        }

        return mapToSuggestions(results);
    }


    private static SearchQuery getSearchQuery(String query) {
        if("*".equals(query) || "".equals(query)){
            return new AllSearchQuery(query);
        } else {
            return new ContainsSearchQuery(query);
        }
    }

    private static List<SearchSuggestion> mapToSuggestions(List<SearchResult> searchResults) {
        return Lists.transform(searchResults, new Function<SearchResult, SearchSuggestion>(){
            @Override
            public SearchSuggestion apply(SearchResult searchResult) {
                return mapToSearchSuggestion(searchResult);
            }
        });

    }

    private static VertexRef mapToVertexRef(SearchSuggestion suggestion) {
        return new DefaultVertexRef(suggestion.getNamespace(), suggestion.getId(), suggestion.getLabel());
    }

    private static SearchSuggestion mapToSearchSuggestion(SearchResult searchResult) {
        SearchSuggestion suggestion = new SearchSuggestion(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        suggestion.setCollapsible(searchResult.isCollapsible());
        suggestion.setCollapsed(searchResult.isCollapsed());
        suggestion.setQuery(searchResult.getQuery());
        return suggestion;
    }

    @Override
    protected SearchBoxState getState() {
        return (SearchBoxState) super.getState();
    }

    private void init() {
        registerRpc(m_rpc);
        getState().immediate = true;
        setWidth(100, Unit.PERCENTAGE);
        setImmediate(true);

        m_suggestionMap = HashMultimap.create();
        updateTokenFieldList(m_operationContext.getGraphContainer());
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        //List<SearchSuggestion> selected = Lists.newArrayList();

        //List<VertexRef> vertexRefs = Lists.newArrayList(selectionContext.getSelectedVertexRefs());
        //getState().setSelected(mapToSuggestions(vertexRefs));

    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        updateTokenFieldList(graphContainer);
    }

    private void updateTokenFieldList(GraphContainer graphContainer) {
        final List<SearchSuggestion> suggestions = Lists.newArrayList();
        final Criteria[] criterium = graphContainer.getCriteria();
        for (Criteria criteria : criterium) {
            try {
                CollapsibleCriteria crit = (CollapsibleCriteria) criteria;
                SearchSuggestion suggestion = new SearchSuggestion(crit.getNamespace(), crit.getId(), crit.getLabel());
                suggestion.setCollapsible(true);
                suggestion.setCollapsed(crit.isCollapsed());
                suggestions.add(suggestion);
                continue;
            } catch (ClassCastException e) {}

            try {
                VertexHopCriteria crit = (VertexHopCriteria) criteria;
                SearchSuggestion suggestion = new SearchSuggestion(crit.getNamespace(), crit.getId(), crit.getLabel());
                suggestions.add(suggestion);
                continue;
            } catch (ClassCastException e) {}
        }

        getState().setFocused(suggestions);
    }

    private static class ContainsSearchQuery extends AbstractSearchQuery implements SearchQuery {
        public ContainsSearchQuery(String query) {
            super(query);
        }

        @Override
        public boolean matches(String str) {
            return str.toLowerCase().contains(getQueryString().toLowerCase());
        }
    }

    private static class AllSearchQuery extends AbstractSearchQuery{

        public AllSearchQuery(String queryString) {
            super(queryString);
        }

        @Override
        public boolean matches(String str) {
            return true;
        }
    }
}