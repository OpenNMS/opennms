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
package org.opennms.netmgt.dao.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.CategoryAvailabilityCalculator;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exercises the set-based availability calculation against the standard mock
 * network: node 1 has four services (ICMP and SMTP on two interfaces), node 2
 * has two (ICMP, HTTP), node 3 has four (SMTP and HTTP on two interfaces).
 *
 * The first cases are ports of the cases that covered the rtcd Hibernate
 * implementation; the rest pin down the window edge behavior.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class, reuseDatabase=false)
public class JdbcCategoryAvailabilityCalculatorIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;

    /** Matches nodes 1 and 2 of the standard network. */
    private static final String NODES_1_AND_2 = "IPADDR IPLIKE 192.168.1.1-3";
    /** Matches every node. */
    private static final String ALL_NODES = "IPADDR != '0.0.0.0'";
    /** Matches nothing. */
    private static final String NO_NODES = "IPADDR IPLIKE 10.255.255.255";

    @Autowired
    private CategoryAvailabilityCalculator m_calculator;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private MockDatabase m_db;
    private MockNetwork m_network;

    private Date m_now;
    private Date m_windowStart;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_db = database;
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_network = new MockNetwork().createStandardNetwork();
        m_db.populate(m_network);
        m_now = new Date();
        m_windowStart = new Date(m_now.getTime() - DAY);
    }

    @Test
    public void categoryWithNoMatchingNodesIsFullyAvailable() {
        final CategoryAvailability cat = calculate("Routers", NO_NODES, null);
        assertEquals("Routers", cat.getLabel());
        assertEquals(0, cat.getNodes().size());
        assertEquals(0, cat.getServiceCount());
        assertEquals(0, cat.getServicesDown());
        assertEquals(100.0, cat.getAvailability(), 0.0);
        assertEquals(DAY, cat.getWindowMillis());
    }

    @Test
    public void canCalculateAvailability() {
        CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertEquals(2, cat.getNodes().size());
        assertEquals(100.0, cat.getAvailability(), 0.0);
        assertNode(cat, 1, 4, 0, 0);
        assertNode(cat, 2, 2, 0, 0);

        // an outage that opened and closed inside the window: 30 minutes on node 1
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(1), minutesAgo(30));

        cat = calculate("NOC", NODES_1_AND_2, null);
        assertEquals(2, cat.getNodes().size());
        assertEquals(6, cat.getServiceCount());
        assertEquals(30L * 60L * 1000L, cat.getDowntimeMillis());
        assertEquals(NodeAvailability.percentage(1800000, DAY, 6), cat.getAvailability(), 0.0001);
        assertNode(cat, 1, 4, 0, 1800000);
        assertEquals(NodeAvailability.percentage(1800000, DAY, 4), node(cat, 1).getAvailability(), 0.0001);
        assertNode(cat, 2, 2, 0, 0);
    }

    @Test
    public void serviceNamesAndRegularExpressionsAreEquivalent() {
        final List<String> literal = Arrays.asList("SMTP", "HTTP");
        final List<String> regex = Arrays.asList("~[HS].TP");

        CategoryAvailability byName = calculate("NOC1", NODES_1_AND_2, literal);
        CategoryAvailability byRegex = calculate("NOC2", NODES_1_AND_2, regex);
        assertEquals(100.0, byName.getAvailability(), 0.0);
        assertNode(byName, 1, 2, 0, 0); // SMTP on both interfaces
        assertNode(byName, 2, 1, 0, 0); // HTTP
        assertSameFigures(byName, byRegex);

        createOutage(service(1, "192.168.1.2", "SMTP"), hoursAgo(1), minutesAgo(30));
        createOutage(service(2, "192.168.1.3", "HTTP"), minutesAgo(15), m_now);

        byName = calculate("NOC1", NODES_1_AND_2, literal);
        byRegex = calculate("NOC2", NODES_1_AND_2, regex);
        assertEquals(45L * 60L * 1000L, byName.getDowntimeMillis());
        assertSameFigures(byName, byRegex);
    }

    @Test
    public void servicesOutsideTheListDoNotCount() {
        // ICMP outage while the category only covers HTTP
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(1), minutesAgo(30));
        final CategoryAvailability cat = calculate("Web", NODES_1_AND_2, Arrays.asList("HTTP"));
        assertNode(cat, 1, 0, 0, 0);
        assertNode(cat, 2, 1, 0, 0);
        assertEquals(100.0, cat.getAvailability(), 0.0);
        assertEquals(100.0, node(cat, 1).getAvailability(), 0.0);
    }

    // See NMS-10458
    @Test
    public void everythingDownForTheWholeWindow() {
        final Date oneDayAgo = m_windowStart;
        createOutage(service(1, "192.168.1.1", "ICMP"), oneDayAgo, null);
        createOutage(service(2, "192.168.1.3", "ICMP"), oneDayAgo, null);
        createOutage(service(2, "192.168.1.3", "HTTP"), oneDayAgo, null);

        final CategoryAvailability cat = calculate("TEST", ALL_NODES, null);
        assertEquals("TEST", cat.getLabel());
        assertEquals(3, cat.getNodes().size());
        assertEquals(10, cat.getServiceCount());
        assertEquals(3, cat.getServicesDown());
        assertEquals(70.0, cat.getAvailability(), 0.0001);

        assertNode(cat, 1, 4, 1, DAY);
        assertEquals(75.0, node(cat, 1).getAvailability(), 0.0001);
        assertNode(cat, 2, 2, 2, 2 * DAY);
        assertEquals(0.0, node(cat, 2).getAvailability(), 0.0001);
        assertNode(cat, 3, 4, 0, 0);
        assertEquals(100.0, node(cat, 3).getAvailability(), 0.0001);
    }

    @Test
    public void outageStraddlingWindowStartIsClamped() {
        // lost 30h ago, regained 23h ago: only the last hour falls in the window
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(30), hoursAgo(23));
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 4, 0, HOUR);
        assertEquals(HOUR, cat.getDowntimeMillis());
    }

    @Test
    public void outageEntirelyBeforeWindowIsIgnored() {
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(30), hoursAgo(25));
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 4, 0, 0);
        assertEquals(100.0, cat.getAvailability(), 0.0);
    }

    @Test
    public void openOutageCountsUpToWindowEnd() {
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(2), null);
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 4, 1, 2 * HOUR);
        assertEquals(1, cat.getServicesDown());
    }

    @Test
    public void openOutageOlderThanWindowCountsTheWholeWindow() {
        createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(72), null);
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 4, 1, DAY);
    }

    @Test
    public void perspectiveOutagesAreIgnored() {
        final OnmsOutage outage = createOutage(service(1, "192.168.1.1", "ICMP"), hoursAgo(2), null, true);
        assertNotNull(outage.getPerspective());
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 4, 0, 0);
    }

    @Test
    public void unmanagedInterfacesAreExcluded() {
        m_db.setInterfaceStatus(m_network.getInterface(1, "192.168.1.2"), 'U');
        final CategoryAvailability cat = calculate("NOC", NODES_1_AND_2, null);
        assertNode(cat, 1, 2, 0, 0);
        assertEquals(4, cat.getServiceCount());
    }

    @Test
    public void invalidRuleIsRejected() {
        try {
            calculate("Broken", "this is not a filter rule", null);
            fail("expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Broken"));
        }
    }

    @Test
    public void emptyWindowIsRejected() {
        try {
            m_calculator.calculate("NOC", NODES_1_AND_2, null, m_now, m_now);
            fail("expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    private CategoryAvailability calculate(final String label, final String rule, final Collection<String> services) {
        return m_calculator.calculate(label, rule, services, m_windowStart, m_now);
    }

    private static NodeAvailability node(final CategoryAvailability cat, final int nodeId) {
        for (final NodeAvailability node : cat.getNodes()) {
            if (node.getNodeId() == nodeId) {
                return node;
            }
        }
        fail("node " + nodeId + " missing from " + cat);
        return null;
    }

    private static void assertNode(final CategoryAvailability cat, final int nodeId, final long serviceCount, final long servicesDown, final long downtimeMillis) {
        final NodeAvailability node = node(cat, nodeId);
        assertEquals("service count of node " + nodeId, serviceCount, node.getServiceCount());
        assertEquals("services down on node " + nodeId, servicesDown, node.getServicesDown());
        assertEquals("downtime of node " + nodeId, downtimeMillis, node.getDowntimeMillis());
    }

    private static void assertSameFigures(final CategoryAvailability a, final CategoryAvailability b) {
        assertEquals(a.getNodes().size(), b.getNodes().size());
        assertEquals(a.getServiceCount(), b.getServiceCount());
        assertEquals(a.getServicesDown(), b.getServicesDown());
        assertEquals(a.getDowntimeMillis(), b.getDowntimeMillis());
        assertEquals(a.getAvailability(), b.getAvailability(), 0.0);
        for (int i = 0; i < a.getNodes().size(); i++) {
            assertEquals(a.getNodes().get(i), b.getNodes().get(i));
        }
    }

    private Date hoursAgo(final long hours) {
        return new Date(m_now.getTime() - hours * HOUR);
    }

    private Date minutesAgo(final long minutes) {
        return new Date(m_now.getTime() - minutes * 60L * 1000L);
    }

    private MockService service(final int nodeId, final String ipAddr, final String svcName) {
        return m_network.getService(nodeId, ipAddr, svcName);
    }

    private OnmsOutage createOutage(final MockService svc, final Date lost, final Date regained) {
        return createOutage(svc, lost, regained, false);
    }

    private OnmsOutage createOutage(final MockService svc, final Date lost, final Date regained, final boolean fromPerspective) {
        return new TransactionTemplate(m_transactionManager).execute(status -> {
            final OnmsMonitoredService monitoredService = m_monitoredServiceDao.get(svc.getNodeId(), svc.getAddress(), svc.getSvcName());
            assertNotNull("service " + svc + " not found in database", monitoredService);
            final OnmsOutage outage = new OnmsOutage();
            outage.setMonitoredService(monitoredService);
            outage.setIfLostService(lost);
            outage.setIfRegainedService(regained);
            if (fromPerspective) {
                outage.setPerspective(m_monitoringLocationDao.getDefaultLocation());
            }
            m_outageDao.save(outage);
            m_outageDao.flush();
            return outage;
        });
    }
}
