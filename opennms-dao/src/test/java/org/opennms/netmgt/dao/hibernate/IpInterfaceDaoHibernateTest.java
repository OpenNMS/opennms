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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
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
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class IpInterfaceDaoHibernateTest implements InitializingBean {

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
        OnmsIpInterface iface = new OnmsIpInterface(m_testAddress.getHostAddress(), n1);
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
