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
package org.opennms.netmgt.snmp.snmp4j;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;

public class Snmp4JStrategyTest {

    Snmp4JStrategy strat = new Snmp4JStrategy();

    @Test
    public void testBuildAgentConfig() throws Exception {
        String address = "127.0.0.1";
        String community = "TEST";
        int port = 10162;
        int timeout = 10;
        int retries = 3;
        PDU pdu = new PDUv1();
        int securityLevel = 3;
        String securityName = "name";
        String authPassPhrase = "please";
        String authProtocol = "manners";
        String privPassPhrase = "pretty please";
        String privProocol = "shmanners";

        SnmpAgentConfig config = strat.buildAgentConfig(address, port, community, pdu);
        assertTrue(InetAddressUtils.str(config.getAddress()).equals(address));
        assertEquals(config.getPort(), port);
        assertEquals(config.getReadCommunity(), community);
        assertEquals(config.getVersion(), 1);

        config = strat.buildAgentConfig(address, port, timeout, retries, community, pdu);
        assertTrue(InetAddressUtils.str(config.getAddress()).equals(address));
        assertEquals(config.getPort(), port);
        assertEquals(config.getReadCommunity(), community);
        assertEquals(config.getVersion(), 1);
        assertEquals(config.getTimeout(), timeout);
        assertEquals(config.getRetries(), retries);

        pdu = new ScopedPDU();

        config = strat.buildAgentConfig(address, port, securityLevel, securityName, authPassPhrase, authProtocol, privPassPhrase, privProocol, pdu);
        assertTrue(InetAddressUtils.str(config.getAddress()).equals(address));
        assertEquals(config.getPort(), port);
        assertEquals(config.getVersion(), 3);
        assertEquals(config.getSecurityLevel(), securityLevel);
        assertEquals(config.getSecurityName(), securityName);
        assertEquals(config.getAuthPassPhrase(), authPassPhrase);
        assertEquals(config.getAuthProtocol(), authProtocol);
        assertEquals(config.getPrivPassPhrase(), privPassPhrase);
        assertEquals(config.getPrivProtocol(), privProocol);

        config = strat.buildAgentConfig(address, port, timeout, retries, securityLevel, securityName, authPassPhrase, authProtocol, privPassPhrase, privProocol, pdu);
        assertTrue(InetAddressUtils.str(config.getAddress()).equals(address));
        assertEquals(config.getPort(), port);
        assertEquals(config.getVersion(), 3);
        assertEquals(config.getTimeout(), timeout);
        assertEquals(config.getRetries(), retries);
        assertEquals(config.getSecurityLevel(), securityLevel);
        assertEquals(config.getSecurityName(), securityName);
        assertEquals(config.getAuthPassPhrase(), authPassPhrase);
        assertEquals(config.getAuthProtocol(), authProtocol);
        assertEquals(config.getPrivPassPhrase(), privPassPhrase);
        assertEquals(config.getPrivProtocol(), privProocol);
    }

}
