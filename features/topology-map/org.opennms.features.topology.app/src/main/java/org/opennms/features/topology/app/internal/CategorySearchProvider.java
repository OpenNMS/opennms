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
package org.opennms.features.topology.app.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.support.HistoryAwareSearchProvider;
import org.opennms.features.topology.api.support.hops.CriteriaUtils;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.netmgt.model.OnmsCategory;

public class CategorySearchProvider extends AbstractSearchProvider implements HistoryAwareSearchProvider {

    private final CategoryProvider categoryProvider;

    private String m_hiddenCategoryPrefix = null;

    private TopologyService topologyService;

    public CategorySearchProvider(TopologyService topologyService, CategoryProvider categoryProvider) {
        this.topologyService = Objects.requireNonNull(topologyService);
        this.categoryProvider = Objects.requireNonNull(categoryProvider);
    }

    @Override
    public String getSearchProviderNamespace() {
        return CategoryHopCriteria.NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return topologyService.isCategoryAware(namespace);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {

        Collection<OnmsCategory> categories = categoryProvider.getAllCategories();

        List<SearchResult> results = new ArrayList<>();
        for (OnmsCategory category : categories) {
            if (!checkHiddenPrefix(category.getName()) && searchQuery.matches(category.getName())) {
                SearchResult result = new SearchResult(CategoryHopCriteria.NAMESPACE, category.getId().toString(), category.getName(),
                        searchQuery.getQueryString(), SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED);
                CollapsibleCriteria criteria = getMatchingCriteria(graphContainer, category.getName());
                if (criteria != null) {
                    result.setCollapsed(criteria.isCollapsed());
                }
                results.add(result);
            }
        }
        return results;
    }

    private boolean checkHiddenPrefix(String name) {
        if(m_hiddenCategoryPrefix == null || m_hiddenCategoryPrefix.equals("")) return false;
        return name.startsWith(m_hiddenCategoryPrefix);
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix(CategoryHopCriteria.NAMESPACE+"=", searchPrefix);
    }

    //FIXME: Should return the <Set> of <VertexRef> that are associated with <SearchResult>
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        return Collections.emptySet();
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        CategoryHopCriteria criteria = createCriteria(searchResult, container);
        container.addCriteria(criteria);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        CategoryHopCriteria criteria = createCriteria(searchResult, container);
        container.removeCriteria(criteria);
    }

    public void setHiddenCategoryPrefix(String prefix) {
        m_hiddenCategoryPrefix = prefix;
    }

    @Override
    public Criteria buildCriteriaFromQuery(SearchResult input, GraphContainer container) {
        CategoryHopCriteria criteria = createCriteria(input, container);
        return criteria;
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        CollapsibleCriteria criteria = getMatchingCriteria(graphContainer, searchResult.getId());
        if (criteria != null) {
            criteria.setCollapsed(!criteria.isCollapsed());
            graphContainer.redoLayout();
        }
    }

    private static CollapsibleCriteria getMatchingCriteria(GraphContainer graphContainer, String id) {
        CollapsibleCriteria[] criteria = CriteriaUtils.getCollapsibleCriteriaForContainer(graphContainer);
        for (CollapsibleCriteria criterion : criteria) {
            if (criterion.getId().equals(id)) {
                return criterion;
            }
        }
        return null;
    }

    private CategoryHopCriteria createCriteria(SearchResult searchResult, GraphContainer graphContainer) {
        CategoryHopCriteria criteria = new CategoryHopCriteria(searchResult, categoryProvider, graphContainer);
        return criteria;
    }
}