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
package org.opennms.netmgt.search.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the complete result set of a search for a given {@link SearchContext}.
 *
 * @author mvrueden
 */
public class SearchResult {

    public static SearchResult EMPTY = new SearchResult("$EMPTY$");

    private final SearchContext context;
    private final List<SearchResultItem> results = new ArrayList<>();
    private boolean more;

    public SearchResult(String context) {
        this(new SearchContext(Objects.requireNonNull(context)));
    }

    public SearchResult(SearchContext searchContext) {
        this.context = Objects.requireNonNull(searchContext);
    }

    public void addItem(SearchResultItem item) {
        Objects.requireNonNull(item);
        final Optional<SearchResultItem> existingItem = results.stream().filter(r -> r.getIdentifier().equals(item.getIdentifier())).findAny();
        if (existingItem.isPresent()) {
            existingItem.get().merge(item);
        } else {
            results.add(item);
        }
    }

    public SearchContext getContext() {
        return context;
    }

    public boolean hasMore() {
        return more;
    }

    public List<SearchResultItem> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public boolean isMore() {
        return more;
    }

    public SearchResult withResults(List<SearchResultItem> searchResultItems) {
        this.results.addAll(searchResultItems);
        return this;
    }

    public SearchResult withMore(boolean hasMore) {
        this.more = hasMore;
        return this;
    }

    public SearchResult withMore(Collection<?> totalList, Collection<?> subList) {
        withMore(totalList.size() > subList.size());
        return this;
    }

}