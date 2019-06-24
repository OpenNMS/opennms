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

package org.opennms.netmgt.spotlight.providers.node;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.spotlight.api.Match;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.providers.Query;
import org.opennms.netmgt.spotlight.providers.SearchResultBuilder;

public class NodeServiceSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;

    public NodeServiceSearchProvider(final NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public List<SearchResult> query(String input) {
        final Criteria criteria = new CriteriaBuilder(OnmsNode.class)
                .alias("ipInterfaces", "ipInterfaces", Alias.JoinType.INNER_JOIN)
                .alias("ipInterfaces.monitoredServices", "monitoredServices", Alias.JoinType.INNER_JOIN)
                .alias("monitoredServices.serviceType", "serviceType", Alias.JoinType.INNER_JOIN)
                .ilike("serviceType.name", Query.ilike(input))
                .distinct()
                .orderBy("label")
                .limit(10) // TODO MVR make configurable
                .toCriteria();
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchResult> searchResults = matchingNodes.stream().map(node -> {
            final SearchResult searchResult = new SearchResultBuilder().withOnmsNode(node).build();
            node.getIpInterfaces().stream()
                    .flatMap(ipInterface -> ipInterface.getMonitoredServices().stream())
                    .filter(service -> Query.matches(service.getServiceName(), input))
                    .forEach(service -> searchResult.addMatch(new Match("service.name", "Monitored Service", service.getServiceName())));
            return searchResult;
        }).collect(Collectors.toList());
        return searchResults;
    }
}
