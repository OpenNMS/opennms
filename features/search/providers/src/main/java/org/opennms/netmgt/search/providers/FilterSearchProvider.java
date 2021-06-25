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

package org.opennms.netmgt.search.providers;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.slf4j.LoggerFactory;

public class FilterSearchProvider implements SearchProvider {

    private final FilterDao filterDao;
    private final NodeDao nodeDao;
    private final EntityScopeProvider entityScopeProvider;

    public FilterSearchProvider(final FilterDao filterDao, final NodeDao nodeDao, final EntityScopeProvider entityScopeProvider) {
        this.filterDao = Objects.requireNonNull(filterDao);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Node;
    }

    @Override
    public SearchResult query(SearchQuery query) {
        final String input = query.getInput();
        try {
            filterDao.validateRule(input);
            final SortedMap<Integer, String> nodeMap = filterDao.getNodeMap(input);
            final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                    .in("id", nodeMap.keySet())
                    .orderBy("label")
                    .distinct()
                    .limit(query.getMaxResults());
            final Criteria criteria = criteriaBuilder.toCriteria();
            final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
            final List<SearchResultItem> searchResultItems = matchingNodes.stream()
                    .map(node -> new SearchResultItemBuilder()
                            .withOnmsNode(node, entityScopeProvider)
                            .withMatch("filter.criteria", "Filter Criteria", input).build())
                    .collect(Collectors.toList());
            return new SearchResult(Contexts.Node).withResults(searchResultItems).withMore(nodeMap.size() > matchingNodes.size());
        } catch (FilterParseException ex) {
            LoggerFactory.getLogger(getClass()).debug("Cannot parse expression: {}: {}", query.getInput(), ex.getMessage());
        }
        return SearchResult.EMPTY;
    }
}
