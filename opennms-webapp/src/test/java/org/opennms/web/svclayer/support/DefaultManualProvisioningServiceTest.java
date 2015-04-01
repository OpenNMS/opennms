/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.mock.MockServiceTypeDao;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

public class DefaultManualProvisioningServiceTest {
    
    private DefaultManualProvisioningService m_provisioningService;

    private Requisition m_testData;
    
    private ForeignSourceRepository m_activeRepository = new MockForeignSourceRepository();
    private ForeignSourceRepository m_pendingRepository = new MockForeignSourceRepository();
    private MockServiceTypeDao m_serviceTypeDao = new MockServiceTypeDao();

    @Before
    public void setUp() throws Exception {
        m_testData = m_activeRepository.importResourceRequisition(ConfigurationTestUtils.getSpringResourceForResource(this, "/tec_dump.xml"));

        m_provisioningService = new DefaultManualProvisioningService();
        m_provisioningService.setDeployedForeignSourceRepository(m_activeRepository);
        m_provisioningService.setPendingForeignSourceRepository(m_pendingRepository);
        m_provisioningService.setServiceTypeDao(m_serviceTypeDao);
    }

    @Test
    public void testGetProvisioningGroupNames() {
        Set<String> expected = new TreeSet<String>();
        expected.add("matt:");
        Collection<String> groupNames = m_provisioningService.getProvisioningGroupNames();
        assertEquals(expected, groupNames);
    }

    @Test
    public void testGetProvisioningGroup() {
        String name = "matt:";
        
        Requisition expected = m_testData;
        Requisition actual = m_provisioningService.getProvisioningGroup(name);
        assertEquals(expected, actual);
    }

    @Test
    public void testAddNewNodeToGroup() {
        String groupName = "matt:";
        String nodeLabel = "david";
        
        int initialCount = m_testData.getNodes().size();

        Requisition result = m_provisioningService.addNewNodeToGroup(groupName, nodeLabel);
        
        int newCount = result.getNodes().size();
        
        assertEquals(initialCount+1, newCount);
        assertEquals(nodeLabel, result.getNodes().get(0).getNodeLabel());
    }

    @Test
    public void testAddCategoryToNode() {
        String groupName = "matt:";
        String pathToNode = "node[0]";
        String categoryName = "categoryName";

        int initialCount = PropertyUtils.getPathValue(m_testData, pathToNode+".categoryCount", int.class); 
        
        Requisition result = m_provisioningService.addCategoryToNode(groupName, pathToNode, categoryName);
        
        int newCount = PropertyUtils.getPathValue(result, pathToNode+".categoryCount", int.class);
        
        assertEquals(initialCount+1, newCount);
        RequisitionCategory newCategory = PropertyUtils.getPathValue(result, pathToNode+".category[0]", RequisitionCategory.class);
        assertNotNull(newCategory);
        assertEquals(categoryName, newCategory.getName());
    }

    @Test
    public void testAddInterfaceToNode() {
        String groupName = "matt:";
        String pathToNode = "node[0]";
        String ipAddr = "10.1.1.1";
        
        int initialCount = PropertyUtils.getPathValue(m_testData, pathToNode+".interfaceCount", int.class); 

        Requisition result = m_provisioningService.addInterfaceToNode(groupName, pathToNode, ipAddr);
        
        int newCount = PropertyUtils.getPathValue(result, pathToNode+".interfaceCount", int.class); 
        
        assertEquals(initialCount+1, newCount);
        RequisitionInterface newIface = PropertyUtils.getPathValue(result, pathToNode+".interface[0]", RequisitionInterface.class);
        assertNotNull(newIface);
        assertEquals(ipAddr, newIface.getIpAddr());
    }

    @Test
    public void testAddServiceToInterface() {
        String groupName = "matt:";
        String pathToInterface = "node[0].interface[0]";
        String serviceName = "SVC";
        
        int initialCount = PropertyUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 

        Requisition result = m_provisioningService.addServiceToInterface(groupName, pathToInterface, serviceName);

        int newCount = PropertyUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        assertEquals(initialCount+1, newCount);
        RequisitionMonitoredService svc = PropertyUtils.getPathValue(result, pathToInterface+".monitoredService[0]", RequisitionMonitoredService.class);
        assertNotNull(svc);
        assertEquals(serviceName, svc.getServiceName());
    }

    @Test
    public void testDeletePath() {
        String groupName = "matt:";
        String pathToInterface = "node[0].interface[0]";
        String pathToDelete = pathToInterface+".monitoredService[0]";
        
        int initialCount = PropertyUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        String svcName = PropertyUtils.getPathValue(m_testData, pathToDelete+".serviceName", String.class);

        Requisition result = m_provisioningService.deletePath(groupName, pathToDelete);
        int newCount = PropertyUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class); 
        assertEquals(initialCount-1, newCount);

        RequisitionMonitoredService svc = PropertyUtils.getPathValue(result, pathToInterface+".monitoredService[0]", RequisitionMonitoredService.class);
        assertNotNull(svc);
        assertFalse(svc.getServiceName().equals(svcName));
    }

    @Test
    public void serviceTypeNamesIncludesServiceFromPollerConfiguration() {
        // Map of service monitors
        final Map<String, ServiceMonitor> serviceMonitors = new HashMap<String, ServiceMonitor>();
        serviceMonitors.put("Shochu-Stock-Level", null);

        // Build a mock config. that returns our map
        final PollerConfig pollerConfig = EasyMock.createNiceMock(PollerConfig.class);
        EasyMock.expect(pollerConfig.getServiceMonitors()).andReturn(serviceMonitors);
        m_provisioningService.setPollerConfig(pollerConfig);

        EasyMock.replay(pollerConfig);

        final Collection<String> services = m_provisioningService.getServiceTypeNames("");
        assertTrue(services.contains("ICMP"));
        assertTrue(services.contains("Shochu-Stock-Level"));

        EasyMock.verify(pollerConfig);
    }
}
