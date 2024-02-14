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

import org.opennms.netmgt.newts.support.RedisResourceMetadataCache;
import org.opennms.newts.cassandra.search.ResourceIdSplitter;

import com.codahale.metrics.MetricRegistry;

public class RedisCacheFactory implements CacheFactory<RedisResourceMetadataCache> {

    private final String hostname;
    private final int port;
    private final int numWriterThreads;
    private final MetricRegistry registry;
    private final ResourceIdSplitter resourceIdSplitter;

    public RedisCacheFactory(String hostname, int port, int numWriterThreads, MetricRegistry registry, ResourceIdSplitter resourceIdSplitter) {
        this.hostname = Objects.requireNonNull(hostname);
        this.port = port;
        this.numWriterThreads = numWriterThreads;
        this.registry = Objects.requireNonNull(registry);
        this.resourceIdSplitter = Objects.requireNonNull(resourceIdSplitter);
    }

    @Override
    public Class<RedisResourceMetadataCache> supportedType() {
        return RedisResourceMetadataCache.class;
    }

    @Override
    public RedisResourceMetadataCache createCache() {
        return new RedisResourceMetadataCache(hostname, port, numWriterThreads, registry, resourceIdSplitter);
    }
}
