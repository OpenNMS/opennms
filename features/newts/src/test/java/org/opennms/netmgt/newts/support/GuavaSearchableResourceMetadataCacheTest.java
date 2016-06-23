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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.codahale.metrics.MetricRegistry;

public class GuavaSearchableResourceMetadataCacheTest {

    private MetricRegistry m_registry = new MetricRegistry();

    @Test
    public void canGetEntriesWithPrefix() {
        Context ctx = Context.DEFAULT_CONTEXT;
        GuavaSearchableResourceMetadataCache cache = new GuavaSearchableResourceMetadataCache(2048, m_registry);

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
    @Ignore
    public void getResourceIdsWithPrefixPerftTest() {
        long numResourceIdsToCache = 200000;
        long numSearches = 1000;

        Context ctx = Context.DEFAULT_CONTEXT;
        GuavaSearchableResourceMetadataCache cache = new GuavaSearchableResourceMetadataCache(numResourceIdsToCache, m_registry);

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        for (long k = 0; k < numResourceIdsToCache; k++) {
            Resource resource = new Resource(String.format("snmp:%d:eth0-x:ifHcInOctets", k));
            cache.merge(ctx, resource, resourceMetadata);
        }

        long start = System.currentTimeMillis();
        String prefix = "snmp:" + (numResourceIdsToCache-1);
        for (long k = 0; k < numSearches; k++) {
            assertEquals(1, cache.getResourceIdsWithPrefix(ctx, prefix).size());
        }
        long elapsed = System.currentTimeMillis() - start;
        System.err.println("elapsed: " + elapsed);
    }
}
