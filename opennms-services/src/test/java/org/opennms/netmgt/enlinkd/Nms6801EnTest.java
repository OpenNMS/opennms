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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCOISIS_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCOISIS_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCOISIS_ISIS_SYS_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCOISIS_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms6802NetworkBuilder;

public class Nms6801EnTest extends EnLinkdTestBuilder {

	Nms6802NetworkBuilder builder = new Nms6802NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = CISCOISIS_IP, port = 161, resource = CISCOISIS_SNMP_RESOURCE),
    })
    public void testIsIsLinks() throws Exception {
        
        m_nodeDao.save(builder.getCiscoIosXrRouter());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        
        assertTrue(m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode ciscoiosxr = m_nodeDao.findByForeignId("linkd", CISCOISIS_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(ciscoiosxr.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscoiosxr.getId()));
        assertEquals(4, m_isisLinkDao.countAll());

        for (OnmsNode node: m_nodeDao.findAll()) {
        	assertNotNull(node.getIsisElement());
        	System.err.println(node.getIsisElement());
        	assertEquals(CISCOISIS_ISIS_SYS_ID, node.getIsisElement().getIsisSysID());
        	assertEquals(IsisAdminState.on,node.getIsisElement().getIsisSysAdminState());
        }
        
        for (IsIsLink link: m_isisLinkDao.findAll()) {
            System.err.println(link);
            assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
            assertEquals(IsisISAdjState.up,link.getIsisISAdjState());
            assertEquals("000000000000", link.getIsisISAdjNeighSNPAAddress());
            assertEquals(IsisISAdjNeighSysType.l2IntermediateSystem, link.getIsisISAdjNeighSysType());
            switch (link.getIsisCircIndex()) {
            case 19:
                assertEquals(5, link.getIsisISAdjIndex().intValue());
                assertEquals(19, link.getIsisCircIfIndex().intValue());
                assertEquals("093176092059", link.getIsisISAdjNeighSysID());
                assertEquals(234881856,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 20:
                assertEquals(5, link.getIsisISAdjIndex().intValue());
                assertEquals(20, link.getIsisCircIfIndex().intValue());
                assertEquals("093176092059", link.getIsisISAdjNeighSysID());
                assertEquals(234881920,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 27:
                assertEquals(3, link.getIsisISAdjIndex().intValue());
                assertEquals(27, link.getIsisCircIfIndex().intValue());
                assertEquals("093176090003", link.getIsisISAdjNeighSysID());
                assertEquals(33554880,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 28:
                assertEquals(3, link.getIsisISAdjIndex().intValue());
                assertEquals(28, link.getIsisCircIfIndex().intValue());
                assertEquals("093176090003", link.getIsisISAdjNeighSysID());
                assertEquals(33554944,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            default:
                assertTrue(false);
                break;
            }
        }
    }
}
