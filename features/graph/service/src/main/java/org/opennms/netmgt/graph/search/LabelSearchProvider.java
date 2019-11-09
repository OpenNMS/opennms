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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelSearchProvider implements SearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LabelSearchProvider.class);

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return true; // every vertex has a label, therefore we can search any graph
    }

    @Override
    public List<SearchSuggestion> getSuggestions(SearchContext searchContext, String namespace, String input) {
        Objects.requireNonNull(input);
        return getVerticesOfGraph(searchContext.getGraphService(), namespace)
                .stream()
                .filter(v -> v.getLabel() != null && v.getLabel().toLowerCase().contains(input.toLowerCase()))
                .map(v -> new SearchSuggestion(getProviderId(), GenericVertex.class.getSimpleName(), v.getLabel(), v.getLabel()))
                .limit(searchContext.getSuggestionsLimit())
                .collect(Collectors.toList());
    }

    @Override
    public List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
        final List<GenericVertex> vertices = getVerticesOfGraph(graphService, searchCriteria.getNamespace())
                .stream()
                .filter(v -> v.getLabel() != null && v.getLabel().toLowerCase().contains(searchCriteria.getCriteria().toLowerCase()))
                .collect(Collectors.toList());
        return vertices;
    }

    private List<GenericVertex> getVerticesOfGraph(GraphService graphService, String namespace) {
        GenericGraph graph = graphService.getGraph(namespace);
        List<GenericVertex> result;
        if (graph != null) {
            result = graph.getVertices();
        } else {
            LOG.warn("Could not find graph for namespace {}", namespace);
            result = Collections.emptyList();
        }
        return result;
    }
}
