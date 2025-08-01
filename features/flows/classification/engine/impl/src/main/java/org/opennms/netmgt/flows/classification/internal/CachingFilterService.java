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
package org.opennms.netmgt.flows.classification.internal;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.exception.InvalidFilterException;

import com.google.common.cache.CacheLoader;

public class CachingFilterService implements FilterService {

    private final Cache<Key, Boolean> cache;
    private final FilterService delegate;

    public CachingFilterService(FilterService delegate, CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig);
        Objects.requireNonNull(delegate);

        this.delegate = delegate;
        this.cache = new org.opennms.core.cache.CacheBuilder<>()
                .withConfig(cacheConfig)
                .withCacheLoader( new CacheLoader<Key, Boolean>() {
                    @Override
                    public Boolean load(Key key) {
                        return delegate.matches(key.address, key.filterExpression);
                    }
                })
                .build();
    }

    @Override
    public void validate(String filterExpression) throws InvalidFilterException {
        delegate.validate(filterExpression); // not cached
    }

    @Override
    public boolean matches(String address, String filterExpression) {
        try {
            return cache.get(new Key(address, filterExpression));
        } catch (ExecutionException e) {
            throw new RuntimeException("Error loading entry from cache", e);
        }
    }

    private static class Key {
        public final String address;
        public final String filterExpression;

        private Key(String address, String filterExpression) {
            this.address = address;
            this.filterExpression = filterExpression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Key key = (Key) o;
            return Objects.equals(address, key.address)
                    && Objects.equals(filterExpression, key.filterExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, filterExpression);
        }
    }
}
