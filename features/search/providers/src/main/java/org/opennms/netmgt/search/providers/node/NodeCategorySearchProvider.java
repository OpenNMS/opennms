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

package org.opennms.netmgt.search.providers.node;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.Match;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.providers.SearchResultItemBuilder;

public class NodeCategorySearchProvider implements SearchProvider {

    private final NodeDao nodeDao;
    private final EntityScopeProvider entityScopeProvider;

    public NodeCategorySearchProvider(final NodeDao nodeDao, final EntityScopeProvider entityScopeProvider) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Node;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final String input = query.getInput();
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                .alias("categories", "categories", Alias.JoinType.INNER_JOIN)
                .ilike("categories.name", QueryUtils.ilike(input))
                .distinct();
        final int totalCount = nodeDao.countMatching(criteriaBuilder.toCriteria());
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteriaBuilder.orderBy("label").limit(query.getMaxResults()).toCriteria());
        final List<SearchResultItem> searchResultItems = matchingNodes.stream().map(node -> {
            final SearchResultItem searchResultItem = new SearchResultItemBuilder().withOnmsNode(node, entityScopeProvider).build();
            node.getCategories().stream()
                    .filter(c -> QueryUtils.matches(c.getName(), input))
                    .forEach(c -> searchResultItem.addMatch(new Match("category.name", "Category", c.getName())));
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Node).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        return searchResult;
    }
}
