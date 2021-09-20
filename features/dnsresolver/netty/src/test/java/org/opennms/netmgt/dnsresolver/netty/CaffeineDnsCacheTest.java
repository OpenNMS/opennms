/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.opennms.netmgt.dnsresolver.netty;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.util.NetUtil;
import org.junit.Test;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * DNS cache test largely adopted from https://github.com/netty/netty/blob/netty-4.1.38.Final/resolver-dns/src/test/java/io/netty/resolver/dns/DefaultDnsCacheTest.java
 */
public class CaffeineDnsCacheTest {

    @Test
    public void testExpire() throws Throwable {
        InetAddress addr1 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 1 });
        InetAddress addr2 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 2 });
        EventLoopGroup group = new DefaultEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache();
            cache.cache("netty.io", null, addr1, 1, loop);
            cache.cache("netty.io", null, addr2, 10000, loop);

            Throwable error = loop.schedule(new Callable<Throwable>() {
                @Override
                public Throwable call() {
                    try {
                        assertNull(cache.get("netty.io", null));
                        return null;
                    } catch (Throwable cause) {
                        return cause;
                    }
                }
            }, 1, TimeUnit.SECONDS).get();
            if (error != null) {
                throw error;
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testExpireWithDifferentTTLs() {
        testExpireWithTTL0(1);
        testExpireWithTTL0(1000);
        testExpireWithTTL0(1000000);
    }

    private static void testExpireWithTTL0(int days) {
        EventLoopGroup group = new NioEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache();
            assertNotNull(cache.cache("netty.io", null, NetUtil.LOCALHOST, days, loop));
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testExpireWithToBigMinTTL() {
        EventLoopGroup group = new NioEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            assertNotNull(cache.cache("netty.io", null, NetUtil.LOCALHOST, 100, loop));
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testAddMultipleAddressesForSameHostname() throws Exception {
        InetAddress addr1 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 1 });
        InetAddress addr2 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 2 });
        EventLoopGroup group = new DefaultEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache();
            cache.cache("netty.io", null, addr1, 10000, loop);
            cache.cache("netty.io", null, addr2, 10000, loop);

            List<? extends DnsCacheEntry> entries = cache.get("netty.io", null);
            assertEquals(2, entries.size());
            assertEntry(entries.get(0), addr1);
            assertEntry(entries.get(1), addr2);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testAddSameAddressForSameHostname() throws Exception {
        InetAddress addr1 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 1 });
        EventLoopGroup group = new DefaultEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache();
            cache.cache("netty.io", null, addr1, 1, loop);
            cache.cache("netty.io", null, addr1, 10000, loop);

            List<? extends DnsCacheEntry> entries = cache.get("netty.io", null);
            assertEquals(1, entries.size());
            assertEntry(entries.get(0), addr1);
        } finally {
            group.shutdownGracefully();
        }
    }

    private static void assertEntry(DnsCacheEntry entry, InetAddress address) {
        assertEquals(address, entry.address());
        assertNull(entry.cause());
    }

    @Test
    public void testCacheFailed() throws Exception {
        InetAddress addr1 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 1 });
        InetAddress addr2 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 2 });
        EventLoopGroup group = new DefaultEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache(1, 100, 100, 0);
            cache.cache("netty.io", null, addr1, 10000, loop);
            cache.cache("netty.io", null, addr2, 10000, loop);

            List<? extends DnsCacheEntry> entries = cache.get("netty.io", null);
            assertEquals(2, entries.size());
            assertEntry(entries.get(0), addr1);
            assertEntry(entries.get(1), addr2);

            Exception exception = new Exception();
            cache.cache("netty.io", null, exception, loop);
            entries = cache.get("netty.io", null);
            DnsCacheEntry entry = entries.get(0);
            assertEquals(1, entries.size());
            assertSame(exception, entry.cause());
            assertNull(entry.address());
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testDotHandling() throws Exception {
        InetAddress addr1 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 1 });
        InetAddress addr2 = InetAddress.getByAddress(new byte[] { 10, 0, 0, 2 });
        EventLoopGroup group = new DefaultEventLoopGroup(1);

        try {
            EventLoop loop = group.next();
            final CaffeineDnsCache cache = new CaffeineDnsCache(1, 100, 100, 0);
            cache.cache("netty.io", null, addr1, 10000, loop);
            cache.cache("netty.io.", null, addr2, 10000, loop);

            List<? extends DnsCacheEntry> entries = cache.get("netty.io", null);
            assertEquals(2, entries.size());
            assertEntry(entries.get(0), addr1);
            assertEntry(entries.get(1), addr2);

            List<? extends DnsCacheEntry> entries2 = cache.get("netty.io.", null);
            assertEquals(2, entries2.size());
            assertEntry(entries2.get(0), addr1);
            assertEntry(entries2.get(1), addr2);
        } finally {
            group.shutdownGracefully();
        }
    }
}