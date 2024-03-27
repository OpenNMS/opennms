/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.search.providers;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.mate.api.EntityScopeProvider;
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
