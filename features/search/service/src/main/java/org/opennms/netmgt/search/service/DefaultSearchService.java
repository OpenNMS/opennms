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
package org.opennms.netmgt.search.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.SearchService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DefaultSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSearchService.class);

    private final BundleContext bundleContext;

    public DefaultSearchService(final BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public List<SearchResult> query(final SearchQuery query) {
        Objects.requireNonNull(query);
        if (query.getMaxResults() <= 0) {
            query.setMaxResults(SearchQuery.DEFAULT_MAX_RESULT);
        }

        // Enforce minimum length, otherwise don't query
        if (Strings.isNullOrEmpty(query.getInput()) || query.getInput().length() < 1) {
            return Collections.emptyList();
        }

        // Fetch Results grouped by context
        final Map<SearchContext, SearchResult> resultMap = new HashMap<>();
        try {
            final ServiceReference<SearchProvider>[] allServiceReferences = (ServiceReference<SearchProvider>[]) bundleContext.getServiceReferences(SearchProvider.class.getCanonicalName(), null);
            if (allServiceReferences != null) {
                for (ServiceReference<SearchProvider> eachReference : allServiceReferences) {
                    final SearchProvider service = bundleContext.getService(eachReference);
                    if (query.getContext() == null || service.getContext().contributesTo(query.getContext())) {
                        try {
                            final SearchResult providerResult = service.query(query);
                            if (resultMap.containsKey(providerResult.getContext())) {
                                final SearchResult mergedResult = resultMap.get(providerResult.getContext());
                                for (SearchResultItem eachItem : providerResult.getResults()) {
                                    mergedResult.setMore(providerResult.hasMore() || mergedResult.hasMore());
                                    mergedResult.addItem(eachItem);
                                }
                            } else {
                                resultMap.put(providerResult.getContext(), providerResult);
                            }
                        } catch (Exception ex) {
                            LOG.error("Could not execute query for provider", ex);
                        } finally {
                            bundleContext.ungetService(eachReference);
                        }
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Could not fetch search providers", e);
        }

        // Sort and limit each context
        final List<SearchResult> searchResultList = resultMap.values().stream()
                .filter(searchResult -> !searchResult.isEmpty())
                .map(searchResult -> {
                        final List<SearchResultItem> limitedAndSortedItems = searchResult.getResults().stream()
                                .sorted(Comparator.comparing(SearchResultItem::getWeight).reversed().thenComparing(SearchResultItem::getLabel))
                                .limit(query.getMaxResults())
                                .collect(Collectors.toList());
                        return new SearchResult(searchResult.getContext()).withMore(searchResult.hasMore()).withResults(limitedAndSortedItems);
                    }
                )
                .sorted(Comparator.comparingInt(searchResult -> searchResult.getContext().getWeight()))
                .collect(Collectors.toList());
        return searchResultList;
    }
}
