/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class IpInterfaceDaoTest implements InitializingBean {
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testGetByIpAddress() {
        Collection<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByIpAddress("192.168.1.1");
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
        assertEquals("192.168.1.1", InetAddressUtils.str(iface.getIpAddress()));
    }

    @Test
    @Transactional
    public void testGetByService() {
        List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByServiceType("SNMP");
        Collections.sort(ifaces, new Comparator<OnmsIpInterface>() {
            @Override
            public int compare(OnmsIpInterface o1, OnmsIpInterface o2) {
                return Integer.valueOf(o1.getNode().getId()).compareTo(o2.getNode().getId());
            }
        });

        assertEquals(6, ifaces.size());

        OnmsIpInterface iface = ifaces.iterator().next();
        assertEquals("node1", iface.getNode().getLabel());
        assertEquals(2, iface.getMonitoredServices().size());
        assertEquals("192.168.1.1", InetAddressUtils.str(iface.getIpAddress()));

        OnmsMonitoredService service = iface.getMonitoredServiceByServiceType("SNMP");
        assertNotNull(service);
        assertEquals(addr("192.168.1.1"), service.getIpAddress());
    }

    @Test
    @Transactional
    public void testCountMatchingInterfaces() {
        OnmsCriteria crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("ipAddress", "192.168.1.%"));
        assertEquals(3, m_ipInterfaceDao.countMatching(crit));

        if (Boolean.getBoolean("skipIpv6Tests")) return;

        crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("ipAddress", "fe80:%dddd\\%5"));
        assertEquals(1, m_ipInterfaceDao.countMatching(crit));
    }

    @Test
    @Transactional
    public void testGetIPv6Interfaces() {
        if (Boolean.getBoolean("skipIpv6Tests")) return;

        OnmsCriteria crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("ipAddress", "fe80:%dddd\\%5"));
        List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(crit);
        assertEquals(1, ifaces.size());

        OnmsIpInterface iface = ifaces.get(0);
        assertTrue(iface.getIpAddress() instanceof Inet6Address);
        Inet6Address v6address = (Inet6Address)iface.getIpAddress();
        assertEquals(5, v6address.getScopeId());
        assertEquals("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5", InetAddressUtils.str(iface.getIpAddress()));
    }

    @Test
    @Transactional
    public void testGetInterfacesForNodes() throws UnknownHostException {
        Map<InetAddress, Integer> interfaceNodes = m_ipInterfaceDao.getInterfacesForNodes();
        assertNotNull("interfaceNodes", interfaceNodes);

        for (Entry<InetAddress, Integer> entry : interfaceNodes.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        assertEquals("node ID for 192.168.1.1", m_databasePopulator.getNode1().getId(), interfaceNodes.get(InetAddressUtils.addr("192.168.1.1")));
        assertEquals("node ID for 192.168.1.2", m_databasePopulator.getNode1().getId(), interfaceNodes.get(InetAddressUtils.addr("192.168.1.2")));
        // This hack assumes that the database ID of node 2 is node1.getId() + 1  :)
        assertEquals("node ID for 192.168.2.1", Integer.valueOf(m_databasePopulator.getNode1().getId() + 1), interfaceNodes.get(InetAddressUtils.addr("192.168.2.1")));
        assertFalse("node ID for *BOGUS*IP* should not have been found", interfaceNodes.containsKey("*BOGUS*IP*"));
    }

}
