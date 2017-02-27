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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.provision.persist.requisition.RequisitionMapper.toPersistenceModel;
import static org.opennms.netmgt.provision.persist.requisition.RequisitionMapper.toRestModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXB;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.mock.MockServiceTypeDao;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.netmgt.provision.persist.MockForeignSourceService;
import org.opennms.netmgt.provision.persist.MockRequisitionService;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
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
@JUnitTemporaryDatabase
@Transactional
@Ignore("The DefaultManualProvisioningService is deprecated and should not be used anymore.")
public class DefaultManualProvisioningServiceIT {

    private DefaultManualProvisioningService m_provisioningService;

    private RequisitionEntity m_testData;

    private RequisitionService m_activeRepository = new MockRequisitionService();
    private MockServiceTypeDao m_serviceTypeDao = new MockServiceTypeDao();

    @Before
    public void setUp() throws Exception {
        m_provisioningService = new DefaultManualProvisioningService();
        m_provisioningService.setRequisitionService(m_activeRepository);
        m_provisioningService.setForeignSourceService(new MockForeignSourceService());
        m_provisioningService.setServiceTypeDao(m_serviceTypeDao);

        final Requisition requisition = JAXB.unmarshal(ConfigurationTestUtils.getSpringResourceForResource(this, "/tec_dump.xml").getURL(), Requisition.class);
        m_provisioningService.deleteProvisioningGroup(requisition.getForeignSource());
        m_provisioningService.saveProvisioningGroup(requisition.getForeignSource(), toPersistenceModel(requisition));

        m_testData = m_activeRepository.getRequisition(requisition.getForeignSource());
    }

    @Test
    public void testGetProvisioningGroupNames() {
        Set<String> expected = new TreeSet<>();
        expected.add("matt:");
        Collection<String> groupNames = m_provisioningService.getProvisioningGroupNames();
        assertEquals(expected, groupNames);
    }

    @Test
    public void testGetProvisioningGroup() {
        String name = "matt:";

        RequisitionEntity actual = m_provisioningService.getProvisioningGroup(name);
        assertEquals(m_testData, actual);
        assertEquals(toRestModel(m_testData), toRestModel(actual));
    }

    @Test
    public void testAddNewNodeToGroup() {
        String groupName = "matt:";
        String nodeLabel = "david";

        int initialCount = m_testData.getNodes().size();

        RequisitionEntity result = m_provisioningService.addNewNodeToGroup(groupName, nodeLabel);

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

        RequisitionEntity result = m_provisioningService.addCategoryToNode(groupName, pathToNode, categoryName);

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

        RequisitionEntity result = m_provisioningService.addInterfaceToNode(groupName, pathToNode, ipAddr);

        int newCount = PropertyUtils.getPathValue(result, pathToNode+".interfaceCount", int.class);

        assertEquals(initialCount+1, newCount);
        // TODO MVR this will fail
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

        RequisitionEntity result = m_provisioningService.addServiceToInterface(groupName, pathToInterface, serviceName);

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

        RequisitionEntity result = m_provisioningService.deletePath(groupName, pathToDelete);
        int newCount = PropertyUtils.getPathValue(m_testData, pathToInterface+".monitoredServiceCount", int.class);
        assertEquals(initialCount-1, newCount);

        RequisitionMonitoredService svc = PropertyUtils.getPathValue(result, pathToInterface+".monitoredService[0]", RequisitionMonitoredService.class);
        assertNotNull(svc);
        assertFalse(svc.getServiceName().equals(svcName));
    }

    @Test
    public void serviceTypeNamesIncludesServiceFromPollerConfiguration() {
        // Map of service monitors
        final Map<String, ServiceMonitor> serviceMonitors = new HashMap<>();
        serviceMonitors.put("Shochu-Stock-Level", new AbstractServiceMonitor() {
            @Override
            public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
                return null;
            }
        });

        // Build a mock config. that returns our map
        final PollerConfig pollerConfig = EasyMock.createNiceMock(PollerConfig.class);
        EasyMock.expect(pollerConfig.getServiceMonitors()).andReturn(serviceMonitors).anyTimes();
        m_provisioningService.setPollerConfig(pollerConfig);

        EasyMock.replay(pollerConfig);

        final Collection<String> services = m_provisioningService.getServiceTypeNames("");
        assertTrue(services.contains("ICMP"));
        assertTrue(services.contains("Shochu-Stock-Level"));

        EasyMock.verify(pollerConfig);
    }
}
