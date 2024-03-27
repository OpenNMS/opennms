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
package org.opennms.core.cache;

import java.util.Objects;
import java.util.concurrent.Callable;
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

    public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
        Objects.requireNonNull(key);
        if (config.isEnabled()) {
            return delegate.get(key, valueLoader);
        }
        try {
            return valueLoader.call();
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
