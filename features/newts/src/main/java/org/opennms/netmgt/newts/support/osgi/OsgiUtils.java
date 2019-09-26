/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support.osgi;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.cassandra.search.CassandraIndexingOptions;

/**
 * Helper Class to make creation of objects easier when instantiating beans in OSGi.
 *
 */
public class OsgiUtils {

    public static Context createContext() {
        return Context.DEFAULT_CONTEXT;
    }

    public static CassandraIndexingOptions createIndexOptions(int maxBatchSize) {
        return new CassandraIndexingOptions.Builder()
                .withHierarchicalIndexing(false)
                .withIndexResourceTerms(false)
                .withIndexUsingDefaultTerm(false)
                .withMaxBatchSize(maxBatchSize)
                .build();
    }

    public static <T extends SearchableResourceMetadataCache> T createCache(String cacheType, List<CacheFactory> factories) {
        Objects.requireNonNull(cacheType);
        Objects.requireNonNull(factories);
        try {
            final Class<?> cacheClass = Class.forName(cacheType);
            final Optional<CacheFactory> cacheFactory = factories.stream().filter(f -> f.supportedType() == cacheClass).findFirst();
            if (cacheFactory.isPresent()) {
                return (T) cacheFactory.get().createCache();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        throw new IllegalArgumentException("No cache of type " + cacheType + " found.");
    }
}
