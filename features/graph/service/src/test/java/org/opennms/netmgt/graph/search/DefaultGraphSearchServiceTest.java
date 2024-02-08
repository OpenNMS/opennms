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
package org.opennms.netmgt.graph.search;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;

public class DefaultGraphSearchServiceTest {

    @Test
    public void shouldMakeSearchSuggestionsUnique() {
        GraphService graphService = Mockito.mock(GraphService.class);
        DefaultGraphSearchService service = new DefaultGraphSearchService(graphService);
        service.onBind(createGraphSearchProvider("provider1"), new HashMap<>());
        service.onBind(createGraphSearchProvider("provider2"), new HashMap<>());
        List<SearchSuggestion> suggestions = service.getSuggestions("blah", "blub");
        assertEquals(3, suggestions.size());
        assertEquals("label0", suggestions.get(0).getLabel());
        assertEquals("label2", suggestions.get(2).getLabel());
    }

    private SearchProvider createGraphSearchProvider(final String providerId) {
        return new SearchProvider() {

            @Override
            public boolean canSuggest(GraphService graphService, String namespace) {
                return true;
            }

            @Override
            public List<SearchSuggestion> getSuggestions(SearchContext searchContext, String namespace, String input) {
                List<SearchSuggestion> suggestions = new ArrayList<>();
                for(int i= 0; i < 3; i++){
                    suggestions.add(new SearchSuggestion(providerId, "context", "id" + i, "label" + i));
                }
                return suggestions;
            }

            @Override
            public String getProviderId(){
                return providerId;
            }

            @Override
            public List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
                return new ArrayList<>();
            }
        };
    }
}
