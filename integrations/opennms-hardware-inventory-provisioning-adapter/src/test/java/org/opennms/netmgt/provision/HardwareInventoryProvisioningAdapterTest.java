/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationSchedule;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false)
@JUnitSnmpAgent(host = "192.168.0.1", resource = "entPhysicalTable-cisco.properties")
public class HardwareInventoryProvisioningAdapterTest implements InitializingBean {

    @Autowired
    private HardwareInventoryProvisioningAdapter m_adapter;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private HwEntityDao m_entityDao;

    private AdapterOperation m_addOperation;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);

        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("HQ").setForeignSource("Cisco").setForeignId("1");
        nb.addInterface("192.168.0.1").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        m_adapter.afterPropertiesSet();

        Integer nodeId = m_nodeDao.findByForeignId("Cisco", "1").getId();
        AdapterOperationSchedule op = new AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS);
        m_addOperation = m_adapter.new AdapterOperation(nodeId, AdapterOperationType.ADD, op);
    }

    @AfterTransaction
    public void tearDown() throws Exception{
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @Transactional
    public void testAdd() throws Exception {
        int nodeId = m_nodeDao.findByForeignId("Cisco", "1").getId();
        m_adapter.processPendingOperationForNode(m_addOperation);
        OnmsHwEntity root = m_entityDao.findRootByNodeId(nodeId);
        Assert.assertNotNull(root);
        StringWriter w = new StringWriter();
        JaxbUtils.marshal(root, w);
        System.out.println(w);
    }

}
