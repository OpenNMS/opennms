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

package org.opennms.features.topology.api.topo;

import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;

/**
 * 
 * @author Donald Desloge
 * 
 * API for Topology map search engine (Providers)
 * 
 * NOTE: The implementations of this API will only ever receive <SearchResult> objects that 
 * were created by the implementation.
 *
 */
public interface SearchProvider {
	
	/**
	 * This is a namespace for search providers...
	 *
	 * @return
	 */
    String getSearchProviderNamespace();
    
    
    
    /**
     * This is the topology provider namespace to which this search provider contributes.
     * 
     * @param namespace
     * @return returns true if the provider support the <@param namespace>.
     */
    boolean contributesTo(String namespace);
    
    
    
    
    /**
     * This API is for getting a list of <SearchResult> from all the search providers.
     * 
     * @param searchQuery This is an API for Vaadin UI code for implementing various search queries types.
     * @param graphContainer
     * @return A list of SearchResults
     */
    List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer);
    
    
    
    
    /**
     * This method is called from the UI when the user wants to add a vertex that is in the current context
     * to the focus list.
     * 
     * @param searchResult
     * @param operationContext
     */
    void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext);
    
    
    
    /**
     * This method is called from the UI when the user wants to remove a vertex that currently in the focus list.
     * 
     * @param searchResult
     * @param operationContext
     */
    void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext);
    
    
    
    /**
     * This method is called by topology app the user is searching to setup prefixes that will only match from 
     * a <SearchProvider>
     * 
     * @param searchPrefix
     * @return
     */
    boolean supportsPrefix(String searchPrefix);
    
    
    /**
     * This method is called when the topology UI needs to know what <VertexRef> are associated with
     * a particular <SearchResult>.
     * 
     * @param searchResult
     * @param graphContainer 
     * @return
     */
    Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer graphContainer);
    
    
    
    /**
     * This method is called when the selection is made from the list of <SearchResult> returned by 
     * a <SearchProvider>query() method of this API.
     * 
     * It is the criteria's responsibility is to determine the <VertexRefs> associated 
     * with the passed <SearchResult> to the <GraphContainer> reference passed to this method.
     * 
     * @param searchResult
     * @param container
     */
    void addVertexHopCriteria(SearchResult searchResult, GraphContainer container);
    
    

    /**
     * When the user requests that the current SearchResult be removed from focus, the search provider should remove the <Criteria> from the <GraphContainer>
     * @param searchResult
     * @param container
     */
    void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container);
    
    
    
    /**
     * This method called when the user clicks the focus icon on the <SearchResult>.  No implementation is required unless you want to enhance 
     * the zoom behavior.  This call will be followed by a call to getVertexRefsBy.
     * 
     * @param searchResult
     * @param graphContainer
     */
    void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer);

    
    /**
     * This method is called if the <SearchResult> provided by the implementation of this API is flagged as collapsible and the user has clicked the
     * toggle icon.  This requires that the <Criteria> implementation created by the implemenation of this API should be able to implement the 
     * <CollapsibleCriteria> API.  The set of <VertexRefs> provided by the Criteria will not change, however, the criteria should be able to
     * provide the both the collapsed and uncollapsed representations of the SearchResult. 
     *  
     * @param searchResult
     * @param graphContainer
     */
    void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer);
}
