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

package org.opennms.features.topology.app.internal.ui;


import com.google.common.base.Function;
import com.google.common.collect.*;
import com.vaadin.ui.AbstractComponent;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxServerRpc;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxState;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.osgi.OnmsServiceManager;

import java.util.*;


public class SearchBox extends AbstractComponent implements SelectionListener, GraphContainer.ChangeListener {

    Multimap<SearchProvider, SearchResult> m_suggestionMap;
    private OperationContext m_operationContext;
    private OnmsServiceManager m_serviceManager;
    SearchBoxServerRpc m_rpc = new SearchBoxServerRpc(){

        private static final long serialVersionUID = 6945103738578953390L;

        @Override
        public void querySuggestions(String query, int indexFrom, int indexTo) {
            if (m_serviceManager != null) {
                getState().setSuggestions(getQueryResults(query));
            }
        }

        @Override
        public void selectSuggestion(SearchSuggestion searchSuggestion) {
            SearchResult searchResult = new SearchResult(searchSuggestion.getId(), searchSuggestion.getNamespace(), searchSuggestion.getLabel());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                if(m_suggestionMap.get(key).contains(searchResult)){
                    key.onSelectSearchResult(searchResult, m_operationContext);

                    break;
                }
            }

            /*FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(m_operationContext.getGraphContainer());
            criteria.add(mapToVertexRef(searchSuggestion));
            m_operationContext.getGraphContainer().redoLayout();*/
        }

        @Override
        public void removeSelected(SearchSuggestion searchSuggestion) {
            SearchResult searchResult = new SearchResult(searchSuggestion.getId(), searchSuggestion.getNamespace(), searchSuggestion.getLabel());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                if(m_suggestionMap.get(key).contains(searchResult)){
                    key.onDeselectSearchResult(searchResult, m_operationContext);

                    break;
                }
            }

            SelectionManager selectionManager = m_operationContext.getGraphContainer().getSelectionManager();
            selectionManager.deselectVertexRefs(Lists.newArrayList(mapToVertexRef(searchSuggestion)));
        }

        @Override
        public void addToFocus(SearchSuggestion searchSuggestion) {
            SearchResult searchResult = new SearchResult(searchSuggestion.getId(), searchSuggestion.getNamespace(), searchSuggestion.getLabel());
            FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(m_operationContext.getGraphContainer());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if(searchResults.contains(searchResult)){
                    //maybe add or change this method to onfocus
                    key.onSelectSearchResult(searchResult, m_operationContext);
                    List<VertexRef> vertexRefsBy = key.getVertexRefsBy(searchResult);
                    criteria.addAll(vertexRefsBy);
                    break;
                }
            }

            getState().getFocused().add(mapToSearchSuggestion(searchResult));
            m_operationContext.getGraphContainer().redoLayout();
        }

        @Override
        public void removeFocused(SearchSuggestion searchSuggestion) {
            SearchResult searchResult = new SearchResult(searchSuggestion.getId(), searchSuggestion.getNamespace(), searchSuggestion.getLabel());
            FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(m_operationContext.getGraphContainer());

            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if(searchResults.contains(searchResult)) {
                    //maybe change method call to on defocus
                    key.onDeselectSearchResult(searchResult, m_operationContext);
                    List<VertexRef> vRefs = key.getVertexRefsBy(searchResult);
                    for (VertexRef vRef : vRefs) {
                        criteria.remove(vRef);
                    }
                    break;
                }
            }

            if (criteria.size() == 0) {
                m_operationContext.getGraphContainer().removeCriteria(criteria);
            }

            getState().getFocused().remove(searchSuggestion);
            m_operationContext.getGraphContainer().redoLayout();

        }

        @Override
        public void centerAndSelectSearchSuggestion(SearchSuggestion searchSuggestion){
            SearchResult searchResult = new SearchResult(searchSuggestion.getId(), searchSuggestion.getNamespace(), searchSuggestion.getLabel());

            List<VertexRef> vRefs = null;
            Multiset<SearchProvider> keys = m_suggestionMap.keys();
            for(SearchProvider key : keys){
                Collection<SearchResult> searchResults = m_suggestionMap.get(key);
                if(searchResults.contains(searchResult)) {
                    key.onSelectSearchResult(searchResult, m_operationContext);
                    vRefs = key.getVertexRefsBy(searchResult);
                    break;
                }
            }

            GraphContainer graphContainer = m_operationContext.getGraphContainer();
            MapViewManager mapViewManager = graphContainer.getMapViewManager();
            mapViewManager.setBoundingBox(graphContainer.getGraph().getLayout().computeBoundingBox(vRefs));
        }

    };

    public SearchBox(OnmsServiceManager serviceManager, OperationContext operationContext) {
        m_serviceManager = serviceManager;
        m_operationContext = operationContext;
        init();
    }

    private List<SearchSuggestion> getQueryResults(final String query) {
        //m_suggestionMap.clear();

        String searchPrefix = getQueryPrefix(query);

        List<SearchProvider> providers = m_serviceManager.getServices(SearchProvider.class, null, new Properties());

        for(SearchProvider provider : providers) {
            if(searchPrefix != null && provider.supportsPrefix(searchPrefix)) {
                String queryOnly = query.replace(searchPrefix, "");
                m_suggestionMap.putAll(provider, provider.query( getSearchQuery(queryOnly) ));
            } else{
                m_suggestionMap.putAll(provider, provider.query(getSearchQuery(query)));
            }

        }

        return mapToSuggestions(Lists.newArrayList(m_suggestionMap.values()));
    }


    private SearchQuery getSearchQuery(String query) {
        SearchQuery searchQuery;
        if(query.equals("*")){
            searchQuery = new AllSearchQuery(query);
        } else{
            searchQuery = new ContainsSearchQuery(query);
        }
        return searchQuery;
    }

    public String getQueryPrefix(String query){
        String prefix = null;
        if(query.contains("=")){
            prefix = query.substring(0, query.indexOf('=') + 1);
        }
        return prefix;
    }

    private List<SearchSuggestion> mapToSuggestions(List<SearchResult> searchResults) {
        return Lists.transform(searchResults, new Function<SearchResult, SearchSuggestion>(){
            @Override
            public SearchSuggestion apply(SearchResult searchResult) {
                return mapToSearchSuggestion(searchResult);
            }
        });

    }

    private List<VertexRef> mapToVertexRefs(List<SearchSuggestion> suggestion){
        return Lists.transform(suggestion, new Function<SearchSuggestion, VertexRef>(){
            @Override
            public VertexRef apply(SearchSuggestion input) {
                return mapToVertexRef(input);
            }
        });
    }

    private VertexRef mapToVertexRef(SearchSuggestion suggestion) {
        return new AbstractVertexRef(suggestion.getNamespace(), suggestion.getId(), suggestion.getLabel());
    }

    private SearchSuggestion mapToSearchSuggestion(SearchResult searchResult) {
        SearchSuggestion suggestion = new SearchSuggestion();
        suggestion.setNamespace(searchResult.getNamespace());
        suggestion.setId(searchResult.getId());
        suggestion.setLabel(searchResult.getLabel());

        return suggestion;
    }

    private boolean checkIfFocused(VertexRef vertexRef) {
        FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(m_operationContext.getGraphContainer());
        return criteria.contains(vertexRef);
    }

    @Override
    protected SearchBoxState getState() {
        return (SearchBoxState) super.getState();
    }

    private void init() {
        registerRpc(m_rpc);
        getState().immediate = true;
        setWidth(250.0f, Unit.PIXELS);
        setImmediate(true);

        m_suggestionMap = HashMultimap.create();
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        List<SearchSuggestion> selected = Lists.newArrayList();

        //List<VertexRef> vertexRefs = Lists.newArrayList(selectionContext.getSelectedVertexRefs());
        //getState().setSelected(mapToSuggestions(vertexRefs));

    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        Criteria[] criterium = graphContainer.getCriteria();
        for(Criteria criteria : criterium){
          //Work needs to be done here for showing Focused nodes
        }


        /*FocusNodeHopCriteria hopCriteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer);

        Set<VertexRef> vertices = hopCriteria.getVertices();
        List<VertexRef> vertexRefs = Lists.newArrayList(vertices);

        getState().setFocused(Lists.transform(vertexRefs, new Function<VertexRef, SearchSuggestion>(){
            @Override
            public SearchSuggestion apply(VertexRef input) {
                SearchSuggestion suggestion = new SearchSuggestion();
                suggestion.setId(input.getId());
                suggestion.setNamespace(input.getNamespace());
                suggestion.setLabel(input.getLabel());
                return suggestion;
            }
        })  );*/

    }

    private class ContainsSearchQuery extends AbstractSearchQuery implements SearchQuery {
        public ContainsSearchQuery(String query) {
            super(query);
        }

        @Override
        public boolean matches(String str) {
            return str.toLowerCase().contains(getQueryString().toLowerCase());
        }
    }

    private class AllSearchQuery extends AbstractSearchQuery{

        public AllSearchQuery(String queryString) {
            super(queryString);
        }

        @Override
        public boolean matches(String str) {
            return true;
        }
    }
}
