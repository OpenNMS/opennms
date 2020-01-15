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

package org.opennms.netmgt.search.providers.action;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.UrlUtils;

public class CategorySearchProvider implements SearchProvider {

    private final CategoryDao categoryDao;

    public CategorySearchProvider(final CategoryDao categoryDao) {
        this.categoryDao = Objects.requireNonNull(categoryDao);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(SearchQuery query) {
        final String input = query.getInput();
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsCategory.class)
                .ilike("name", QueryUtils.ilike(input))
                .orderBy("name")
                .distinct();
        final int totalCount = categoryDao.countMatching(builder.toCriteria());
        final Criteria criteria = builder.limit(query.getMaxResults()).toCriteria();
        final List<OnmsCategory> matchingResult = categoryDao.findMatching(criteria);
        final List<SearchResultItem> searchResultItems = matchingResult.stream().map(category -> {
            final SearchResultItem searchResultItem = new SearchResultItem();
            searchResultItem.setIdentifier(category.getId().toString());
            searchResultItem.setLabel("Show nodes in category '" + category.getName() + "'");
            searchResultItem.setUrl("element/nodeList.htm?category1=" + UrlUtils.encode(category.getName()));
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        return searchResult;
    }
}
