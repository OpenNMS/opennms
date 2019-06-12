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

package org.opennms.netmgt.graph.provider.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.model.OnmsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSearchProvider implements SearchProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationSearchProvider.class);

    private final ApplicationDao applicationDao;

    public ApplicationSearchProvider(ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), ApplicationGraph.TOPOLOGY_NAMESPACE);
    }

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return ApplicationGraph.TOPOLOGY_NAMESPACE.equals(namespace);
    }

    @Override
    public List<SearchSuggestion> getSuggestions(final SearchContext searchContext,
                                                 final String namespace, final String input) {
        Objects.requireNonNull(searchContext);
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(input);

        LOG.debug("ApplicationSearchProvider->getSuggestions: called with search query: '{}'", input);

        CriteriaBuilder bldr = new CriteriaBuilder(OnmsApplication.class);
        bldr.ilike("name", String.format("%%%s%%", input));
        bldr.orderBy("name", true);
        bldr.limit(searchContext.getSuggestionsLimit());
        Criteria dbQueryCriteria = bldr.toCriteria();

        List<SearchSuggestion> suggestions = new ArrayList<>();
        for (OnmsApplication application : applicationDao.findMatching(dbQueryCriteria)) {
            SearchSuggestion suggestion = new SearchSuggestion(getProviderId(),
                    OnmsApplication.class.getSimpleName(),
                    application.getName());
            suggestions.add(suggestion);
        }
        LOG.debug("ApplicationServiceSearchProvider->getSuggestions: found {} results: {}", suggestions.size(), suggestions);
        return suggestions;
    }

    @Override
    public List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {

        return getVerticesOfGraph(graphService, searchCriteria.getNamespace())
                .stream()
                .map(ApplicationVertex::new)
                .filter(v -> filter(v, searchCriteria.getCriteria()))
                .map(ApplicationVertex::asGenericVertex)
                .collect(Collectors.toList());
    }

    private boolean filter(ApplicationVertex vertex, String input) {
        if (vertex.getName() != null) {
            return vertex.getName().contains(input);
        }
        return false;
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
