/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.RequisitionedCategoryAssociation;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Sets;

public class DefaultProvisionServiceTest {
    final MonitoringLocationDao m_monitoringLocationDao = mock(MonitoringLocationDao.class);
    final NodeDao m_nodeDao = mock(NodeDao.class);
    final IpInterfaceDao m_ipInterfaceDao = mock(IpInterfaceDao.class);
    final SnmpInterfaceDao m_snmpInterfaceDao = mock(SnmpInterfaceDao.class);
    final MonitoredServiceDao m_monitoredServiceDao = mock(MonitoredServiceDao.class);
    final ServiceTypeDao m_serviceTypeDao = mock(ServiceTypeDao.class);

    final MockEventIpcManager m_eventIpcManager = new MockEventIpcManager();

    final DefaultProvisionService m_provisionService = new DefaultProvisionService();

    private PlatformTransactionManager m_transactionManager = mock(PlatformTransactionManager.class);

    private CategoryDao m_categoryDao = mock(CategoryDao.class);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_provisionService.setMonitoringLocationDao(m_monitoringLocationDao);
        m_provisionService.setNodeDao(m_nodeDao);
        m_provisionService.setIpInterfaceDao(m_ipInterfaceDao);
        m_provisionService.setSnmpInterfaceDao(m_snmpInterfaceDao);
        m_provisionService.setMonitoredServiceDao(m_monitoredServiceDao);
        m_provisionService.setServiceTypeDao(m_serviceTypeDao);

        m_provisionService.setEventForwarder(m_eventIpcManager);

        m_provisionService.setCategoryDao(m_categoryDao);
        m_provisionService.setTransactionManager(m_transactionManager);
    }

    @Test
    public void testHandleDeleteServiceKeepUnmanaged() throws Exception {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");

        final OnmsIpInterface active = new OnmsIpInterface(InetAddressUtils.UNPINGABLE_ADDRESS, node);
        active.setIsManaged("M");
        final OnmsIpInterface inactive = new OnmsIpInterface(InetAddressUtils.UNPINGABLE_ADDRESS_IPV6, node);
        inactive.setIsManaged("U");

        final OnmsServiceType serviceType = new OnmsServiceType("ICMP");

        final OnmsMonitoredService activeService = new OnmsMonitoredService(active, serviceType);
        new OnmsMonitoredService(inactive, serviceType); // make sure there's a second service on the node

        when(m_monitoredServiceDao.get(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP")).thenReturn(activeService);

        m_provisionService.deleteService(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP", false);

        assertEquals(1, node.getIpInterfaces().size());
        verify(m_nodeDao, times(0)).delete(node);
    }

    @Test
    public void testHandleDeleteServiceIgnoreUnmanaged() throws Exception {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");

        final OnmsIpInterface active = new OnmsIpInterface(InetAddressUtils.UNPINGABLE_ADDRESS, node);
        active.setIsManaged("M");
        final OnmsIpInterface inactive = new OnmsIpInterface(InetAddressUtils.UNPINGABLE_ADDRESS_IPV6, node);
        inactive.setIsManaged("U");

        final OnmsServiceType serviceType = new OnmsServiceType("ICMP");

        final OnmsMonitoredService activeService = new OnmsMonitoredService(active, serviceType);
        new OnmsMonitoredService(inactive, serviceType); // make sure there's a second service on the node

        when(m_monitoredServiceDao.get(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP")).thenReturn(activeService);

        m_provisionService.deleteService(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP", true);

        verify(m_nodeDao, times(1)).delete(node);
    }

    @Test
    public void testHandleDeleteUnmanagedServiceIgnoreUnmanaged() throws Exception {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");

        final OnmsIpInterface active = new OnmsIpInterface(InetAddressUtils.UNPINGABLE_ADDRESS, node);
        active.setIsManaged("U");

        final OnmsServiceType serviceType = new OnmsServiceType("ICMP");

        final OnmsMonitoredService activeService = new OnmsMonitoredService(active, serviceType);

        when(m_monitoredServiceDao.get(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP")).thenReturn(activeService);

        m_provisionService.deleteService(1, InetAddressUtils.UNPINGABLE_ADDRESS, "ICMP", true);

        verify(m_nodeDao, times(1)).delete(node);
    }

    @Test
    public void testNMS16402() {
        assertEquals(Sets.newHashSet("A1", "A2", "A3", "A4"), checkSetsOfCategories(Sets.newHashSet("A1", "A2"), Sets.newHashSet("A3", "A4"), Sets.newHashSet("A1", "A3"), Sets.newHashSet("A1", "A2", "A3", "A4")));;
        assertEquals(Sets.newHashSet("A1", "A2", "A3", "A4"), checkSetsOfCategories(Sets.newHashSet("A1", "A2"), Sets.newHashSet("A3", "A4"), Sets.newHashSet(), Sets.newHashSet()));;
        assertEquals(Sets.newHashSet("A1", "A2", "A3"), checkSetsOfCategories(Sets.newHashSet("A1"), Sets.newHashSet("A3"), Sets.newHashSet("A1", "A2"), Sets.newHashSet("A1", "A3")));;
    }

    private Set<String> checkSetsOfCategories(Set<String> reqCats, Set<String> polCats, Set<String> dbCats, Set<String> dbReqCats) {
        final OnmsNode node = new OnmsNode();
        node.setForeignId("foreignId");
        node.setForeignSource("foreignSource");
        node.setCategories(dbCats.stream().map(OnmsCategory::new).collect(Collectors.toSet()));
        node.setRequisitionedCategories(Sets.union(polCats, dbReqCats));;
        node.setId(1);
        node.setLabel("myNode");

        final ForeignSourceRepository foreignSourceRepository = mock(ForeignSourceRepository.class);
        final OnmsNodeRequisition onmsNodeRequisition = mock(OnmsNodeRequisition.class);
        final RequisitionNode requisitionNode = mock(RequisitionNode.class);
        final RequisitionedCategoryAssociationDao requisitionedCategoryAssociationDao = mock(RequisitionedCategoryAssociationDao.class);

        when(requisitionNode.getCategories()).thenReturn(reqCats.stream().map(RequisitionCategory::new).collect(Collectors.toList()));
        when(onmsNodeRequisition.getNode()).thenReturn(requisitionNode);
        when(foreignSourceRepository.getNodeRequisition(node.getForeignSource(), node.getForeignId())).thenReturn(onmsNodeRequisition);
        when(m_nodeDao.get(node.getId())).thenReturn(node);
        when(requisitionedCategoryAssociationDao.findByNodeId(node.getId())).thenReturn(dbReqCats.stream().map(c -> new RequisitionedCategoryAssociation(node, new OnmsCategory(c))).collect(Collectors.toList()));

        m_provisionService.setForeignSourceRepository(foreignSourceRepository);
        m_provisionService.setCategoryAssociationDao(requisitionedCategoryAssociationDao);

        return m_provisionService.updateNodeAttributes(node).getCategories().stream()
                .map(OnmsCategory::getName)
                .collect(Collectors.toSet());
    }
}
