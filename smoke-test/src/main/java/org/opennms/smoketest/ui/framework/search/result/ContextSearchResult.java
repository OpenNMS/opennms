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
