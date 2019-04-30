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

package org.opennms.core.cache;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache<K, V> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CacheConfig config;
    private final CacheLoader<K, V> cacheLoader;
    private LoadingCache<K, V> delegate;

    public Cache(CacheConfig config, CacheLoader<K, V> cacheLoader) {
        this.config = Objects.requireNonNull(config);
        this.cacheLoader = Objects.requireNonNull(cacheLoader);

        config.validate();
        logger.debug("Cache cache.{} is {}", config.getName(), config.isEnabled());

        if (config.isEnabled()) {
            this.delegate = config.createBuilder().build(cacheLoader);
        }

        // Expose cache statistics
        if (delegate != null) {
            if (config.isRecordStats()) {
                logger.debug("Recording of \"{}\" cache statistics is enabled.", config.getName());
                final MetricRegistry registry = config.getMetricRegistry();
                registry.register(MetricRegistry.name("cache." + config.getName() + ".evictionCount"), (Gauge) () -> delegate.stats().evictionCount());
                registry.register(MetricRegistry.name("cache." + config.getName() + ".hitRate"), (Gauge) () -> delegate.stats().hitRate());
                registry.register(MetricRegistry.name("cache." + config.getName() + ".loadExceptionCount"), (Gauge) () -> delegate.stats().loadExceptionCount());
            } else {
                logger.debug("Recording of \"{}\" cache statistics is disabled.", config.getName());
            }
        }
    }

    public V get(K key) throws ExecutionException {
        Objects.requireNonNull(key);
        if (config.isEnabled()) {
            return delegate.get(key);
        }
        try {
            return cacheLoader.load(key);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public V getIfCached(K key)  {
        Objects.requireNonNull(key);
        if (!config.isEnabled()) {
            return null;
        }
        return delegate.getIfPresent(key);
    }

    public void invalidateAll() {
        if (delegate != null) {
            delegate.invalidateAll();
        }
    }

    public void refresh(K key) {
        if (delegate != null) {
            delegate.refresh(key);
        }
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key);
        if (delegate != null) {
            delegate.put(key, value);
        }
    }

}
