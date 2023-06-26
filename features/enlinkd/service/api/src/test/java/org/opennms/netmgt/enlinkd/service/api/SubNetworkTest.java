/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class SubNetworkTest {

    @Test
    public void s() throws UnknownHostException {
        SubNetwork ipv4 = SubNetwork.createSubNetwork(100, InetAddress.getByName("192.168.1.10"),InetAddress.getByName("255.255.255.0"));
        assertEquals(ipv4.getCidr(),"192.168.1.0/24");
        assertEquals(str(ipv4.getNetwork()),"192.168.1.0");
        assertEquals(str(ipv4.getNetmask()),"255.255.255.0");
        SubNetwork ipv4B = SubNetwork.createSubNetwork(101, InetAddress.getByName("192.168.1.100"),InetAddress.getByName("255.255.255.0"));
        assertEquals(ipv4B.getCidr(),"192.168.1.0/24");
        assertEquals(str(ipv4B.getNetwork()),"192.168.1.0");
        assertEquals(str(ipv4B.getNetmask()),"255.255.255.0");
        assertEquals(ipv4,ipv4B);
        for (int i=1;i<255;i++) {
            assertTrue(ipv4B.isInRange(InetAddress.getByName("192.168.1." + i)));
        }
        for (int i=0;i<256;i++) {
            assertFalse(ipv4B.isInRange(InetAddress.getByName("192.168.0." + i)));
        }
        for (int i=0;i<256;i++) {
            assertFalse(ipv4B.isInRange(InetAddress.getByName("192.168.2." + i)));
        }
        assertEquals(1,ipv4.getNodeIds().size());
        assertTrue(ipv4.getNodeIds().contains(100));
        assertEquals(1,ipv4B.getNodeIds().size());
        assertTrue(ipv4B.getNodeIds().contains(101));

        assertFalse(ipv4.remove(1, InetAddress.getByName("192.168.1.10")));
        assertFalse(ipv4B.remove(100, InetAddress.getByName("192.168.1.100")));

        assertTrue(ipv4B.remove(101, InetAddress.getByName("192.168.1.100")));
        assertEquals(0,ipv4B.getNodeIds().size());

    }

    @Test
    public void ipv6SubnetTest() throws UnknownHostException {
        SubNetwork ipv6 = SubNetwork.createSubNetwork(200,InetAddress.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7344"),InetAddress.getByName("FFFF:FF00:0000:0000:0000:0000:0000:0000"));
        assertEquals(ipv6.getCidr(),"2001:d00:0:0:0:0:0:0/24");

        SubNetwork ipv6A = SubNetwork.createSubNetwork(201,InetAddress.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7344"),InetAddress.getByName("FFFF:FFFF:0000:0000:0000:0000:0000:0000"));
        assertEquals(ipv6A.getCidr(),"2001:db8:0:0:0:0:0:0/32");

        SubNetwork ipv6B = SubNetwork.createSubNetwork(202,InetAddress.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7344"),InetAddress.getByName("FFFF:FFFF:FFFF:FFFF:0000:0000:0000:0000"));
        assertEquals(ipv6B.getCidr(),"2001:db8:85a3:8d3:0:0:0:0/64");

    }

}

