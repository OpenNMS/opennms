//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2011 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.hibernate;

import java.util.Map;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class IpInterfaceDaoHibernateTest extends AbstractTransactionalDaoTestCase {

    private Integer m_node2Id;
    private String m_node2IpAddress;

    @Override
    protected void onSetUpInTransactionIfEnabled() {
        super.onSetUpInTransactionIfEnabled();

        // Get Node2 Information
        OnmsNode n2 = getNodeDao().findByForeignId("imported:", "2");
        m_node2Id = n2.getId();
        assertNotNull(m_node2Id);
        m_node2IpAddress = n2.getPrimaryInterface().getIpAddress();
        assertNotNull(m_node2IpAddress);

        // Adding the test address as a secondary address (unmanaged) to Node1
        OnmsNode n1 = getNode1();
        OnmsIpInterface iface = new OnmsIpInterface(m_node2IpAddress, n1);
        iface.setIsManaged("U");
        iface.setIsSnmpPrimary(PrimaryType.SECONDARY);
        OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(m_node2IpAddress, 1001, n1);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        n1.addIpInterface(iface);
        getNodeDao().save(n1); 
        getNodeDao().flush();
    }

    public void testNMS4822() throws Exception {
        // Verify that test IP address exists on Node1 as non-primary
        OnmsIpInterface ipIntf = getIpInterfaceDao().findByNodeIdAndIpAddress(getNode1().getId(), m_node2IpAddress);
        assertFalse(ipIntf.isPrimary());
        assertFalse(ipIntf.isManaged());

        // Verify that test IP address exists on Node2 as primary
        ipIntf = getIpInterfaceDao().findByNodeIdAndIpAddress(m_node2Id, m_node2IpAddress);
        assertTrue(ipIntf.isPrimary());
        assertTrue(ipIntf.isManaged());

        // Get Interfaces For Nodes Map
        Map<String, Integer> map = getIpInterfaceDao().getInterfacesForNodes();
        assertNotNull(map);

        // Verify that the test address is associated with Node2 because primary addresses has precedence over non-primary addresses.
        assertEquals(m_node2Id, map.get(m_node2IpAddress));
    }

}
