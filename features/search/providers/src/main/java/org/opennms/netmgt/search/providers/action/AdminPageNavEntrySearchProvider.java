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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.web.navigate.PageNavEntry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Allows to search for dynamically added {@link org.opennms.web.navigate.PageNavEntry}s.
 *
 * @author mvrueden
 */
public class AdminPageNavEntrySearchProvider implements SearchProvider {

    private final BundleContext bundleContext;

    public AdminPageNavEntrySearchProvider(final BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final List<PageNavEntry> totalMatchingEntries = getPageNavEntries(query);
        final List<SearchResultItem> resultItems = totalMatchingEntries.stream().map(e -> {
                final SearchResultItem searchResultItem = new SearchResultItem();
                searchResultItem.setLabel(e.getName());
                searchResultItem.setIdentifier(e.getUrl());
                searchResultItem.setUrl(e.getUrl());
                searchResultItem.setWeight(10); // Admin actions should show up at the top
                return searchResultItem;
            }
        ).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action)
                .withMore(totalMatchingEntries, resultItems)
                .withResults(resultItems);
        return searchResult;
    }

    private List<PageNavEntry> getPageNavEntries(final SearchQuery query) {
        Objects.requireNonNull(query);
        final List<PageNavEntry> totalMatches = Lists.newArrayList();
        try {
            final Collection<ServiceReference<PageNavEntry>> serviceReferences = bundleContext.getServiceReferences(PageNavEntry.class, "(Page=admin)");
            if (serviceReferences != null) {
                for (ServiceReference<PageNavEntry> serviceReference : serviceReferences) {
                    try {
                        final PageNavEntry pageNavEntry = bundleContext.getService(serviceReference);
                        if (QueryUtils.matches(pageNavEntry.getName(), query.getInput())) {
                            totalMatches.add(pageNavEntry);
                        }
                    } finally {
                        bundleContext.ungetService(serviceReference);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            LoggerFactory.getLogger(getClass()).error("An error occurred while performing the search: {}", e.getMessage(), e);
        }
        return totalMatches;
    }
}
