/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
