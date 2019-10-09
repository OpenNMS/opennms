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

package org.opennms.netmgt.spotlight.providers.action;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.opennms.netmgt.spotlight.api.Contexts;
import org.opennms.netmgt.spotlight.api.SearchContext;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchQuery;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.api.SearchResultItem;
import org.opennms.netmgt.spotlight.providers.QueryUtils;
import org.opennms.web.api.MenuProvider;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.navigate.MenuEntry;

/**
 * This {@link SearchProvider} allows searching all clickable menu entries and presents them as a search result.
 *
 * @author mvrueden
 */
// TODO MVR here we can optimize a bit, as we always load the menu entry
public class MenuActionSearchProvider implements SearchProvider {

    private MenuProvider menuProvider;

    public MenuActionSearchProvider(MenuProvider menuProvider) {
        this.menuProvider = Objects.requireNonNull(menuProvider);
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
            searchResultItem.setContext(Contexts.Action);
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
        final MenuContext context = new MenuContext() {
            @Override public String getLocation() { return null; }
            @Override public boolean isUserInRole(String role) {  return query.isUserInRole(role); }
        };

        final Predicate<MenuEntry> menuFilter = e -> e.getEntries() == null || e.getEntries().isEmpty() && e.getDisplayStatus() == DisplayStatus.DISPLAY_LINK;
        final List<MenuEntry> menu = menuProvider.getMenu(context);
        final List<MenuEntry> actualMenuItems = menu.stream().filter(menuFilter).collect(Collectors.toList());
        menu.removeAll(actualMenuItems);

        final List<MenuEntry> otherTopLevelEntries = menu.stream().flatMap(e -> e.getEntries().stream()).filter(menuFilter).collect(Collectors.toList());
        actualMenuItems.addAll(otherTopLevelEntries);
        return actualMenuItems;
    }
}
