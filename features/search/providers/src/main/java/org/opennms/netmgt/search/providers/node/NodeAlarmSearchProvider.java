/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.providers.SearchResultItemBuilder;

public class NodeAlarmSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;
    private final AlarmDao alarmDao;
    private final EntityScopeProvider entityScopeProvider;

    public NodeAlarmSearchProvider(final NodeDao nodeDao, final AlarmDao alarmDao, final EntityScopeProvider entityScopeProvider) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Node;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final String input = query.getInput();
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class)
                .alias("node", "node")
                .eq("severity", OnmsSeverity.get(input))
                .distinct();

        final List<OnmsAlarm> matchingAlarms = alarmDao.findMatching(criteriaBuilder.limit(query.getMaxResults()).toCriteria());

        final List<Restriction> restrictions = new ArrayList<>();
        for(var alarm : matchingAlarms){
            if(!Objects.isNull(alarm.getNodeId())) {
                restrictions.add(Restrictions.eq("id", alarm.getNodeId().intValue()));
            }
        }

        if (restrictions.size() > 0) {
            criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                    .or(restrictions.toArray(new Restriction[restrictions.size()]))
                    .distinct();
            final int totalCount = nodeDao.countMatching(criteriaBuilder.toCriteria());
            final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteriaBuilder.orderBy("id").limit(query.getMaxResults()).toCriteria());
            final List<SearchResultItem> searchResultItems = matchingNodes.stream().map(node -> {
                final SearchResultItem searchResultItem = new SearchResultItemBuilder().withOnmsNode(node, entityScopeProvider).build();
                searchResultItem.setLabel("Show nodes with severity '" + input + "'");
                return searchResultItem;
            }).collect(Collectors.toList());
            return new SearchResult(Contexts.Node).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        } else {
            return new SearchResult(Contexts.Node).withMore(false).withResults(Collections.emptyList());
        }
    }
}
