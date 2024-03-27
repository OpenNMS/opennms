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
