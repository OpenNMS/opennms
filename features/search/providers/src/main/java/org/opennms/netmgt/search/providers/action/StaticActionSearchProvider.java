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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Searches entries in <code>${OPENNMS_HOME}/etc/search-actions.xml</code>.
 *
 * @author mvrueden
 */
public class StaticActionSearchProvider implements SearchProvider {

    private final LoadingCache<PrincipalCacheKey, Actions> cache;

    public StaticActionSearchProvider() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<PrincipalCacheKey, Actions>() {
                    @Override
                    public Actions load(PrincipalCacheKey key) throws Exception {
                        final Path etc = Paths.get(System.getProperty("opennms.home"), "etc", "search-actions.xml");
                        final Actions actions = JAXB.unmarshal(etc.toFile(), Actions.class);
                        return actions;
                    }
                });
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(SearchQuery query) {
        Objects.requireNonNull(query.getPrincipal());
        final PrincipalCacheKey cacheKey = new PrincipalCacheKey(query);
        final Actions actions = cache.getUnchecked(cacheKey);
        final String input = query.getInput();
        final List<SearchResultItem> allItemsForUser = actions.getActions().stream()
                .filter(action -> action.getPrivilegedRoles().isEmpty() || action.getPrivilegedRoles().stream().anyMatch(query::isUserInRole))
                .filter(action -> QueryUtils.matches(action.getLabel(), input) || QueryUtils.matches(action.getAliases(), input))
                .sorted(Comparator.comparing(Action::getLabel))
                .map(action -> {
                    final SearchResultItem searchResultItem = new SearchResultItem();
                    // If the label matches, use the label
                    if (QueryUtils.matches(action.getLabel(), input)) {
                        searchResultItem.setLabel(action.getLabel());
                    } else {
                        // Otherwise at least one alias matched, use it as label
                        searchResultItem.setLabel(QueryUtils.getFirstMatch(action.getAliases(), input));
                    }
                    searchResultItem.setUrl(action.getUrl());
                    searchResultItem.setIdentifier(action.getUrl());
                    if (!Strings.isNullOrEmpty(action.getIcon())) {
                        searchResultItem.setIcon(action.getIcon());
                    }
                    if (action.getWeight() != 0) {
                        searchResultItem.setWeight(action.getWeight());
                    } else if (action.getPrivilegedRoles().contains("ROLE_ADMIN")) {
                        searchResultItem.setWeight(10); // Admin actions should be on top of the list
                    }
                    return searchResultItem;
                })
                .collect(Collectors.toList());
        final List<SearchResultItem> searchResultItems = QueryUtils.shrink(allItemsForUser, query.getMaxResults());
        final SearchResult searchResult = new SearchResult(Contexts.Action)
                .withMore(allItemsForUser, searchResultItems)
                .withResults(searchResultItems);
        return searchResult;
    }
}
