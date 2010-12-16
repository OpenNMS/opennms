//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Add test for getInterfacesForNodes. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;


public class IpInterfaceDaoTest extends AbstractTransactionalDaoTestCase {

    public void testGetByIpAddress() {
        Collection<OnmsIpInterface> ifaces = getIpInterfaceDao().findByIpAddress("192.168.1.1");
        assertEquals(1, ifaces.size());
        OnmsIpInterface iface = ifaces.iterator().next();
        assertEquals("node1", iface.getNode().getLabel());
        
        int count = 0;
        for (Iterator<OnmsMonitoredService> it = iface.getMonitoredServices().iterator(); it.hasNext();) {
            it.next();
            count++;
        }
        
        assertEquals(2, count);
        assertEquals(2, iface.getMonitoredServices().size());
        assertEquals("192.168.1.1", iface.getInetAddress().getHostAddress());
    }
    
    public void testGetByService() {
        List<OnmsIpInterface> ifaces = getIpInterfaceDao().findByServiceType("SNMP");
        Collections.sort(ifaces, new Comparator<OnmsIpInterface>() {
            public int compare(OnmsIpInterface o1, OnmsIpInterface o2) {
                return new Integer(o1.getNode().getId()).compareTo(o2.getNode().getId());
            }
        });
        
        assertEquals(6, ifaces.size());
        
        OnmsIpInterface iface = ifaces.iterator().next();
        assertEquals("node1", iface.getNode().getLabel());
        assertEquals(2, iface.getMonitoredServices().size());
        assertEquals("192.168.1.1", iface.getInetAddress().getHostAddress());
        
        OnmsMonitoredService service = iface.getMonitoredServiceByServiceType("SNMP");
        assertNotNull(service);
        assertEquals("192.168.1.1", service.getIpAddress());
    }
    
    public void testCountMatchingInerfaces() {
        OnmsCriteria crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("inetAddress", "192.168.1.%"));
        assertEquals(3, getIpInterfaceDao().countMatching(crit));
        
        crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("inetAddress", "fe80:%dddd\\%5"));
        assertEquals(1, getIpInterfaceDao().countMatching(crit));
    }

    public void testGetIPv6Interfaces() {
        OnmsCriteria crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("inetAddress", "fe80:%dddd\\%5"));
        List<OnmsIpInterface> ifaces = getIpInterfaceDao().findMatching(crit);
        assertEquals(1, ifaces.size());
        
        OnmsIpInterface iface = ifaces.get(0);
        assertTrue(iface.getInetAddress() instanceof Inet6Address);
        Inet6Address v6address = (Inet6Address)iface.getInetAddress();
        assertEquals(5, v6address.getScopeId());
        assertEquals("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5", iface.getIpAddress());
    }

    public void testGetInterfacesForNodes() throws UnknownHostException {
        Map<InetAddress, Integer> interfaceNodes = getIpInterfaceDao().getInterfacesForNodes();
        assertNotNull("interfaceNodes", interfaceNodes);
        
        for (Entry<InetAddress, Integer> entry : interfaceNodes.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        assertEquals("node ID for 192.168.1.1", new Integer(1), interfaceNodes.get(InetAddress.getByName("192.168.1.1")));
        assertEquals("node ID for 192.168.1.2", new Integer(1), interfaceNodes.get(InetAddress.getByName("192.168.1.2")));
        assertEquals("node ID for 192.168.2.1", new Integer(2), interfaceNodes.get(InetAddress.getByName("192.168.2.1")));
        assertFalse("node ID for *BOGUS*IP* should not have been found", interfaceNodes.containsKey("*BOGUS*IP*"));
    }
    
}
