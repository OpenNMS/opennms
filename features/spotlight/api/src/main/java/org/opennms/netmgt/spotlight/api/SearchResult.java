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

package org.opennms.netmgt.spotlight.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchResult {

    public static SearchResult EMPTY = new SearchResult("$EMPTY$");

    private final SearchContext context;
    private int totalCount;
    private final List<SearchResultItem> results = new ArrayList<>();

    public SearchResult(String context) {
        this(new SearchContext(Objects.requireNonNull(context)));
    }

    public SearchResult(SearchContext searchContext) {
        this.context = Objects.requireNonNull(searchContext);
    }

    public void addItem(SearchResultItem item) {
        this.results.add(item);
    }

    public SearchContext getContext() {
        return context;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<SearchResultItem> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public SearchResult withResults(List<SearchResultItem> searchResultItems) {
        this.results.addAll(searchResultItems);
        return this;
    }

    public SearchResult withTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public void merge(SearchResultItem mergeMe) {
        Objects.requireNonNull(mergeMe);
        final Optional<SearchResultItem> existingItem = results.stream().filter(r -> r.getUrl().equals(mergeMe.getUrl())).findAny();// TODO MVR should be identifier instead
        if (existingItem.isPresent()) {
            // Merge attributes
            mergeMe.getProperties().putAll(existingItem.get().getProperties());
            // Merge Matches
            mergeMe.getMatches().forEach(m -> existingItem.get().addMatch(m));
        } else {
            results.add(mergeMe);
        }
    }
}