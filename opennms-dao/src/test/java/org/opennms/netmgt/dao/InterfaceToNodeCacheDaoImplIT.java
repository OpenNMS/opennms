/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class InterfaceToNodeCacheDaoImplIT implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;
    @Autowired
    private InterfaceToNodeCache m_interfaceToNodeCache;

    private static boolean m_populated = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testDuplicate() throws Exception {
        final OnmsMonitoringLocation defaultLocation = m_monitoringLocationDao.getDefaultLocation();

        final InetAddress theAddress = InetAddress.getByName("1.2.3.4");

        Assert.assertNotNull(m_interfaceToNodeCache);

        final OnmsNode node1 = new OnmsNode(defaultLocation,"node1");
        final OnmsIpInterface iface1 = new OnmsIpInterface();
        iface1.setIpAddress(theAddress);
        iface1.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node1.addIpInterface(iface1);
        final int nodeId1 = m_nodeDao.save(node1);

        m_interfaceToNodeCache.setNodeId(defaultLocation.getLocationName(), iface1.getIpAddress(), node1.getId());

        Assert.assertEquals(nodeId1, (int) m_interfaceToNodeCache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());

        final OnmsNode node2 = new OnmsNode(defaultLocation,"node2");
        final OnmsIpInterface iface2 = new OnmsIpInterface();
        iface2.setIpAddress(theAddress);
        iface2.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node2.addIpInterface(iface2);
        final int nodeId2 = m_nodeDao.save(node2);

        m_interfaceToNodeCache.setNodeId(defaultLocation.getLocationName(), iface2.getIpAddress(), node2.getId());

        Assert.assertEquals(nodeId1, (int) m_interfaceToNodeCache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());

        m_interfaceToNodeCache.removeNodeId(defaultLocation.getLocationName(), theAddress, nodeId1);

        Assert.assertEquals(nodeId2, (int) m_interfaceToNodeCache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());
    }
}
