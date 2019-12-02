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
