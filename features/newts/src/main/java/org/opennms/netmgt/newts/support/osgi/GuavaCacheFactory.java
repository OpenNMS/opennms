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

import java.util.Objects;

import org.opennms.netmgt.newts.support.GuavaSearchableResourceMetadataCache;

import com.codahale.metrics.MetricRegistry;

public class GuavaCacheFactory implements CacheFactory<GuavaSearchableResourceMetadataCache> {

    private final long cacheSize;
    private final MetricRegistry metricRegistry;

    public GuavaCacheFactory(long cacheSize, MetricRegistry metricRegistry) {
        this.cacheSize = cacheSize;
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
    }

    @Override
    public Class<GuavaSearchableResourceMetadataCache> supportedType() {
        return GuavaSearchableResourceMetadataCache.class;
    }

    @Override
    public GuavaSearchableResourceMetadataCache createCache() {
        return new GuavaSearchableResourceMetadataCache(cacheSize, metricRegistry);
    }
}
