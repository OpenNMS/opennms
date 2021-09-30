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

package org.opennms.smoketest.ui.framework.search.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.WebDriver;

import com.google.common.collect.Maps;

public class ContextSearchResult extends SearchResult {

    private final String context;

    public ContextSearchResult(WebDriver driver, String input, String context) {
        super(driver, input);
        this.context = context;
    }

    @Override
    public List<SearchResultItem> getItems() {
        return getItemsByContext().getOrDefault(context, new ArrayList<>());
    }

    public SearchResultItem getItem(String caption) {
        // Actually check if we have any item for the context
        final SearchResultItem item = getItems()
                .stream()
                .filter(i -> i.getLabel()
                        .contains(caption))
                .findFirst().orElse(null);
        return item;
    }

    public boolean hasMore() {
        final Optional<SearchResultItem> moreItem = getItems().stream().filter(item -> item.getType() == ItemType.More).findAny();
        return moreItem.isPresent();
    }

    public void loadMore() {
        final Optional<SearchResultItem> any = getItems().stream().filter(item -> item.getType() == ItemType.More).findAny();
        if (any.isPresent()) {
            any.get().click();
        }
    }

    private Map<String, List<SearchResultItem>> getItemsByContext() {
        // Group items by context
        String currentContext = null;
        final Map<String, List<SearchResultItem>> contextMap = Maps.newHashMap();
        for (SearchResultItem item : super.getItems()) {
            if (item.getType() == ItemType.Header) {
                // The context may be appended with s to show that there are more elements in the context
                // However for better handling purposes it must be removed
                final String context = item.getLabel().endsWith("s") ? item.getLabel().substring(0, item.getLabel().length() - 1) : item.getLabel();
                contextMap.putIfAbsent(context, new ArrayList<>());
                currentContext = context;
            }
            if (currentContext != null && item.getType() != ItemType.Header) {
                contextMap.get(currentContext).add(item);
            }
        }
        return contextMap;
    }
}
