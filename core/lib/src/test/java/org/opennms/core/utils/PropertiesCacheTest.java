/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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
