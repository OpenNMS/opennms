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
package org.opennms.netmgt.search.providers.action;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.web.api.MenuProvider;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.navigate.MenuEntry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This {@link SearchProvider} allows searching all clickable menu entries and presents them as a search result.
 *
 * @author mvrueden
 */
public class MenuActionSearchProvider implements SearchProvider {

    // Cache for the menu. Uses the user's name as key
    private final LoadingCache<PrincipalCacheKey, List<MenuEntry>> cache;

    // MenuProvider to receive the menu
    private final MenuProvider menuProvider;

    public MenuActionSearchProvider(MenuProvider menuProvider) {
        this.menuProvider = Objects.requireNonNull(menuProvider);
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<PrincipalCacheKey, List<MenuEntry>>() {
                    @Override
                    public List<MenuEntry> load(PrincipalCacheKey cacheKey) throws Exception {
                        final Predicate<MenuEntry> menuFilter = e -> e.getEntries() == null || e.getEntries().isEmpty() && e.getDisplayStatus() == DisplayStatus.DISPLAY_LINK;
                        final List<MenuEntry> menu = menuProvider.getMenu((MenuContext) cacheKey);
                        final List<MenuEntry> actualMenuItems = menu.stream().filter(menuFilter).collect(Collectors.toList());
                        menu.removeAll(actualMenuItems);

                        final List<MenuEntry> otherTopLevelEntries = menu.stream().flatMap(e -> e.getEntries().stream()).filter(menuFilter).collect(Collectors.toList());
                        actualMenuItems.addAll(otherTopLevelEntries);
                        return actualMenuItems;
                    }
                });
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final List<MenuEntry> searchableMenuItems = getSearchableMenuItems(query);
        final List<MenuEntry> menuEntries = searchableMenuItems.stream().filter(item -> QueryUtils.matches(item.getName(), query.getInput())).collect(Collectors.toList());
        final List<MenuEntry> subList = QueryUtils.shrink(menuEntries, query.getMaxResults());
        final List<SearchResultItem> resultItems = subList.stream().map(entry -> {
            final SearchResultItem searchResultItem = new SearchResultItem();
            searchResultItem.setIdentifier(entry.getUrl());
            searchResultItem.setUrl(entry.getUrl());
            searchResultItem.setLabel(String.format("Open %s", entry.getName()));
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action)
                .withMore(menuEntries, resultItems)
                .withResults(resultItems);
        return searchResult;
    }

    private List<MenuEntry> getSearchableMenuItems(final SearchQuery query) {
        return cache.getUnchecked(new PrincipalCacheKey(query));
    }

}
