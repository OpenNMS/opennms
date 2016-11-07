/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
