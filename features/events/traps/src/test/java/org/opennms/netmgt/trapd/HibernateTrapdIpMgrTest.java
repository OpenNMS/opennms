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

package org.opennms.netmgt.trapd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
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
        "classpath:/org/opennms/netmgt/trapd/trapdIpMgr-test.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HibernateTrapdIpMgrTest implements InitializingBean {

    @Autowired
    TrapdIpMgr m_trapdIpMgr;

    @Autowired
    DatabasePopulator m_databasePopulator;

    int m_testNodeId;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_databasePopulator.populateDatabase();
        m_trapdIpMgr.dataSourceSync();

        OnmsNode n = new OnmsNode(m_databasePopulator.getDistPollerDao().get("localhost"), "my-new-node");
        n.setForeignSource("junit");
        n.setForeignId("10001");
        OnmsIpInterface iface = new OnmsIpInterface("192.168.1.3", n);
        iface.setIsManaged("M");
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(n, 1001);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        n.addIpInterface(iface);
        m_databasePopulator.getNodeDao().save(n);
        m_testNodeId = n.getId();
    }

    @Test
    @Transactional
    public void testTrapdIpMgrSetId() throws Exception {
        String ipAddr = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress().getHostAddress();
        long expectedNodeId = Long.parseLong(m_databasePopulator.getNode2().getNodeId());

        long nodeId = m_trapdIpMgr.getNodeId(ipAddr);
        Assert.assertEquals(expectedNodeId, nodeId);

        // Address already exists on database and it is not primary.
        Assert.assertEquals(-1, m_trapdIpMgr.setNodeId("192.168.1.3", 1));

        // Address already exists on database but the new node also contain the address and is their primary address.
        Assert.assertEquals(1, m_trapdIpMgr.setNodeId("192.168.1.3", m_testNodeId)); // return old nodeId
        Assert.assertEquals(m_testNodeId, m_trapdIpMgr.getNodeId("192.168.1.3")); // return the new nodeId
    }

}
