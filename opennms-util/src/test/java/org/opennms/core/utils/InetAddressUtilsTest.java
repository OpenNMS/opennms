//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 2010 Feb 23: Add tests for substitute methods that take multiple
//              Properties via varargs. - jeffg@opennms.org
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class InetAddressUtilsTest {

    @Test
    @Ignore
    public void testPreferIPv6() throws Exception {
        InetAddressUtils.resolveHostname("ipv4.www.yahoo.com", false);
        try {
            InetAddressUtils.resolveHostname("ipv4.www.yahoo.com", true);
            fail();
        } catch (UnknownHostException e) {
            // Expected exception
        }
        try {
            InetAddressUtils.resolveHostname("ipv6.www.yahoo.com", false);
            fail();
        } catch (UnknownHostException e) {
            // Expected exception
        }
        InetAddressUtils.resolveHostname("ipv6.www.yahoo.com", true);
    }

    @Test
    @Ignore
    public void testLookup() throws Exception {
        InetAddress fb = InetAddressUtils.resolveHostname("www.opennms.org", false);
        assertNotNull(fb);
    }

    /**
     * Make sure this test is FIRST.
     */
    @Test
    @Ignore
    public void testOrderingOfLookups() throws Exception {
        //String lookup = "www.opennms.org";
        String lookup = "www.facebook.com";
        Record[] fb = new Lookup(lookup, Type.AAAA).run();
        fb = new Lookup(lookup, Type.A).run();
        assertNotNull(fb);
    }

    @Test
    public void testMacAddressFunctions() throws Exception {
        byte[] expected = new byte[] {
            (byte)0xff,
            (byte)0x80,
            (byte)0x0f,
            (byte)0xf0,
            (byte)0x01,
            (byte)0x00 
        };
        byte[] actual = InetAddressUtils.macAddressStringToBytes("ff:80:f:f0:01:00");
        Assert.assertArrayEquals(expected, actual);
        //assertEquals("FF:80:0F:F0:01:00", InetAddressUtils.macAddressBytesToString(actual));
        assertEquals("ff800ff00100", InetAddressUtils.macAddressBytesToString(actual));

        actual = InetAddressUtils.macAddressStringToBytes("ff:80:f:f0:01:0");
        Assert.assertArrayEquals(expected, actual);
        assertEquals("ff800ff00100", InetAddressUtils.macAddressBytesToString(actual));

        actual = InetAddressUtils.macAddressStringToBytes("ff800ff00100");
        Assert.assertArrayEquals(expected, actual);
        assertEquals("ff800ff00100", InetAddressUtils.macAddressBytesToString(actual));

        try {
            InetAddressUtils.macAddressStringToBytes("ff800ff0010");
            fail("Parsed MAC address value that was too short");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            InetAddressUtils.macAddressStringToBytes("ff800ff001000");
            fail("Parsed MAC address value that was too long");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void testNMS4972() throws Exception {
        String ip1 = "1.1.1.1";
        String ip2 = "255.255.255.255";
        Assert.assertFalse(BigInteger.ZERO.compareTo(InetAddressUtils.difference(ip1, ip2)) < 0);
    }
    
    @Test
    public void testCidrFunctions() throws Exception {
        assertEquals("255.0.0.0", str(InetAddressUtils.convertCidrToInetAddressV4(8)));
        assertEquals("255.255.0.0", str(InetAddressUtils.convertCidrToInetAddressV4(16)));
        assertEquals("255.255.255.0", str(InetAddressUtils.convertCidrToInetAddressV4(24)));
        assertEquals("255.255.255.255", str(InetAddressUtils.convertCidrToInetAddressV4(32)));

        assertEquals("ffff:ffff:ffff:0000:0000:0000:0000:0000", str(InetAddressUtils.convertCidrToInetAddressV6(48)));
        assertEquals("ffff:ffff:ffff:ffff:0000:0000:0000:0000", str(InetAddressUtils.convertCidrToInetAddressV6(64)));
        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", str(InetAddressUtils.convertCidrToInetAddressV6(128)));
    }
}
