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
