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
package org.opennms.netmgt.rtc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.xml.rtc.Category;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.opennms.netmgt.xml.rtc.Node;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-rtc.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class AvailabilityServiceIT implements TemporaryDatabaseAware<MockDatabase> {

    @Autowired
    private AvailabilityService m_availabilityService;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    private MockDatabase m_mockDatabase;

    @Autowired
    PlatformTransactionManager m_transactionManager;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_mockDatabase = database;
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void categoryIsFullyAvailableWhenNoServicesArePresent() throws Exception {
        final RTCCategory rtcCat = mock(RTCCategory.class);
        when(rtcCat.getLabel()).thenReturn("Routers");
        // This nodeid should not exist in the database
        when(rtcCat.getNodes()).thenReturn(Lists.newArrayList(99999));

        final EuiLevel euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        final Category category = euiLevel.getCategory().get(0);
        assertEquals(100.0, category.getCatvalue(), 0.001);
        assertEquals(1, category.getNode().size());

        final Node node = category.getNode().get(0);
        assertEquals(100.0, node.getNodevalue(), 0.001);
        assertEquals(0, node.getNodesvccount());
        assertEquals(0, node.getNodesvcdowncount());
    }

    @Test
    public void canCalculateAvailability() throws Exception {
        final MockNetwork mockNetwork = new MockNetwork();
        // This test depends on the specifics in the standard network definition
        mockNetwork.createStandardNetwork();
        m_mockDatabase.populate(mockNetwork);

        final RTCCategory rtcCat = mock(RTCCategory.class);
        when(rtcCat.getLabel()).thenReturn("NOC");
        when(rtcCat.getNodes()).thenReturn(Lists.newArrayList(1, 2));

        // Verify the availability when no outages are present
        EuiLevel euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        Category category = euiLevel.getCategory().get(0);
        assertEquals(100.0, category.getCatvalue(), 0.001);
        assertEquals(2, category.getNode().size());

        // Assumes the nodes are sorted
        assertEquals(4, category.getNode().get(0).getNodesvccount());
        assertEquals(2, category.getNode().get(1).getNodesvccount());

        // Create an outage that is both open and closed within the window
        final Date now = new Date();
        final Date oneHourAgo = new Date(now.getTime() - (60 * 60 * 1000));
        final Date thirtyMinutesAgo = new Date(now.getTime() - (30 * 60 * 1000));

        final OnmsMonitoredService icmpService = toMonitoredService(mockNetwork.getService(1, "192.168.1.1", "ICMP"));

        new TransactionTemplate(m_transactionManager).execute(status -> {
                    OnmsOutage outage = new OnmsOutage();
                    outage.setMonitoredService(icmpService);
                    outage.setIfLostService(oneHourAgo);
                    outage.setIfRegainedService(thirtyMinutesAgo);
                    m_outageDao.save(outage);
                    m_outageDao.flush();
                    return null;
                });

        // Verify the availability when outages are present
        euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        category = euiLevel.getCategory().get(0);
        // This number should only need to be adjusted if the duration of the outage
        // or the number of services in the category changes
        assertEquals(RTCUtils.getOutagePercentage(1800000, 86400000, 6), category.getCatvalue(), 0.0001);
        assertEquals(2, category.getNode().size());
    }

    @Test
    @Transactional
    public void compareServiceNamesAndRegExp() {
        final MockNetwork mockNetwork = new MockNetwork();
        mockNetwork.createStandardNetwork();
        m_mockDatabase.populate(mockNetwork);

        final RTCCategory rtcCat1 = mock(RTCCategory.class);
        when(rtcCat1.getLabel()).thenReturn("NOC1");
        when(rtcCat1.getServices()).thenReturn(Lists.newArrayList("SMTP", "HTTP"));
        when(rtcCat1.getNodes()).thenReturn(Lists.newArrayList(1, 2));

        final RTCCategory rtcCat2 = mock(RTCCategory.class);
        when(rtcCat2.getLabel()).thenReturn("NOC2");
        when(rtcCat2.getServices()).thenReturn(Lists.newArrayList("~[HS].TP"));
        when(rtcCat2.getNodes()).thenReturn(Lists.newArrayList(1, 2));

        EuiLevel euiLevel1 = m_availabilityService.getEuiLevel(rtcCat1);
        EuiLevel euiLevel2 = m_availabilityService.getEuiLevel(rtcCat2);

        assertEquals(1, euiLevel1.getCategory().size());
        assertEquals(100.0, euiLevel1.getCategory().get(0).getCatvalue(), 0.001);
        assertEquals(2, euiLevel1.getCategory().get(0).getNode().size());

        compareEuiLevel(euiLevel1, euiLevel2);

        final Date now = new Date();
        final Date oneHourAgo = new Date(now.getTime() - (60 * 60 * 1000));
        final Date thirtyMinutesAgo = new Date(now.getTime() - (30 * 60 * 1000));
        final Date fifteenMinutesAgo = new Date(now.getTime() - (15 * 60 * 1000));

        final OnmsMonitoredService smtpService = toMonitoredService(mockNetwork.getService(1, "192.168.1.2", "SMTP"));
        final OnmsOutage outage1 = new OnmsOutage();
        outage1.setMonitoredService(smtpService);
        outage1.setIfLostService(oneHourAgo);
        outage1.setIfRegainedService(thirtyMinutesAgo);
        m_outageDao.save(outage1);
        m_outageDao.flush();

        final OnmsMonitoredService httpService = toMonitoredService(mockNetwork.getService(2, "192.168.1.3", "HTTP"));
        final OnmsOutage outage2 = new OnmsOutage();
        outage2.setMonitoredService(httpService);
        outage2.setIfLostService(fifteenMinutesAgo);
        outage2.setIfRegainedService(now);
        m_outageDao.save(outage2);
        m_outageDao.flush();

        euiLevel1 = m_availabilityService.getEuiLevel(rtcCat1);
        euiLevel2 = m_availabilityService.getEuiLevel(rtcCat2);

        compareEuiLevel(euiLevel1, euiLevel2);
    }

    private void compareEuiLevel(final EuiLevel e1, final EuiLevel e2) {
        assertEquals(1, e1.getCategory().size());
        assertEquals(1, e2.getCategory().size());

        final Category c1 = e1.getCategory().get(0);
        final Category c2 = e2.getCategory().get(0);

        assertEquals(c1.getCatvalue(), c2.getCatvalue(), 0.0);
        assertEquals("NOC1", c1.getCatlabel());
        assertEquals("NOC2", c2.getCatlabel());
        assertEquals(c1.getNode().size(), c2.getNode().size());
        assertEquals(c1.getNode().size(), c2.getNode().size());

        final Map<Integer, Node> nodes1 = c1.getNode().stream().collect(Collectors.toMap(node -> (int) node.getNodeid(), Function.identity()));
        final Map<Integer, Node> nodes2 = c2.getNode().stream().collect(Collectors.toMap(node -> (int) node.getNodeid(), Function.identity()));

        assertEquals(nodes1.get(1).getNodesvccount() , nodes2.get(1).getNodesvccount());
        assertEquals(nodes1.get(2).getNodesvccount() , nodes2.get(2).getNodesvccount());

        assertEquals(nodes1.get(1).getNodesvcdowncount(), nodes2.get(1).getNodesvcdowncount());
        assertEquals(nodes1.get(2).getNodesvcdowncount() , nodes2.get(2).getNodesvcdowncount());

        assertEquals(nodes1.get(1).getNodevalue() , nodes2.get(1).getNodevalue(),0.0);
        assertEquals(nodes1.get(2).getNodevalue() , nodes2.get(2).getNodevalue(),0.0);
    }

    // See NMS-10458
    @Test
    @Transactional
    public void verifyAvailabilityIfEverythingIsDown() {
        final MockNetwork mockNetwork = new MockNetwork();
        mockNetwork.createStandardNetwork();
        m_mockDatabase.populate(mockNetwork);

        final RTCCategory rtcCat = mock(RTCCategory.class);
        when(rtcCat.getLabel()).thenReturn("TEST");
        when(rtcCat.getNodes()).thenReturn(Lists.newArrayList(1, 2, 3));

        final long now = System.currentTimeMillis();
        final long oneDayAgo = now - (1000 * 60 * 60 * 24);

        // Create Outage for Node 1
        createOutage(mockNetwork.getService(1, "192.168.1.1", "ICMP"), oneDayAgo);

        // Create Outages for Node 2
        createOutage(mockNetwork.getService(2, "192.168.1.3", "ICMP"), oneDayAgo);
        createOutage(mockNetwork.getService(2, "192.168.1.3", "HTTP"), oneDayAgo);

        // Calculate Availability for category
        final EuiLevel euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        // Verify Category Availability
        final Category category = euiLevel.getCategory().get(0);
        assertEquals("TEST", category.getCatlabel());
        assertEquals(70.0f, category.getCatvalue(), 0.0001);

        // Verify Nodes
        final List<Node> nodes = category.getNode();
        assertEquals(3, nodes.size());
        verifyNode(nodes.get(0), 1, 75.0f);
        verifyNode(nodes.get(1), 2, 0.0f);
        verifyNode(nodes.get(2), 3, 100.0f);
    }

    private OnmsMonitoredService toMonitoredService(MockService svc) {
        return m_monitoredServiceDao.get(svc.getNodeId(), svc.getAddress(), svc.getSvcName());
    }

    private void createOutage(MockService mockService, long oneDayAgo) {
        final OnmsMonitoredService monitoredService = toMonitoredService(mockService);
        final OnmsOutage outage = new OnmsOutage();
        outage.setMonitoredService(monitoredService);
        outage.setIfLostService(new Date(oneDayAgo));
        m_outageDao.save(outage);
    }

    private static void verifyNode(Node node, long nodeId, float availability) {
        assertEquals(nodeId, node.getNodeid());
        assertEquals(availability, node.getNodevalue(), 0.0001);
    }
}
