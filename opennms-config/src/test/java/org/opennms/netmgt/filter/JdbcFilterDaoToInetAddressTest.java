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
package org.opennms.netmgt.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.Inet6Address;
import java.net.InetAddress;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;

/**
 * A single row whose ipaddr value cannot be converted to an InetAddress must
 * not abort a whole filter evaluation (NMS-20004). These cover the tolerant
 * conversion the result-row paths use.
 */
public class JdbcFilterDaoToInetAddressTest {

    @Test
    public void convertsPlainAddresses() {
        assertEquals(InetAddressUtils.addr("10.0.1.2"),
                JdbcFilterDao.toInetAddressOrNull("10.0.1.2"));
        assertEquals(InetAddressUtils.addr("fe80:0000:0000:0000:0000:0000:0000:0001"),
                JdbcFilterDao.toInetAddressOrNull("fe80:0000:0000:0000:0000:0000:0000:0001"));
    }

    @Test
    public void keepsNumericZoneIds() {
        // the form OpenNMS itself stores for scoped addresses
        final InetAddress converted =
                JdbcFilterDao.toInetAddressOrNull("fe80:0000:0000:0000:0000:0000:0000:0001%4");
        assertTrue(converted instanceof Inet6Address);
        assertEquals(4, ((Inet6Address) converted).getScopeId());
    }

    @Test
    public void dropsUnresolvableInterfaceNameZones() {
        // "no-such-if0" names an interface of the monitored node, not this
        // host; the address is still usable without its zone
        assertEquals(InetAddressUtils.addr("fe80:0000:0000:0000:0000:0000:0000:0001"),
                JdbcFilterDao.toInetAddressOrNull("fe80:0000:0000:0000:0000:0000:0000:0001%no-such-if0"));
    }

    @Test
    public void skipsHostnamesWithoutResolving() {
        assertNull(JdbcFilterDao.toInetAddressOrNull("some-host.example.org"));
        // all-hex-digit labels must not be mistaken for address literals
        assertNull(JdbcFilterDao.toInetAddressOrNull("bad.cafe"));
    }

    @Test
    public void skipsNonAddressValues() {
        assertNull(JdbcFilterDao.toInetAddressOrNull(null));
        assertNull(JdbcFilterDao.toInetAddressOrNull(""));
        assertNull(JdbcFilterDao.toInetAddressOrNull("   "));
        assertNull(JdbcFilterDao.toInetAddressOrNull("1.2.3.4.5"));
        assertNull(JdbcFilterDao.toInetAddressOrNull("10.999.0.1"));
        assertNull(JdbcFilterDao.toInetAddressOrNull(":::"));
    }
}
