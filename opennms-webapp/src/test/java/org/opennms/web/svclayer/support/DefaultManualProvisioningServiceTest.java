//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.web.BeanUtils;

public class DefaultManualProvisioningServiceTest extends TestCase {
    
    private DefaultManualProvisioningService m_provisioningService;

    private Requisition m_testData;
    
    private ForeignSourceRepository m_repository = new MockForeignSourceRepository();
    
    @Override
    protected void setUp() throws Exception {
        m_testData = m_repository.deployResourceRequisition(ConfigurationTestUtils.getSpringResourceForResource(this, "/tec_dump.xml"));

        m_provisioningService = new DefaultManualProvisioningService();
        m_provisioningService.setForeignSourceRepository(m_repository);
    }

    public void testGetProvisioningGroupNames() {
        Collection<String> expected = new ArrayList<String>();
        expected.add("matt:");
        Collection<String> groupNames = m_provisioningService.getProvisioningGroupNames();
        assertEquals(expected, groupNames);
    }
    
    public void testGetProvisioningGroup() {
        String name = "matt:";
        
        Requisition expected = m_testData;
        Requisition actual = m_provisioningService.getProvisioningGroup(name);
        assertEquals(expected, actual);
    }
    
    public void testAddNewNodeToGroup() {
        String groupName = "matt:";
        String nodeLabel = "david";
        
        int initialCount = m_testData.getNodes().size();

        Requisition result = m_provisioningService.addNewNodeToGroup(groupName, nodeLabel);
        
        int newCount = result.getNodes().size();
        
        assertEquals(initialCount+1, newCount);
        assertEquals(nodeLabel, result.getNodes().get(0).getNodeLabel());
    }
    
    public void testAddCategoryToNode() {
        String groupName = "matt:";
        String pathToNode = "node[0]";
        String categoryName = "categoryName";

        int initialCount = BeanUtils.getPathValue(m_testData, pathToNode+".categoryCount", int.class); 
        
        Requisition result = m_provisioningService.addCategoryToNode(groupName, pathToNode, categoryName);
        
        int newCount = BeanUtils.getPathValue(result, pathToNode+".categoryCount", int.class);
        
        assertEquals(initialCount+1, newCount);
        RequisitionCategory newCategory = BeanUtils.getPathValue(result, pathToNode+".category[0]", RequisitionCategory.class);
        assertNotNull(newCategory);
        assertEquals(categoryName, newCategory.getName());
    }
    
    public void testAddInterfaceToNode() {
        String groupName = "matt:";
        String pathToNode = "node[0]";
        String ipAddr = "10.1.1.1";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToNode+".interfaceCount", int.class); 

        Requisition result = m_provisioningService.addInterfaceToNode(groupName, pathToNode, ipAddr);
        
        int newCount = BeanUtils.getPathValue(result, pathToNode+".interfaceCount", int.class); 
        
        assertEquals(initialCount+1, newCount);
        RequisitionInterface newIface = BeanUtils.getPathValue(result, pathToNode+".interface[0]", RequisitionInterface.class);
        assertNotNull(newIface);
        assertEquals(ipAddr, newIface.getIpAddr());
    }
    
    public void testAddServiceToInterface() {
        String groupName = "matt:";
        String pathToInterface = "node[0].interface[0]";
        String serviceName = "SVC";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 

        Requisition result = m_provisioningService.addServiceToInterface(groupName, pathToInterface, serviceName);

        int newCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        assertEquals(initialCount+1, newCount);
        RequisitionMonitoredService svc = BeanUtils.getPathValue(result, pathToInterface+".monitoredService[0]", RequisitionMonitoredService.class);
        assertNotNull(svc);
        assertEquals(serviceName, svc.getServiceName());
    }
    
    public void testDeletePath() {
        String groupName = "matt:";
        String pathToInterface = "node[0].interface[0]";
        String pathToDelete = pathToInterface+".monitoredService[0]";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        String svcName = BeanUtils.getPathValue(m_testData, pathToDelete+".serviceName", String.class);

        Requisition result = m_provisioningService.deletePath(groupName, pathToDelete);
        int newCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        assertEquals(initialCount-1, newCount);

        RequisitionMonitoredService svc = BeanUtils.getPathValue(result, pathToInterface+".monitoredService[0]", RequisitionMonitoredService.class);
        assertNotNull(svc);
        assertFalse(svc.getServiceName().equals(svcName));
    }
    
}
