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

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class NodeSearchProvider implements SearchProvider {

    protected static final String CONTEXT = "Node";

    private final NodeDao nodeDao;

    public NodeSearchProvider(final NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return true; // at the moment all vertices are NodeRefAware.
    }

    @Override
    public List<SearchSuggestion> getSuggestions(SearchContext searchContext, String namespace, String input) {
        final Criteria criteria = new CriteriaBuilder(OnmsNode.class)
                .ilike("label", "%" + input + "%")
                .orderBy("label", true)
                .limit(searchContext.getSuggestionsLimit())
                .toCriteria();
        final List<OnmsNode> matchingNodes = nodeDao.findMatching(criteria);
        final List<SearchSuggestion> suggestions = Lists.newArrayList();
        for (OnmsNode eachNode : matchingNodes) {
            final SearchSuggestion suggestion = new SearchSuggestion(
                    getProviderId(),
                    CONTEXT,
                    Integer.toString(eachNode.getId()),
                    eachNode.getLabel()
            );
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    @Override
    public List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
        final OnmsNode node = nodeDao.get(searchCriteria.getCriteria());
        final NodeRef nodeRef = NodeRef.from(node.getId(), node.getForeignSource(), node.getForeignId());
        final List<GenericVertex> vertices = graphService.getGraph(searchCriteria.getNamespace()).resolveVertices(nodeRef);
        return vertices;
    }
}
