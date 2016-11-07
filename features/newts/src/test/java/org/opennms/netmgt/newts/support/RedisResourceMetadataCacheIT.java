/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.EscapableResourceIdSplitter;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.codahale.metrics.MetricRegistry;

import redis.clients.jedis.Jedis;

/**
 * Requires a Redis server.
 *
 * @author jwhite
 */
@Ignore
public class RedisResourceMetadataCacheIT {

    private static final String REDIS_HOSTNAME = "localhost";

    private static final int REDIS_PORT = 6379;

    private MetricRegistry m_registry = new MetricRegistry();

    @Before
    public void setUp() {
        // Delete all keys
        try (Jedis jedis = new Jedis(REDIS_HOSTNAME, REDIS_PORT)) {
            jedis.flushAll();
        }
    }

    @Test
    public void canGetEntriesWithPrefix() {
        Context ctx = Context.DEFAULT_CONTEXT;

        RedisResourceMetadataCache cache = new RedisResourceMetadataCache(REDIS_HOSTNAME, REDIS_PORT, 8, m_registry, new EscapableResourceIdSplitter());

        assertTrue(cache.getResourceIdsWithPrefix(ctx, "a").isEmpty());

        Resource resource = new Resource("a:b:c");
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        cache.merge(ctx, resource, resourceMetadata);

        assertTrue(cache.getResourceIdsWithPrefix(ctx, "a").contains("a:b:c"));
        assertTrue(cache.getResourceIdsWithPrefix(ctx, "a:b").contains("a:b:c"));
        assertTrue(cache.getResourceIdsWithPrefix(ctx, "a:b:c").contains("a:b:c"));
        assertTrue(cache.getResourceIdsWithPrefix(ctx, "a:b:c:d").isEmpty());
    }

    @Test
    public void canUpdateEntry() {
        Context ctx = Context.DEFAULT_CONTEXT;

        RedisResourceMetadataCache cache = new RedisResourceMetadataCache(REDIS_HOSTNAME, REDIS_PORT, 8, m_registry, new EscapableResourceIdSplitter());

        // Insert
        Resource resource = new Resource("a:b:c");
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.putAttribute("a1", "1");
        cache.merge(ctx, resource, resourceMetadata);

        // Verify
        assertTrue("attribute a1 must be set", cache.get(ctx, resource).get().containsAttribute("a1", "1"));

        // Update
        resourceMetadata = new ResourceMetadata();
        resourceMetadata.putAttribute("a2", "2");
        cache.merge(ctx, resource, resourceMetadata);

        // Verify
        assertTrue("attribute a1 must be set", cache.get(ctx, resource).get().containsAttribute("a1", "1"));
        assertTrue("attribute a2 must be set", cache.get(ctx, resource).get().containsAttribute("a2", "2"));

    }
}
