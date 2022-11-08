/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class InetAddressUtilsTest {

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

    @Test
    public void testGetNetwork() throws Exception {
        assertEquals("192.168.1.0", str(InetAddressUtils.getNetwork(InetAddressUtils.addr("192.168.1.3"),InetAddressUtils.addr("255.255.255.0"))));
        assertEquals("192.168.0.0", str(InetAddressUtils.getNetwork(InetAddressUtils.addr("192.168.1.3"),InetAddressUtils.addr("255.255.0.0"))));
        assertEquals("192.0.0.0", str(InetAddressUtils.getNetwork(InetAddressUtils.addr("192.168.1.3"),InetAddressUtils.addr("255.0.0.0"))));
        assertEquals("fd25:28a1:ba2f:0000:0000:0000:0000:0000", str(InetAddressUtils.getNetwork(InetAddressUtils.addr("fd25:28a1:ba2f:6b78:0000:0000:0000:0001"),InetAddressUtils.addr("ffff:ffff:ffff:0:0:0:0:0"))));
        assertEquals("fd25:28a1:ba2f:6b78:0000:0000:0000:0000", str(InetAddressUtils.getNetwork(InetAddressUtils.addr("fd25:28a1:ba2f:6b78:0000:0000:0000:0001"),InetAddressUtils.addr("ffff:ffff:ffff:ffff:0:0:0:0"))));
    }

    @Test
    public void testGetCidr() throws Exception {
        assertEquals(1, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("128.0.0.0")));
        assertEquals(2, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("192.0.0.0")));
        assertEquals(3, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("224.0.0.0")));
        assertEquals(4, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("240.0.0.0")));
        assertEquals(5, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("248.0.0.0")));
        assertEquals(6, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("252.0.0.0")));
        assertEquals(7, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("254.0.0.0")));
        assertEquals(8, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.0.0.0")));
        assertEquals(9, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.128.0.0")));
        assertEquals(10, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.192.0.0")));
        assertEquals(11, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.224.0.0")));
        assertEquals(12, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.240.0.0")));
        assertEquals(13, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.248.0.0")));
        assertEquals(14, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.252.0.0")));
        assertEquals(15, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.254.0.0")));
        assertEquals(16, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.255.0.0")));
        assertEquals(24, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.255.255.0")));
        assertEquals(32, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.255.255.255")));
        assertEquals(64, InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("ffff:ffff:ffff:ffff:0:0:0:0")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNotValidMask() {
        InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.128.255.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNotValidMaskValue() {
        InetAddressUtils.convertInetAddressMaskToCidr(InetAddressUtils.addr("255.255.251.0"));
    }


}
