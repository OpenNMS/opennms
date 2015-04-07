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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DW_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DW_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DW_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms7777DWNetworkBuilder;

public class Nms7777DWEnTest extends EnLinkdTestBuilder {

	Nms7777DWNetworkBuilder builder = new Nms7777DWNetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DW_IP, port=161, resource=DW_SNMP_RESOURCE)
    })
    public void testLldpNoLinks() throws Exception {
        
        m_nodeDao.save(builder.getDragonWaveRouter());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode dw = m_nodeDao.findByForeignId("linkd", DW_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(dw.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(dw.getId()));

        for (final OnmsNode node: m_nodeDao.findAll()) {
            if (node.getLldpElement() != null)
                    printLldpElement(node.getLldpElement());
    }

        assertEquals(0, m_lldpLinkDao.countAll());


        
        
    }
}
