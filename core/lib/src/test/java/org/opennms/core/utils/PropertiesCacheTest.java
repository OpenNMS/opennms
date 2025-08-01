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
package org.opennms.core.utils;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;

public class PropertiesCacheTest {
    private final String FILE1 = "src/test/resources/share/rrd/snmp/1/strings.properties";
    private final String FILE2 = "src/test/resources/share/rrd/snmp/2/strings.properties";

    static class FakeTicker extends Ticker {
        private final AtomicLong nanos = new AtomicLong();

        public void skip(long time, TimeUnit timeUnit) {
            nanos.getAndAdd(timeUnit.toNanos(time));
        }

        @Override
        public long read() {
            return nanos.get();
        }
    }

    @Test
    public void testFile() throws Exception {
        final PropertiesCache propertiesCache = new PropertiesCache();
        final Properties properties1 = propertiesCache.getProperties(new File(FILE1));

        Assert.assertEquals("1000", properties1.getProperty("ifSpeed"));
        Assert.assertEquals(1L, propertiesCache.m_cache.size());
    }

    @Test
    public void testClear() throws Exception {
        final PropertiesCache propertiesCache = new PropertiesCache();
        final Properties properties1 = propertiesCache.getProperties(new File(FILE1));

        Assert.assertEquals("1000", properties1.getProperty("ifSpeed"));
        Assert.assertEquals(1L, propertiesCache.m_cache.size());

        propertiesCache.clear();

        Assert.assertEquals(0L, propertiesCache.m_cache.size());
    }

    @Test
    public void testEviction() throws Exception {
        final FakeTicker fakeTicker = new FakeTicker();
        final PropertiesCache propertiesCache = new PropertiesCache(CacheBuilder.newBuilder().ticker(fakeTicker).concurrencyLevel(1));

        final Properties properties1 = propertiesCache.getProperties(new File(FILE1));
        Assert.assertEquals("1000", properties1.getProperty("ifSpeed"));

        final Properties properties2 = propertiesCache.getProperties(new File(FILE2));
        Assert.assertEquals("100", properties2.getProperty("ifSpeed"));

        Assert.assertEquals(2L, propertiesCache.m_cache.size());

        fakeTicker.skip(PropertiesCache.DEFAULT_CACHE_TIMEOUT * 2, TimeUnit.SECONDS);
        propertiesCache.getProperties(new File(FILE2));

        Assert.assertEquals(1L, propertiesCache.m_cache.size());
    }
}
