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

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.web.BeanUtils;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;

public class DefaultManualProvisioningServiceTest extends TestCase {
    
    private List m_mocks = new ArrayList();

    private DefaultManualProvisioningService m_provisioningService;

    private ManualProvisioningDao m_provisioningDao;
    
    private ModelImport m_testData;
    
    
    @Override
    protected void setUp() throws Exception {
        m_testData = CastorUtils.unmarshal(ModelImport.class, ConfigurationTestUtils.getSpringResourceForResource(this, "/tec_dump.xml"));
        
        m_provisioningDao = createMock(ManualProvisioningDao.class);
        
        m_provisioningService = new DefaultManualProvisioningService();
        m_provisioningService.setProvisioningDao(m_provisioningDao);
        
        
    }


    public void testGetProvisioningGroupNames() {
        
        List<String> expectedGroupNames = new LinkedList<String>();
        
        expect(m_provisioningDao.getProvisioningGroupNames()).andReturn(expectedGroupNames);
        
        replayMocks();
        
        Collection<String> groupNames = m_provisioningService.getProvisioningGroupNames();
        
        verifyMocks();
        
        assertSame(expectedGroupNames, groupNames);
    }
    
    public void testGetProvisioningGroup() {
        String name = "groupName";
        
        ModelImport expected = m_testData;
        expect(m_provisioningDao.get(name)).andReturn(expected);
        
        replayMocks();
        
        ModelImport actual = m_provisioningService.getProvisioningGroup(name);
        
        verifyMocks();
        
        assertSame(expected, actual);
    }
    
    public void testAddNewNodeToGroup() {
        String groupName = "groupName";
        String nodeLabel = "nodeLabel";
        
        ModelImport group = m_testData;
        
        int initialCount = m_testData.getNodeCount();
        
        expect(m_provisioningDao.get(groupName)).andReturn(group).atLeastOnce();
        m_provisioningDao.save(groupName, group);
        
        replayMocks();
        
        ModelImport result = m_provisioningService.addNewNodeToGroup(groupName, nodeLabel);
        
        verifyMocks();
        
        int newCount = result.getNodeCount();
        
        assertEquals(initialCount+1, newCount);
        
        assertEquals(nodeLabel, result.getNode(0).getNodeLabel());
    }
    
    public void testAddCategoryToNode() {
        String groupName = "groupName";
        String pathToNode = "node[0]";
        String categoryName = "categoryName";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToNode+".categoryCount", int.class); 
        
        expect(m_provisioningDao.get(groupName)).andReturn(m_testData).atLeastOnce();
        m_provisioningDao.save(groupName, m_testData);

        replayMocks();
            
        ModelImport result = m_provisioningService.addCategoryToNode(groupName, pathToNode, categoryName);
        
        verifyMocks();
        
        int newCount = BeanUtils.getPathValue(result, pathToNode+".categoryCount", int.class);
        
        assertEquals(initialCount+1, newCount);
        Category newCategory = BeanUtils.getPathValue(result, pathToNode+".category[0]", Category.class);
        assertNotNull(newCategory);
        assertEquals(categoryName, newCategory.getName());
    }
    
    public void testAddInterfaceToNode() {
        String groupName = "groupName";
        String pathToNode = "node[0]";
        String ipAddr = "10.1.1.1";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToNode+".interfaceCount", int.class); 

        expect(m_provisioningDao.get(groupName)).andReturn(m_testData).atLeastOnce();
        m_provisioningDao.save(groupName, m_testData);

        replayMocks();
        
        ModelImport result = m_provisioningService.addInterfaceToNode(groupName, pathToNode, ipAddr);
        
        verifyMocks();

        int newCount = BeanUtils.getPathValue(result, pathToNode+".interfaceCount", int.class); 
        
        assertEquals(initialCount+1, newCount);
        
        Interface newIface = BeanUtils.getPathValue(result, pathToNode+".interface[0]", Interface.class);
        assertNotNull(newIface);
        assertEquals(ipAddr, newIface.getIpAddr());
    }
    
    public void testAddServiceToInterface() {
        String groupName = "groupName";
        String pathToInterface = "node[0].interface[0]";
        String serviceName = "SVC";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 

        expect(m_provisioningDao.get(groupName)).andReturn(m_testData).atLeastOnce();
        m_provisioningDao.save(groupName, m_testData);

        replayMocks();

        ModelImport result = m_provisioningService.addServiceToInterface(groupName, pathToInterface, serviceName);

        verifyMocks();
        
        int newCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        
        assertEquals(initialCount+1, newCount);
        
        MonitoredService svc = BeanUtils.getPathValue(result, pathToInterface+".monitoredService[0]", MonitoredService.class);
        assertNotNull(svc);
        assertEquals(serviceName, svc.getServiceName());
    }
    
    public void testDeletePath() {
        String groupName = "groupName";
        String pathToInterface = "node[0].interface[0]";
        String pathToDelete = pathToInterface+".monitoredService[0]";
        
        int initialCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        String svcName = BeanUtils.getPathValue(m_testData, pathToDelete+".serviceName", String.class);

        expect(m_provisioningDao.get(groupName)).andReturn(m_testData).atLeastOnce();
        m_provisioningDao.save(groupName, m_testData);

        replayMocks();

        ModelImport result = m_provisioningService.deletePath(groupName, pathToDelete);

        verifyMocks();
        
        int newCount = BeanUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        
        assertEquals(initialCount-1, newCount);
        
        MonitoredService svc = BeanUtils.getPathValue(result, pathToInterface+".monitoredService[0]", MonitoredService.class);
        assertNotNull(svc);
        assertFalse(svc.getServiceName().equals(svcName));
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createMock(Class<T> name) {
        T mock = EasyMock.createMock(name);
        m_mocks.add(mock);
        return mock;
    }

    private void verifyMocks() {
        EasyMock.verify(m_mocks.toArray());
    }

    private void replayMocks() {
        EasyMock.replay(m_mocks.toArray());
    }

}
