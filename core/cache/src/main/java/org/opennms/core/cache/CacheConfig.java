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
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;

/**
 * {@link CacheConfig} bean to help configuring a Google Guava {@link CacheBuilder}.
 * This is helpful when using {@link CacheBuilder}s in blueprint bundles.
 *
 * @see CacheBuilder
 */
public class CacheConfig {

    private String name;

    private boolean enabled = true;

    private Long maximumSize;

    private Long expireAfterWrite; // Seconds

    private Long expireAfterRead; // Seconds

    private boolean recordStats;

    private MetricRegistry metricRegistry;

    protected CacheConfig() {

    }

    public CacheConfig(String cacheName) {
        this.name = Objects.requireNonNull(cacheName);
    }

    public Long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(Long maxSize) {
        this.maximumSize = maxSize;
    }

    public Long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(Long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    public Long getExpireAfterRead() {
        return expireAfterRead;
    }

    public void setExpireAfterRead(Long expireAfterRead) {
        this.expireAfterRead = expireAfterRead;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected <K, V> CacheBuilder<K,V> createBuilder() {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (getMaximumSize() != null && getMaximumSize() > 0) {
            cacheBuilder.maximumSize(getMaximumSize());
        }
        if (getExpireAfterWrite() != null && getExpireAfterWrite() > 0) {
            cacheBuilder.expireAfterWrite(getExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if (getExpireAfterRead() != null && getExpireAfterRead() > 0) {
            cacheBuilder.expireAfterAccess(getExpireAfterRead(), TimeUnit.SECONDS);
        }
        if (isRecordStats()) {
            cacheBuilder.recordStats();
        }
        return cacheBuilder;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("name", name)
                .add("maximumSize", maximumSize)
                .add("expireAfterWrite", expireAfterWrite != null ? expireAfterWrite + " sec" : null)
                .add("expireAfterRead", expireAfterRead != null ? expireAfterRead + " sec" : null)
                .add("recordStats", recordStats)
                .toString();
    }

    public void validate() {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalStateException("Cache has no name");
        }
        if (recordStats && metricRegistry == null) {
            throw new IllegalStateException("Cache '" + name + "' should record statistics, but no MetricRegistry is provided");
        }
    }
}
