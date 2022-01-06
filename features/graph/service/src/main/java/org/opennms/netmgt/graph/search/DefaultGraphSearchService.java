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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGraphSearchService implements GraphSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphSearchService.class);

    private final static int MIN_CHAR_FOR_SEARCH = 3;

    private Map<String, SearchProvider> graphSearchProviders = new HashMap<>();
    private GraphService graphService;

    public DefaultGraphSearchService(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public List<SearchSuggestion> getSuggestions(String namespace, String input) {
        Objects.requireNonNull(namespace);

        if (input == null || input.length() < MIN_CHAR_FOR_SEARCH) {
            return Collections.emptyList();
        }

        final SearchContext context = SearchContext.builder()
                .graphService(graphService)
                .suggestionsLimit(10)
                .build();

        // Remove duplicates (same label and same context but different provider):
        final Set<SearchSuggestion> suggestions = new TreeSet<>(Comparator.comparing(SearchSuggestion::getLabel)
                .thenComparing(SearchSuggestion::getContext));

        for(SearchProvider provider : graphSearchProviders.values()){
            if (provider.canSuggest(graphService, namespace)){
                List<SearchSuggestion> suggestionsOfProvider = provider.getSuggestions(context, namespace, input);
                if(suggestionsOfProvider == null) {
                    LOG.warn("Provider {} does not work properly, received null suggestion list.", provider.getProviderId());
                } else {
                    // If not implemented properly, the search provider may return a null value.
                    // Those are removed here to prevent anny issues later
                    List<SearchSuggestion> excludeNullResults = suggestionsOfProvider.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    if (excludeNullResults.size() != suggestionsOfProvider.size()) {
                        LOG.warn("Provider {} returned a null value. Please ensure it is implemented correctly", provider.getProviderId());
                    }
                    suggestions.addAll(excludeNullResults);
                }
            }
        }
        return new ArrayList<>(suggestions);
    }

    @Override
    public List<GenericVertex> search(SearchCriteria searchCriteria) {
        SearchProvider provider = graphSearchProviders.get(searchCriteria.getProviderId());
        Objects.requireNonNull(provider, "Could not find provider with id=" + searchCriteria.getProviderId());
        Objects.requireNonNull(searchCriteria.getCriteria());
        return provider.resolve(graphService, searchCriteria);
    }

    public void onBind(SearchProvider graphSearchProvider, Map<String, String> props) {
        graphSearchProviders.put(graphSearchProvider.getProviderId(), graphSearchProvider);
    }

    public void onUnbind(SearchProvider graphSearchProvider, Map<String, String> props) {
        if (graphSearchProvider != null) {
            graphSearchProviders.remove(graphSearchProvider.getProviderId());
        }
    }
}
