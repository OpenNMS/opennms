/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.RequisitionDao;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionInterfaceEntity;
import org.opennms.netmgt.model.requisition.RequisitionMonitoredServiceEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class RequisitionDaoIT {

    @Autowired
    private RequisitionDao requisitionDao;

    @Test
    @Transactional
    public void testCreateRequisition() {
        // create
        Date lastUpdate = new Date();
        Date lastImport = new Date();
        RequisitionEntity requisition = new RequisitionEntity();
        requisition.setName("dummy");
        requisition.setLastUpdate(lastUpdate);
        requisition.setLastImport(lastImport);

        RequisitionNodeEntity node = new RequisitionNodeEntity();
        node.setRequisition(requisition);
        node.setCity("Frankfurt");
        node.setBuilding("H1");
        node.setNodeLabel("my label");
        node.setForeignId("1234");
        node.setLocation("localhost");
        node.setParentForeignId("parent 1000");
        node.setParentForeignSource("default");
        node.setParentNodeLabel("parent label");
        node.addAsset("my-key", "my-value");
        node.addCategory("node-category-1");
        node.setRequisition(requisition);

        RequisitionInterfaceEntity nodeInterface = new RequisitionInterfaceEntity();
        nodeInterface.setIpAddress("127.0.0.1");
        nodeInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        nodeInterface.setDescription("some description");
        nodeInterface.addCategory("interface-category-1");
        nodeInterface.setManaged(true);
        nodeInterface.setNode(node);

        RequisitionMonitoredServiceEntity interfaceService = new RequisitionMonitoredServiceEntity();
        interfaceService.setServiceName("SSH");
        interfaceService.addCategory("service-category-1");
        interfaceService.setIpInterface(nodeInterface);

        // relate
        nodeInterface.addMonitoredService(interfaceService);
        node.addInterface(nodeInterface);
        requisition.addNode(node);

        // save
        requisitionDao.save(requisition);
        requisitionDao.flush(); // force validation

        // validate
        RequisitionEntity readRequisition = requisitionDao.get("dummy");
        Assert.assertNotNull(readRequisition);
        Assert.assertEquals(lastUpdate, readRequisition.getLastUpdate());
        Assert.assertEquals(lastImport, readRequisition.getLastImport());
        Assert.assertEquals("dummy", readRequisition.getName());
        Assert.assertEquals("dummy", readRequisition.getForeignSource());
        Assert.assertEquals(1, readRequisition.getNodes().size());

        RequisitionNodeEntity readNode = readRequisition.getNode("1234");
        Assert.assertTrue("No node id set", readNode.getId() > 0);
        Assert.assertNotNull(readNode);
        Assert.assertEquals(node.getBuilding(), readNode.getBuilding());
        Assert.assertEquals(node.getCity(), readNode.getCity());
        Assert.assertEquals(node.getForeignId(), readNode.getForeignId());
        Assert.assertEquals(node.getForeignSource(), readNode.getForeignSource());
        Assert.assertEquals(node.getLocation(), readNode.getLocation());
        Assert.assertEquals(node.getParentForeignId(), readNode.getParentForeignId());
        Assert.assertEquals(node.getParentForeignSource(), readNode.getParentForeignSource());
        Assert.assertEquals(node.getParentNodeLabel(), readNode.getParentNodeLabel());
        Assert.assertSame(node.getRequisition(), readRequisition);
        Assert.assertEquals(1, readNode.getCategories().size());
        Assert.assertEquals(1 + 2, readNode.getAssets().size()); // city and building + custom asset
        Assert.assertEquals(1, readNode.getInterfaces().size());

        RequisitionInterfaceEntity readInterface = readNode.getInterface("127.0.0.1");
        Assert.assertNotNull(readInterface);
        Assert.assertTrue("No interface id set", readInterface.getId() > 0);
        Assert.assertEquals(nodeInterface.getIpAddress(), readInterface.getIpAddress());
        Assert.assertEquals(nodeInterface.getDescription(), readInterface.getDescription());
        Assert.assertEquals(nodeInterface.getSnmpPrimary(), readInterface.getSnmpPrimary());
        Assert.assertEquals(nodeInterface.getStatus(), readInterface.getStatus());
        Assert.assertEquals(1, readInterface.getCategories().size());
        Assert.assertEquals(1, readInterface.getMonitoredServices().size());

        RequisitionMonitoredServiceEntity readService = readInterface.getMonitoredService("SSH");
        Assert.assertNotNull(readService);
        Assert.assertTrue("No service id set", readService.getId() > 0);
        Assert.assertEquals(interfaceService.getServiceName(), readService.getServiceName());
        Assert.assertEquals(1, readService.getCategories().size());

    }
}
