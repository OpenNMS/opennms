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
