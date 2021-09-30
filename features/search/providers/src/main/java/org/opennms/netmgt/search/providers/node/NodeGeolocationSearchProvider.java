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

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.Matcher;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.providers.SearchResultItemBuilder;

import com.google.common.collect.Lists;

public class NodeGeolocationSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;
    private final EntityScopeProvider entityScopeProvider;

    public NodeGeolocationSearchProvider(final NodeDao nodeDao, final EntityScopeProvider entityScopeProvider) {
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
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                .alias("assetRecord", "assetRecord")
                .alias("assetRecord.geolocation", "geolocation")
                .and(
                        Restrictions.isNotNull("assetRecord"),
                        Restrictions.isNotNull("assetRecord.geolocation"),
                        Restrictions.or(
                                Restrictions.ilike("assetRecord.geolocation.address1", QueryUtils.ilike(input)),
                                Restrictions.ilike("assetRecord.geolocation.address2", QueryUtils.ilike(input)),
                                Restrictions.ilike("assetRecord.geolocation.city", QueryUtils.ilike(input)),
                                Restrictions.ilike("assetRecord.geolocation.state", QueryUtils.ilike(input)),
                                Restrictions.ilike("assetRecord.geolocation.zip", QueryUtils.ilike(input)),
                                Restrictions.ilike("assetRecord.geolocation.country", QueryUtils.ilike(input))
                        )
                )
                .distinct();
        final int totalCount = nodeDao.countMatching(criteriaBuilder.toCriteria());
        final Criteria criteria = criteriaBuilder.orderBy("label").limit(query.getMaxResults()).toCriteria();
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchResultItem> results = matchingNodes.stream()
            .map(node -> {
                final SearchResultItem result = new SearchResultItemBuilder().withOnmsNode(node, entityScopeProvider).build();
                final OnmsGeolocation geolocation = node.getAssetRecord().getGeolocation();
                final List<Matcher> matcherList = Lists.newArrayList(
                        new Matcher("Country", geolocation.getCountry()),
                        new Matcher("City", geolocation.getCity()),
                        new Matcher("State", geolocation.getState()),
                        new Matcher("Zip", geolocation.getZip()),
                        new Matcher("Address 1", geolocation.getAddress1()),
                        new Matcher("Address 2", geolocation.getAddress2())
                );
                result.addMatches(matcherList, input);
                return result;
            })
            .collect(Collectors.toList());
        final SearchResult result = new SearchResult(Contexts.Node).withResults(results).withMore(totalCount > results.size());
        return result;
    }
}
