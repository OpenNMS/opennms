/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.*;
import org.opennms.netmgt.search.providers.SearchResultItemBuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NodeMetaDataSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;
    private final EntityScopeProvider entityScopeProvider;

    public NodeMetaDataSearchProvider(final NodeDao nodeDao, final EntityScopeProvider entityScopeProvider) {
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
                .sql("{alias}.nodeid IN (SELECT m.id FROM node_metadata m WHERE m.key !~ '.*([pP]assword|[sS]ecret).*' AND m.value LIKE '%" + StringEscapeUtils.escapeSql(input) + "%')")
                .distinct();
        final int totalCount = nodeDao.countMatching(criteriaBuilder.toCriteria());
        final Criteria criteria = criteriaBuilder.orderBy("label").distinct().limit(query.getMaxResults()).toCriteria();

        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchResultItem> searchResultItems = matchingNodes.stream().map(node -> {
            final SearchResultItem searchResultItem = new SearchResultItemBuilder().withOnmsNode(node, entityScopeProvider).build();
            for (OnmsMetaData onmsMetaData : node.getMetaData()) {
                if (onmsMetaData.getValue() != null && !onmsMetaData.getKey().matches(".*([pP]assword|[sS]ecret).*") && onmsMetaData.getValue().contains(input)) {
                    searchResultItem.addMatch(new Match(onmsMetaData.getContext() + ":" + onmsMetaData.getKey(), "Meta-Data '" + onmsMetaData.getContext() + ":" + onmsMetaData.getKey() + "'", onmsMetaData.getValue()));
                    break;
                }
            }
            searchResultItem.setWeight(100);
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Node).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        return searchResult;
    }

}
