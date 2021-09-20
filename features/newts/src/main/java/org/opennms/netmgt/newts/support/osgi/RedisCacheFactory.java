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
