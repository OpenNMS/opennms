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
package org.opennms.core.network;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;

public class IPAddressTest {

    @Test
    public void shouldReturnDefensiveCopyFromToOctets() {
        final IPAddress ipAddress = new IPAddress("192.0.2.10");
        final byte[] octets = ipAddress.toOctets();
        octets[0] = 0;

        // Mutating the returned array must not alter internal state.
        assertEquals("192.0.2.10", ipAddress.toDbString());
        assertTrue(ipAddress.equals(new IPAddress("192.0.2.10")));
    }

    @Test
    public void shouldIncrementAndDecrementUsingCachedBytes() {
        final IPAddress ipAddress = new IPAddress("192.0.2.10");

        final IPAddress incremented = ipAddress.incr();
        final IPAddress decremented = incremented.decr();

        assertEquals(ipAddress, decremented);
        assertArrayEquals(new byte[] {(byte) 192, 0, 2, 11}, incremented.toOctets());
    }

    @Test
    public void shouldFormatIpv4FromBytesWithoutInetAddressRoundTrip() {
        final IPAddress ipAddress = new IPAddress(new byte[] {(byte) 192, 0, 2, 10});
        assertEquals("192.0.2.10", ipAddress.toDbString());
        assertEquals("192.0.2.10", ipAddress.toUserString());
    }

    @Test
    public void shouldParseIpv4LiteralsWithoutNameService() throws Exception {
        final IPAddress fast = new IPAddress("10.20.30.40");
        final IPAddress jdk = new IPAddress(InetAddress.getByName("10.20.30.40"));
        assertEquals(jdk, fast);
        assertEquals("10.20.30.40", fast.toDbString());
    }

    @Test
    public void shouldFallBackToNameServiceForNonDecimalIpv4Literals() throws Exception {
        final IPAddress ipv6 = new IPAddress("::1");
        assertTrue(ipv6.toInetAddress() instanceof java.net.Inet6Address);
    }

    @Test
    public void shouldDetectImmediatePredecessorAndSuccessorWithoutAllocating() {
        final IPAddress zero = new IPAddress("0.0.0.0");
        final IPAddress one = new IPAddress("0.0.0.1");
        final IPAddress two = new IPAddress("0.0.0.2");

        assertTrue(zero.isPredecessorOf(one));
        assertTrue(one.isSuccessorOf(zero));
        assertTrue(one.isPredecessorOf(two));
        assertTrue(two.isSuccessorOf(one));

        assertFalse(one.isPredecessorOf(one));
        assertFalse(one.isSuccessorOf(one));
        assertFalse(zero.isSuccessorOf(two));
    }
}
