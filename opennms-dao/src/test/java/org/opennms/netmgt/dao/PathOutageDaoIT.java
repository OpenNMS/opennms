/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author mhuot
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class PathOutageDaoIT implements InitializingBean {

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private PathOutageDao m_pathOutageDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsServiceType t = new OnmsServiceType("ICMP");
                m_serviceTypeDao.save(t);
            }
        });
    }

    @Test
    @Transactional
    public void testSave() {
        final OnmsServiceType serviceType = m_serviceTypeDao.findByName("ICMP");
        assertNotNull(serviceType);

        // This will be our router with one IP address
        OnmsNode router = new OnmsNode(m_locationDao.getDefaultLocation(), "router");
        m_nodeDao.save(router);
        OnmsIpInterface routerIpInterface = new OnmsIpInterface(addr("172.16.1.1"), router);
        routerIpInterface.setIsManaged("M");
        OnmsMonitoredService routerService = new OnmsMonitoredService(routerIpInterface, serviceType);
        routerService.setStatus("A");

        // Add a node that will be routed through the router
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "localhost");
        m_nodeDao.save(node);
        OnmsIpInterface nodeIpInterface = new OnmsIpInterface(addr("172.16.1.2"), node);
        nodeIpInterface.setIsManaged("M");
        OnmsMonitoredService nodeMonitoredService = new OnmsMonitoredService(nodeIpInterface, serviceType);
        nodeMonitoredService.setStatus("A");

        // Make another node with an interface that is initially marked as deleted
        OnmsNode newNode = new OnmsNode(m_locationDao.getDefaultLocation(), "newnode");
        m_nodeDao.save(newNode);
        OnmsIpInterface newIpInterface = new OnmsIpInterface(addr("172.16.1.3"), newNode);
        newIpInterface.setIsManaged("D");
        OnmsMonitoredService newMonitoredService = new OnmsMonitoredService(newIpInterface, serviceType);
        newMonitoredService.setStatus("A");

        OnmsPathOutage outage = new OnmsPathOutage(node, routerIpInterface.getIpAddress(), routerService.getServiceName());
        m_pathOutageDao.save(outage);

        //it works we're so smart! hehe
        OnmsPathOutage temp = m_pathOutageDao.get(outage.getNode().getId());

        assertEquals(1, m_pathOutageDao.countAll());

        // Make sure that the path outage points from the node to the router interface/service
        assertEquals(node.getLabel(), temp.getNode().getLabel());
        assertEquals(routerIpInterface.getIpAddress(), temp.getCriticalPathIp());
        assertEquals(routerService.getServiceName(), temp.getCriticalPathServiceName());

        List<Integer> nodes = m_pathOutageDao.getNodesForPathOutage(temp);
        assertEquals(1, nodes.size());
        assertEquals(node.getId(), nodes.get(0));

        nodes = m_pathOutageDao.getNodesForPathOutage(routerIpInterface.getIpAddress(), routerService.getServiceName());
        assertEquals(1, nodes.size());
        assertEquals(node.getId(), nodes.get(0));

        // Make sure that nothing is using either node as a path outage
        nodes = m_pathOutageDao.getNodesForPathOutage(nodeIpInterface.getIpAddress(), nodeMonitoredService.getServiceName());
        assertEquals(0, nodes.size());
        nodes = m_pathOutageDao.getNodesForPathOutage(newIpInterface.getIpAddress(), newMonitoredService.getServiceName());
        assertEquals(0, nodes.size());

        assertEquals(1, m_pathOutageDao.countAll());

        OnmsPathOutage newOutage = new OnmsPathOutage(newNode, routerIpInterface.getIpAddress(), routerService.getServiceName());
        m_pathOutageDao.save(newOutage);

        assertEquals(2, m_pathOutageDao.countAll());

        // Should return zero results because the interface is marked as 'D' for deleted
        nodes = m_pathOutageDao.getNodesForPathOutage(routerIpInterface.getIpAddress(), routerService.getServiceName());
        assertEquals(2, nodes.size());
        nodes = m_pathOutageDao.getAllNodesDependentOnAnyServiceOnInterface(routerIpInterface.getIpAddress());
        assertEquals(2, nodes.size());

        // After we mark it as managed, the node should appear in the path outage list
        newIpInterface.setIsManaged("M");
        nodes = m_pathOutageDao.getNodesForPathOutage(routerIpInterface.getIpAddress(), routerService.getServiceName());
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(node.getId()));
        assertTrue(nodes.contains(newNode.getId()));

        assertEquals(2, m_pathOutageDao.countAll());
    }
}
