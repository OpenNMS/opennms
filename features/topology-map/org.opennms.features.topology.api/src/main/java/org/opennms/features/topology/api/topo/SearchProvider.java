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
