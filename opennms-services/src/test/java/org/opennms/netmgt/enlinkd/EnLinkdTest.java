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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SYSOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;

public class EnLinkdTest extends EnLinkdTestBuilder {

	Nms10205bNetworkBuilder builder10205a = new Nms10205bNetworkBuilder();
	Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();    

    @Test
    public void testGetSnmpNodeList() throws Exception {
        m_nodeDao.save(builder10205a.getMumbai());
        m_nodeDao.save(builder10205a.getDelhi());
        m_nodeDao.save(builder.getSwitch1());

        m_nodeDao.flush();

        final int mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME).getId().intValue();
        final int delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME).getId().intValue();
        final int switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME).getId().intValue();
        

        List<LinkableSnmpNode> linkablenodes = m_linkd.getQueryManager().getSnmpNodeList();
        assertNotNull(linkablenodes);
        assertEquals(3, linkablenodes.size());
        
        for (LinkableSnmpNode linkablenode: linkablenodes) {
        	if (linkablenode.getNodeId() == mumbai) {
        		assertEquals(InetAddressUtils.addr(MUMBAI_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(MUMBAI_SYSOID,linkablenode.getSysoid());
        	} else if (linkablenode.getNodeId() == delhi) {
        		assertEquals(InetAddressUtils.addr(DELHI_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(DELHI_SYSOID,linkablenode.getSysoid());
        	} else if (linkablenode.getNodeId() == switch1) {
        		assertEquals(InetAddressUtils.addr(SWITCH1_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(SWITCH1_SYSOID,linkablenode.getSysoid());
        	} else {
        		assertTrue(false);
        	}
        }

        LinkableSnmpNode delhilinkablenode = m_linkd.getQueryManager().getSnmpNode(delhi);
        assertNotNull(delhilinkablenode);
		assertEquals(delhi, delhilinkablenode.getNodeId());
		assertEquals(InetAddressUtils.addr(DELHI_IP), delhilinkablenode.getSnmpPrimaryIpAddr());
		assertEquals(DELHI_SYSOID,delhilinkablenode.getSysoid());
        
    }
}
