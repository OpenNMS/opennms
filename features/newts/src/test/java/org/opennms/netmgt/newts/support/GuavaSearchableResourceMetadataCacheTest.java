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
package org.opennms.netmgt.newts.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;
import org.springframework.test.annotation.IfProfileValue;

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
    @IfProfileValue(name="runBenchmarkTests", value="true")
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
