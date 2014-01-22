/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

public class Nms4930Test extends Nms4930NetworkBuilder {

    @Before
    public void setUpNetwork4005() throws Exception {
        buildNetwork4930();
    }

    /*
     * The main fact is that this devices have only the Bridge MIb walk
     * dlink_DES has STP disabled
     * dlink_DGS has STP enabled but root is itself
     * no way to find links....
     * Also there is no At interface information
     * c2007db90010 --> 10.1.1.2  ---nothing in the bridge forwarding table...
     * no way to get links...
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host="10.1.1.2", port=161, resource="classpath:linkd/nms4930/dlink_DES-3026.properties"),
            @JUnitSnmpAgent(host="10.1.2.2", port=161, resource="classpath:linkd/nms4930/dlink_DGS-3612G.properties")
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
        
    	final OnmsNode cisco1 = m_nodeDao.findByForeignId("linkd", "cisco1");
        final OnmsNode cisco2 = m_nodeDao.findByForeignId("linkd", "cisco2");

        assertTrue(m_linkd.scheduleNodeCollection(cisco1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco2.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(cisco1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco2.getId()));

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }
        
        // Note By AR: I've inspected the snmp file, only the bridge mib are there
        //             and no link is found
        assertEquals("we should have found no links", 0, ifaces.size());
    }
}
