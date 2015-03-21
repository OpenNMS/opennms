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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.nb.Nms4930NetworkBuilder;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_SNMP_RESOURCE;

public class Nms4930Test extends LinkdTestBuilder {

	Nms4930NetworkBuilder builder = new Nms4930NetworkBuilder();
    @Before
    public void setUpNetwork4930() throws Exception {
    	builder.setNodeDao(m_nodeDao);
        builder.buildNetwork4930();
    }

    /*
     * The main fact is that this devices have only the Bridge MIb walk
     * dlink_DES has STP disabled
     * dlink_DGS has STP enabled but root is itself
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930Network() throws Exception {

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseBridgeDiscovery(true);
        example1.setUseIsisDiscovery(false);

        example1.setSaveRouteTable(false);
        example1.setEnableVlanDiscovery(false);
        
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        
        assertEquals("we should have found 1 link", 1, ifaces.size());
        for (final DataLinkInterface link: ifaces) {
            checkLink(dlink1, dlink2, 24, 10, link);
            assertEquals(DiscoveryProtocol.bridge, link.getProtocol());
        }
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930NetworkReverse() throws Exception {

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseBridgeDiscovery(true);
        example1.setUseIsisDiscovery(false);

        example1.setSaveRouteTable(false);
        example1.setEnableVlanDiscovery(false);
        
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        assertEquals("we should have found one link", 1, ifaces.size());
        for (final DataLinkInterface link: ifaces) {
            checkLink(dlink1,dlink2 , 24, 10, link);
            assertEquals(DiscoveryProtocol.bridge, link.getProtocol());
        }

    }

}
