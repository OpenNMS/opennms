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

package org.opennms.netmgt.telemetry.common.utils;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

public class DnsUtilsTest {

    @After
    public void after() {
        //DnsUtils.setDnsServers();
    }

    private List<String> getServers(final ExtendedResolver extendedResolver) throws Exception {
        final List<String> list = new ArrayList<>();

        for(final Resolver resolver : extendedResolver.getResolvers()) {
            final SimpleResolver simpleResolver = (SimpleResolver) resolver;

            final Field privateAddressField = SimpleResolver.class.getDeclaredField("address");
            privateAddressField.setAccessible(true);
            list.add(((InetSocketAddress)privateAddressField.get(simpleResolver)).getAddress().getHostAddress());
        }
        return list;
    }

    @Test
    public void setDnsServersTest() throws Exception{
        final List<String> addresses1 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses1.size() > 0);

        DnsUtils.setDnsServers("9.8.7.6", "8.7.6.5");
        final List<String> addresses2 = getServers(DnsUtils.getResolver());

        Assert.assertEquals(2, addresses2.size());
        Assert.assertEquals(true, addresses2.contains("9.8.7.6"));
        Assert.assertEquals(true, addresses2.contains("9.8.7.6"));

        DnsUtils.setDnsServers("4.3.2.1");
        final List<String> addresses3 = getServers(DnsUtils.getResolver());

        Assert.assertEquals(1, addresses3.size());
        Assert.assertEquals(true, addresses3.contains("4.3.2.1"));

        DnsUtils.setDnsServers();

        final List<String> addresses4 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses4.size() > 0);
    }

    @Test
    public void resolveTest() throws UnknownHostException {
        final String hostname1 = DnsUtils.reverseLookup(InetAddress.getByAddress(new byte[]{1, 1, 1, 1}));
        Assert.assertEquals("one.one.one.one", hostname1);

        final String hostname2 = DnsUtils.reverseLookup("1.1.1.1");
        Assert.assertEquals("one.one.one.one", hostname2);

        final String hostname3 = DnsUtils.reverseLookup("2606:4700:4700::1111");
        Assert.assertEquals("one.one.one.one", hostname3);
    }

    @Test
    public void resolveFailTest() {
        // 198.51.100.0/24 should be TEST-NET-2 (see RFC #5737). Should fail...
        final String hostname1 = DnsUtils.reverseLookup("198.51.100.1");
        Assert.assertEquals(null, hostname1);

        final String hostname2 = DnsUtils.reverseLookup("fe80::");
        Assert.assertEquals(null, hostname2);
    }

    @Test
    public void lookupFailTest() {
        // 198.51.100.0/24 should be TEST-NET-2 (see RFC #5737). Should fail...
        final String hostname1 = DnsUtils.hostnameOrIpAddress("198.51.100.1");
        Assert.assertEquals("198.51.100.1", hostname1);

        final String hostname2 = DnsUtils.hostnameOrIpAddress("fe80::");
        Assert.assertEquals("fe80:0:0:0:0:0:0:0", hostname2);
    }

    @Test
    public void lookupTest() throws UnknownHostException {
        final String hostname1 = DnsUtils.hostnameOrIpAddress(InetAddress.getByAddress(new byte[]{1, 1, 1, 1}));
        Assert.assertEquals("one.one.one.one", hostname1);

        final String hostname2 = DnsUtils.hostnameOrIpAddress("1.1.1.1");
        Assert.assertEquals("one.one.one.one", hostname2);

        final String hostname3 = DnsUtils.hostnameOrIpAddress("2606:4700:4700::1111");
        Assert.assertEquals("one.one.one.one", hostname3);
    }
}
