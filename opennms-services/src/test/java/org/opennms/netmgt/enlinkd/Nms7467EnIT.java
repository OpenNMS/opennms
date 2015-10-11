/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.CdpElement.CdpGlobalDeviceIdFormat;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.nb.Nms7467NetworkBuilder;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_GLOBAL_DEVICEID;

public class Nms7467EnIT extends EnLinkdBuilderITCase {

	private Nms7467NetworkBuilder builder = new Nms7467NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE)
    })
    public void testCisco01Links() throws Exception {
        
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode cisco01 = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(cisco01.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(cisco01.getId()));

        for (final OnmsNode node: m_nodeDao.findAll()) {
            
            assertNotNull(node.getCdpElement());
            printCdpElement(node.getCdpElement());
            assertEquals(TruthValue.TRUE, node.getCdpElement().getCdpGlobalRun());
            assertEquals(CISCO_WS_C2948_GLOBAL_DEVICEID,node.getCdpElement().getCdpGlobalDeviceId());
            assertEquals(CdpGlobalDeviceIdFormat.other, node.getCdpElement().getCdpGlobalDeviceIdFormat());
        }

        assertEquals(5, m_cdpLinkDao.countAll());
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            assertNotNull(link);
            printCdpLink(link);
        }
        
    }


}
