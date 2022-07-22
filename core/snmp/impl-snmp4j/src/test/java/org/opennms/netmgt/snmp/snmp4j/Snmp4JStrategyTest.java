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
