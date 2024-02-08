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
