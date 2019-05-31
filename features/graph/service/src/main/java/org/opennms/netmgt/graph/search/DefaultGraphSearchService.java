/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;

public class DefaultGraphSearchService implements GraphSearchService {

    private List<SearchProvider> graphSearchProviders = new ArrayList<>();
    private GraphService graphService;

    public DefaultGraphSearchService(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public List<SearchSuggestion> getSuggestions(String namespace, String input) {

        List<SearchSuggestion> suggestions = new ArrayList<>();
        for(SearchProvider provider : graphSearchProviders){
            if (provider.canSuggest(graphService, namespace)){
                suggestions.addAll(provider.getSuggestions(graphService, namespace, input));
            }
        }
        suggestions.sort(Comparator.naturalOrder());
        return suggestions;
    }

    @Override
    public List<GenericVertex> search(SearchCriteria searchCriteria) {
        List<GenericVertex> results = new ArrayList<>();
        for(SearchProvider provider : graphSearchProviders) {
            // on a logical level there should be only one provider which can resolve the SearchCriteria but the code
            // allows for more than one:
            if (provider.canResolve(searchCriteria.getProviderId())) {
                results.addAll(provider.resolve(graphService, searchCriteria));
            }
        }
        return results;
    }

    public void onBind(SearchProvider graphSearchProvider, Map<String, String> props) {
        graphSearchProviders.add(graphSearchProvider);
    }

    public void onUnbind(SearchProvider graphSearchProvider, Map<String, String> props) {
        graphSearchProviders.remove(graphSearchProvider);
    }
}
