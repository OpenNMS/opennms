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
package org.opennms.netmgt.jasper.helper;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>SnmpInformantOidResolverTest class.</p>
 *
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public class SnmpInformantOidResolverTest {

    @Test
    public void testStringToAsciiOid() {
        Assert.assertEquals("2.67.58", SnmpInformantOidResolver.stringToAsciiOid("C:"));
        Assert.assertEquals("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.91.84.77.93.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116", SnmpInformantOidResolver.stringToAsciiOid("Broadcom NetLink [TM]-Gigabit-Ethernet"));
    }

    @Test
    public void testAsciiOidToString() {
        Assert.assertEquals("C:", SnmpInformantOidResolver.asciiOidToString("2.67.58"));
        Assert.assertEquals("Broadcom NetLink [TM]-Gigabit-Ethernet", SnmpInformantOidResolver.asciiOidToString("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.91.84.77.93.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116"));
        Assert.assertEquals("Broadcom NetLink (TM)-Gigabit-Ethernet", SnmpInformantOidResolver.asciiOidToString("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.40.84.77.41.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116"));
    }
}
