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

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.spotlight.api.Contexts;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchResult;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class NodeSearchProvider implements SearchProvider {

    private final NodeDao nodeDao;

    public NodeSearchProvider(final NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public List<SearchResult> query(String input) {
        final List<Restriction> restrictions = Lists.newArrayList(
                Restrictions.ilike("label", input),
                Restrictions.eq("foreignSource", input),
                Restrictions.eq("foreignId", input));

        // Try if input could be an id
        try {
            int nodeId = Integer.parseInt(input);
            restrictions.add(Restrictions.eq("id", nodeId));
        } catch (NumberFormatException ex) {
            // expected, we ignore it
        }

        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                .or(restrictions.toArray(new Restriction[restrictions.size()]))
                .distinct()
                .orderBy("label")
                .limit(10); // TODO MVR make configurable
        final Criteria criteria = criteriaBuilder.toCriteria();
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchResult> searchResults = matchingNodes.stream().map(n -> {
            final SearchResult searchResult = new SearchResult();
            searchResult.setContext(Contexts.Node);
            searchResult.setIdentifer(new NodeRef(n).asString());
            searchResult.setUrl("element/node.jsp?node=" + n.getId());
            searchResult.setLabel(n.getLabel());
            searchResult.setProperties(ImmutableMap.<String, String>builder()
                    .put("label", n.getLabel())
                    .put("foreignId", n.getForeignId())
                    .put("foreignSource", n.getForeignSource()).build());
            return searchResult;
        }).collect(Collectors.toList());
        return searchResults;
    }
}
