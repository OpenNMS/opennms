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
