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
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class IpInterfaceDaoHibernateIT implements InitializingBean {

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private InetAddress m_testAddress;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();

        // Set the primary address of Node2 as the test address.
        m_testAddress = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress();

        // Adding the test address as a secondary address (unmanaged) to Node1
        OnmsNode n1 = m_databasePopulator.getNode1();
        OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr(m_testAddress.getHostAddress()), n1);
        iface.setIsManaged("U");
        iface.setIsSnmpPrimary(PrimaryType.SECONDARY);
        OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(n1, 1001);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        n1.addIpInterface(iface);
        m_databasePopulator.getNodeDao().save(n1);
    }

    @Test
    @Transactional
    public void testPrimaryType() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsIpInterface.class).eq("snmpPrimary", PrimaryType.PRIMARY.getCharCode());
        List<OnmsIpInterface> ifaces = new ArrayList<>(m_ipInterfaceDao.findMatching(cb.toCriteria()));
        assertEquals(Integer.valueOf(1), ifaces.get(0).getIfIndex());

        cb = new CriteriaBuilder(OnmsIpInterface.class).eq("snmpPrimary", PrimaryType.NOT_ELIGIBLE.getCharCode());
        ifaces = m_ipInterfaceDao.findMatching(cb.toCriteria());
        assertEquals(Integer.valueOf(3), ifaces.get(0).getIfIndex());

        cb = new CriteriaBuilder(OnmsIpInterface.class).eq("snmpPrimary", PrimaryType.SECONDARY.getCharCode()).eq("isManaged", "U");
        ifaces = m_ipInterfaceDao.findMatching(cb.toCriteria());
        assertEquals(1, ifaces.size());

        assertEquals(Integer.valueOf(1001), ifaces.iterator().next().getIfIndex());
    }

    @Test
    @Transactional
    public void testNMS4822() throws Exception {
        // Verify that test IP address exists on Node1 as non-primary
        OnmsIpInterface ipIntf = m_ipInterfaceDao.findByNodeIdAndIpAddress(m_databasePopulator.getNode1().getId(), m_testAddress.getHostAddress());
        assertFalse(ipIntf.isPrimary());
        assertFalse(ipIntf.isManaged());

        // Verify that test IP address exists on Node2 as primary
        ipIntf = m_ipInterfaceDao.findByNodeIdAndIpAddress(m_databasePopulator.getNode2().getId(), m_testAddress.getHostAddress());
        assertTrue(ipIntf.isPrimary());
        assertTrue(ipIntf.isManaged());

        // Get Interfaces For Nodes Map
        Map<InetAddress, Integer> map = m_ipInterfaceDao.getInterfacesForNodes();
        assertNotNull(map);

        // Verify that the test address is associated with Node2 because primary addresses has precedence over non-primary addresses.
        assertEquals(Integer.valueOf(m_databasePopulator.getNode2().getId()), map.get(m_testAddress));
    }

}
