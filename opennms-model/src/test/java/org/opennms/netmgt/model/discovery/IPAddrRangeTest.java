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
package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import junit.framework.TestCase;

import org.opennms.core.network.IPAddress;

/**
 * IPAddressRangeTest
 *
 * @author brozow
 */
public class IPAddrRangeTest extends TestCase {

    private final IPAddress addr2 = new IPAddress("192.168.1.3");
    private final IPAddress addr3 = new IPAddress("192.168.1.5");
    private final IPAddress v6low = new IPAddress("ffdd:0000:0000:0000:0000:0000:0000:0000");
    private final IPAddress v6high = new IPAddress("ffff:0000:0000:0000:0000:0000:0000:0000");

    private final IPAddrRange singleton;
    private final IPAddrRange v6singleton;
    private final IPAddrRange small;
    private final IPAddrRange v6large;

    public IPAddrRangeTest() throws UnknownHostException {
        small = new IPAddrRange(addr2.toString(), addr3.toString());
        singleton = new IPAddrRange(addr2.toString(), addr2.toString());
        v6large = new IPAddrRange(v6low.toString(), v6high.toString());
        v6singleton = new IPAddrRange(v6low.toString(), v6low.toString());
    }

    public void testIterator() {
        // assertEquals(3, small.size());
        Iterator<InetAddress> it = small.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2.toInetAddress(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr2.incr().toInetAddress(), it.next());
        assertTrue(it.hasNext());
        assertEquals(addr3.toInetAddress(), it.next());
        assertFalse(it.hasNext());
    }

    public void testIterateSingleton() {
        Iterator<InetAddress> it = singleton.iterator();
        assertTrue(it.hasNext());
        assertEquals(addr2.toInetAddress(), it.next());
        assertFalse(it.hasNext());
    }

    public void testGetSizeOfIpAddrRange() {
        assertEquals(BigInteger.valueOf(3), small.size()); 
        assertEquals(BigInteger.ONE, singleton.size());
    }

    public void testSizeLargerThanIntegerMaxValue() {
        assertEquals(new BigInteger("220000000000000000000000000000", 16).add(BigInteger.ONE), v6large.size()); 
        assertEquals(BigInteger.ONE, v6singleton.size());
    }
}
