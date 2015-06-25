/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms003NetworkBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.*;

public class Nms003EnTest extends EnLinkdTestBuilder {

    Nms003NetworkBuilder builder = new Nms003NetworkBuilder();

    /*
     * switch1 0016c8bd4d80 --bft 0016C894AA81/2/3/4-->64
     * switch2 0016c894aa80 --bft 0016C8BD4D89/A/B/C-->64  F4EA67EBDC13/4-->72
     * switch3 f4ea67ebdc00 --bft 0016C894AA93/4-->56
     * 
     * 5001(Port-channel1):64:-------switch2-------:72:5002(Port-channel2)
     *       |                                                 |
     * 5001(Port-channel1):64:---switch1||switch3--:56:5001(Port-channel1)
     */

    private String[] macs = {
            "0016c894aa81", "0016c894aa82", "0016c894aa83", "0016c894aa84",
            "0016c8bd4d89", "0016c8bd4d8a", "0016c8bd4d8b", "0016c8bd4d8c",
            "f4ea67ebdc13", "f4ea67ebdc14",
            "0016c894aa93", "0016c894aa94"
    };

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SWITCH1_IP, port = 161, resource = SWITCH1_SNMP_RESOURCE_003),
            @JUnitSnmpAgent(host = SWITCH2_IP, port = 161, resource = SWITCH2_SNMP_RESOURCE_003),
            @JUnitSnmpAgent(host = SWITCH3_IP, port = 161, resource = SWITCH3_SNMP_RESOURCE_003)
    })
    public void testNetwork003Links() throws Exception {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());

        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));

        assertEquals(0, m_cdpLinkDao.countAll());
        assertEquals(0, m_lldpLinkDao.countAll());
        assertEquals(0, m_ospfLinkDao.countAll());
        assertEquals(0, m_isisLinkDao.countAll());
        assertEquals(0, m_bridgeBridgeLinkDao.countAll());
        assertEquals(0, m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(4, m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(10, m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(12, m_bridgeMacLinkDao.countAll());

        for (String mac : macs) {
            List<BridgeMacLink> macLinks = m_bridgeMacLinkDao.findByMacAddress(mac);

            assertEquals(1, macLinks.size());

            for (BridgeMacLink maclink : macLinks) {
                printBridgeMacLink(maclink);
            }
        }
    }
}
