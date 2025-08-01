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
package org.opennms.netmgt.snmp.proxy.common.testutils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

/**
 * Expected results from the loadSnmpDataTest.properties file.
 *
 * @author jwhite
 */
public class ExpectedResults {

    public static void compareToKnownIpAddressList(List<String> ipAddresses) {
        assertThat(ipAddresses, contains(
                "127.0.0.1",
                "172.17.0.1",
                "172.23.1.102",
                "172.23.1.255",
                "192.168.122.1",
                "192.168.122.255",
                "0000:0000:0000:0000:0000:0000:0000:0001",
                "fe80:0000:0000:0000:2820:58ff:fe24:ae32"));
        assertEquals(8, ipAddresses.size());
    }

    public static void compareToKnownIfIndices(Set<Integer> ifIndices) {
        assertThat(ifIndices, contains(1, 3, 5, 7));
        assertEquals(4, ifIndices.size());
    }
}
