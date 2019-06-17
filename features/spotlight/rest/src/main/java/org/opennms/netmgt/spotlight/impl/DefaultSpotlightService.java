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

package org.opennms.netmgt.spotlight.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.spotlight.api.SearchContext;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.api.SpotlightService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.google.common.base.Strings;

public class DefaultSpotlightService implements SpotlightService {

    private final BundleContext bundleContext;

    public DefaultSpotlightService(final BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public List<SearchResult> query(String query) {
        // TODO MVR use searchContext
        final SearchContext searchContext = new SearchContext();
        final List<SearchResult> totalResult = new ArrayList<>();

        // Enforce minimum length, otherwise don't query
        if (Strings.isNullOrEmpty(query) || query.length() < 1) { // TODO MVR length?
            return totalResult;
        }
        try {
            final ServiceReference<SearchProvider>[] allServiceReferences = (ServiceReference<SearchProvider>[]) bundleContext.getServiceReferences(SearchProvider.class.getCanonicalName(), null);
            if (allServiceReferences != null) {
                for (ServiceReference<SearchProvider> eachReference : allServiceReferences) {
                    final SearchProvider service = bundleContext.getService(eachReference);
                    try {
                        final List<SearchResult> providerResult = service.query(query);
                        totalResult.addAll(providerResult);
                    } finally {
                        bundleContext.ungetService(eachReference);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace(); // TODO MVR
        }
        // TODO MVR unify result
        return totalResult;
    }
}
