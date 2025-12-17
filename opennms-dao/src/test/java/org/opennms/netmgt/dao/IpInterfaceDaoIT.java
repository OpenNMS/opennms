/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("deprecation")
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class IpInterfaceDaoIT implements InitializingBean {
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
    public void testPrimaryType() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsIpInterface.class).eq("snmpPrimary", PrimaryType.PRIMARY.getCharCode());
        Collection<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(cb.toCriteria());
        assertEquals(Integer.valueOf(1), ifaces.iterator().next().getIfIndex());

        cb = new CriteriaBuilder(OnmsIpInterface.class).eq("snmpPrimary", PrimaryType.NOT_ELIGIBLE.getCharCode());
        ifaces = m_ipInterfaceDao.findMatching(cb.toCriteria());
        assertEquals(Integer.valueOf(3), ifaces.iterator().next().getIfIndex());
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

        if (Boolean.getBoolean("skipIpv6Tests")) {
            return;
        }

        crit = new OnmsCriteria(OnmsIpInterface.class);
        crit.add(Restrictions.like("ipAddress", "fe80:%dddd\\%5"));
        assertEquals(1, m_ipInterfaceDao.countMatching(crit));
    }

	@Test
    @Transactional
    public void testGetIPv6Interfaces() {
        if (Boolean.getBoolean("skipIpv6Tests")) {
            return;
        }

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
    @SuppressWarnings("unlikely-arg-type")
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

    @Test
    @Transactional
    public void testFindByIpAddressAndLocation() {
        var ipAddress = "192.168.1.1";
        var location = "Default";
        OnmsIpInterface itf = m_ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location).stream().findFirst().orElse(null);
        assertNotNull(itf);
        assertEquals(itf.getIpAddress().getHostAddress(), ipAddress);
        assertEquals(itf.getNode().getLocation().getLocationName(), location);
        OnmsIpInterface itf2 = m_ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location + location).stream().findFirst().orElse(null);
        assertNull(itf2);
    }

    @Test
    @Transactional
    public void testFindInterfacesWithMetadata() {
        OnmsIpInterface ipinterface;

        ipinterface = m_ipInterfaceDao.findByIpAddress("192.168.1.1").get(0);
        ipinterface.addMetaData("context", "key", "value");
        m_ipInterfaceDao.save(ipinterface);

        ipinterface = m_ipInterfaceDao.findByIpAddress("192.168.1.2").get(0);
        ipinterface.addMetaData("context", "key", "foo,bar");
        m_ipInterfaceDao.save(ipinterface);

        ipinterface = m_ipInterfaceDao.findByIpAddress("192.168.1.3").get(0);
        ipinterface.addMetaData("context", "key", "value,foo,bar");
        m_ipInterfaceDao.save(ipinterface);

        ipinterface = m_ipInterfaceDao.findByIpAddress("192.168.2.1").get(0);
        ipinterface.addMetaData("context", "key", "foo,value,bar");
        m_ipInterfaceDao.save(ipinterface);

        ipinterface = m_ipInterfaceDao.findByIpAddress("192.168.2.2").get(0);
        ipinterface.addMetaData("context", "key", "foo,bar,value");
        m_ipInterfaceDao.save(ipinterface);

        m_ipInterfaceDao.flush();

        assertEquals(Sets.newHashSet(),
                m_ipInterfaceDao.findInterfacesWithMetadata("context", "key", "xxx", false).stream()
                        .map(i->i.getIpAddressAsString())
                        .collect(Collectors.toSet()));

        assertEquals(Sets.newHashSet(),
                m_ipInterfaceDao.findInterfacesWithMetadata("context", "key", "xxx", true).stream()
                        .map(i->i.getIpAddressAsString())
                        .collect(Collectors.toSet()));

        assertEquals(Sets.newHashSet("192.168.1.1"),
                m_ipInterfaceDao.findInterfacesWithMetadata("context", "key", "value", false).stream()
                        .map(i->i.getIpAddressAsString())
                        .collect(Collectors.toSet()));

        assertEquals(Sets.newHashSet("192.168.1.1", "192.168.1.3", "192.168.2.1", "192.168.2.2"),
                m_ipInterfaceDao.findInterfacesWithMetadata("context", "key", "value", true).stream()
                        .map(i->i.getIpAddressAsString())
                        .collect(Collectors.toSet()));

    }
}
