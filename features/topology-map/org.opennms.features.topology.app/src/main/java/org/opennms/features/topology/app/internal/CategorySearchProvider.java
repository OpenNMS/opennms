/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;


import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteria;
import org.opennms.features.topology.app.internal.support.CategoryHopCriteriaFactory;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;

public class CategorySearchProvider extends AbstractSearchProvider implements SearchProvider {

    private CategoryHopCriteriaFactory m_categoryHopFactory;
    private CategoryDao m_categoryDao;
    private String m_hiddenCategoryPrefix = null;

    public CategorySearchProvider(CategoryDao categoryDao, NodeDao nodeDao){
        m_categoryDao = categoryDao;
        
        //Not sure why we have to do this...
        m_categoryHopFactory = new CategoryHopCriteriaFactory(categoryDao, nodeDao);
    }

    @Override
    public String getSearchProviderNamespace() {
        return "category";
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {

        Collection<OnmsCategory> categories = m_categoryDao.findAll();

        List<SearchResult> results = new ArrayList<SearchResult>();
        for (OnmsCategory category : categories) {
            if (!checkHiddenPrefix(category.getName()) && searchQuery.matches(category.getName())) {
                SearchResult result = new SearchResult("category", category.getId().toString(), category.getName(), searchQuery.getQueryString());
                result.setCollapsible(true);
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
        return supportsPrefix("category=", searchPrefix);
    }

    //FIXME: Should return the <Set> of <VertexRef> that are associated with <SearchResult>
    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        return Collections.emptySet();
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        CategoryHopCriteria criteria = m_categoryHopFactory.getCriteria(searchResult.getLabel());
        criteria.setId(searchResult.getId());
        container.addCriteria(criteria);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        CategoryHopCriteria c = m_categoryHopFactory.getCriteria(searchResult.getLabel());
        c.setId(searchResult.getId());
        container.removeCriteria(c);
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public void setHiddenCategoryPrefix(String prefix) {
        m_hiddenCategoryPrefix = prefix;
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
        CollapsibleCriteria[] criteria = VertexHopGraphProvider.getCollapsibleCriteriaForContainer(graphContainer);
        for (CollapsibleCriteria criterium : criteria) {
            if (criterium.getId().equals(id)) {
                return criterium;
            }
        }
        return null;
    }
}
