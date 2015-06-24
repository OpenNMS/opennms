/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.hardware.HwExtension;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.provision.SnmpHardwareInventoryProvisioningAdapter;
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

/**
 * The Test Class for SnmpHardwareInventoryProvisioningAdapter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host="192.168.0.1", resource="entPhysicalTable-cisco-r1.properties"),
        @JUnitSnmpAgent(host="192.168.0.2", resource="entPhysicalTable-cisco-r2.properties"),
        @JUnitSnmpAgent(host="192.168.0.3", resource="entPhysicalTable-cisco-r3.properties"),
        @JUnitSnmpAgent(host="192.168.0.4", resource="entPhysicalTable-cisco-r4.properties")
})
public class SnmpHardwareInventoryProvisioningAdapterTest implements InitializingBean {

    /**
     * The Class TestOperation.
     */
    public final static class TestOperation {

        /** The node id. */
        public Integer nodeId;

        /** The operation. */
        public AdapterOperation operation;

        /**
         * The Constructor.
         *
         * @param nodeId the node id
         * @param operation the operation
         */
        public TestOperation(Integer nodeId, AdapterOperation operation) {
            super();
            this.nodeId = nodeId;
            this.operation = operation;
        }
    }

    /** The SNMP Hardware Provisioning Adapter. */
    @Autowired
    private SnmpHardwareInventoryProvisioningAdapter m_adapter;

    /** The node DAO. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The entity DAO. */
    @Autowired
    private HwEntityDao m_entityDao;

    /** The operations. */
    private List<TestOperation> m_operations = new ArrayList<TestOperation>();

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @BeforeTransaction
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);

        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode("R1").setForeignSource("Cisco").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.1").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("R2").setForeignSource("Cisco").setForeignId("2").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.2").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("R3").setForeignSource("Cisco").setForeignId("3").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.3").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("R4").setForeignSource("Cisco").setForeignId("4").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.4").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

        m_adapter.afterPropertiesSet();

        for (int i=1; i<=4; i++) {
            Integer nodeId = m_nodeDao.findByForeignId("Cisco", Integer.toString(i)).getId();
            AdapterOperationSchedule ops = new AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS);        
            AdapterOperation op = m_adapter.new AdapterOperation(nodeId, AdapterOperationType.ADD, ops);
            m_operations.add(new TestOperation(nodeId, op));
        }
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @AfterTransaction
    public void tearDown() throws Exception{
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test discover SNMP entities.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testDiscoverSnmpEntities() throws Exception {
        HwInventoryAdapterConfiguration config = m_adapter.getHwAdapterConfigDao().getConfiguration();
        Assert.assertEquals(3, config.getExtensions().size());
        Assert.assertEquals(5, config.getExtensions().get(0).getMibObjects().size());
        Assert.assertEquals(12, config.getExtensions().get(1).getMibObjects().size());
        Assert.assertEquals(10, config.getExtensions().get(2).getMibObjects().size());

        HwExtension ext = config.getExtensions().get(0);
        Assert.assertEquals("CISCO-ENTITY-EXT-MIB", ext.getName());
        Assert.assertEquals(5, ext.getMibObjects().size());

        ext = config.getExtensions().get(1);
        Assert.assertEquals("CISCO-ENTITY-ASSET-MIB", ext.getName());
        Assert.assertEquals(12, ext.getMibObjects().size());

        Assert.assertEquals(27, m_adapter.getVendorAttributeMap().size());

        for (TestOperation op : m_operations) {
            m_adapter.processPendingOperationForNode(op.operation);

            OnmsHwEntity root = m_entityDao.findRootByNodeId(op.nodeId);
            Assert.assertNotNull(root);
            Assert.assertTrue(root.isRoot());
            FileWriter w = new FileWriter("target/" + op.nodeId + ".xml");
            JaxbUtils.marshal(root, w);
            w.close();

            m_nodeDao.flush();
            m_entityDao.flush();
        }

        Assert.assertEquals(112, m_entityDao.countAll());
    }

}
