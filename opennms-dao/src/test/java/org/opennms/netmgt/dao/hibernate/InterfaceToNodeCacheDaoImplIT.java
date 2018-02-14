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

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class InterfaceToNodeCacheDaoImplIT implements InitializingBean {

    @Autowired
    InterfaceToNodeCache m_cache;

    @Autowired
    DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_databasePopulator.populateDatabase();
        m_cache.clear();
    }

    @Test
    @Transactional
    public void testGetId() throws Exception {
        m_cache.dataSourceSync();

        final InetAddress ipAddr = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress();
        final int expectedNodeId = m_databasePopulator.getNode2().getId();

        final int nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);
    }

    @Test
    @Transactional
    public void testSetId() throws Exception {
        final int nodeId = m_databasePopulator.getNode1().getId();

        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.3"), nodeId));
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.2"), nodeId));
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.1"), nodeId));
        Assert.assertEquals(false, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.3"), nodeId));
    }

    @Test
    @Transactional
    public void testSetIdWithDifferentNodes() throws Exception {
        m_cache.dataSourceSync();

        final OnmsNode node = new OnmsNode(m_databasePopulator.getMonitoringLocationDao().getDefaultLocation(), "my-new-node");
        node.setForeignSource("junit");
        node.setForeignId("10001");
        final OnmsIpInterface iface = new OnmsIpInterface(InetAddress.getByName("192.168.1.2"), node);
        iface.setIsManaged("M");
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        final OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(node, 1001);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        node.addIpInterface(iface);
        m_databasePopulator.getNodeDao().save(node);

        // Different node ID must return true again
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress(), node.getId()));

        // Cache must return both results
        Assert.assertTrue(Iterables.contains(m_cache.getNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress()), node.getId()));
        Assert.assertTrue(Iterables.contains(m_cache.getNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress()), m_databasePopulator.getNode1().getId()));

        // Primary must be first
        Assert.assertEquals(m_databasePopulator.getNode1().getId(), m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress()).get());
    }

    @Test
    @Transactional
    public void testNullLocation() throws Exception {
        m_cache.dataSourceSync();

        // Retrieve a known entry stored using the default location id
        InetAddress ipAddr = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress();
        long expectedNodeId = Long.parseLong(m_databasePopulator.getNode2().getNodeId());
        long nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);

        // Now try retrieving that same node using null for the location
        nodeId = m_cache.getFirstNodeId(null, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);
    }
}
