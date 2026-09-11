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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.CategoryAvailabilityCalculator;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Loads a synthetic inventory of configurable size straight into the tables
 * the calculator reads, then checks that the category figures match the
 * downtime that was inserted. The expected totals are accumulated in Java
 * while the fixture is generated, independently of the SQL under test, so
 * this catches regressions in the set-based query shape (a per-node query, a
 * materialized IN list) that a three-node network cannot expose.
 *
 * The default size is deliberately modest so the test is cheap in CI. Timings
 * are logged but not asserted, because they measure the build host rather
 * than the code. To rerun at a large installation's size by hand:
 *
 *   -Dorg.opennms.availability.scale.nodes=140000
 *
 * Reference point, September 2026, PostgreSQL 15 with default settings on a
 * developer workstation, 140,000 nodes with 980,000 services and 78,000
 * outages: the all-nodes category computed in about 6.6 s, the rule-restricted
 * one in about 7.0 s, the service-restricted one in about 5.4 s. Loading the
 * fixture at that size takes about two minutes per test method.
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
@JUnitTemporaryDatabase(reuseDatabase=false)
public class CategoryAvailabilityLargeInventoryIT {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryAvailabilityLargeInventoryIT.class);

    private static final long MINUTE = 60L * 1000L;
    private static final long HOUR = 60L * MINUTE;
    private static final long DAY = 24L * HOUR;

    private static final int NODES = SystemProperties.getInteger("org.opennms.availability.scale.nodes", 2000);
    private static final int BATCH = 5000;

    /** Index in this array is the service slot on every node. */
    private static final String[] SERVICES = { "ICMP", "SNMP", "HTTP", "HTTPS", "SMTP", "DNS", "SSH" };
    private static final int SERVICE_ID_BASE = 9000;

    private static final String ALL_NODES = "IPADDR != '0.0.0.0'";
    /** Nodes whose synthetic address falls in 10.0.x.y, that is node IDs below 65536. */
    private static final String FIRST_BLOCK = "IPADDR IPLIKE 10.0.*.*";

    @Autowired
    private CategoryAvailabilityCalculator m_calculator;

    @Autowired
    private DataSource m_dataSource;

    private JdbcTemplate m_jdbc;
    private Date m_now;
    private Date m_windowStart;

    /** Expected figures, accumulated while the fixture is generated. */
    private long m_expectedDowntimeAll;
    private long m_expectedDownAll;
    private long m_expectedDowntimeFirstBlock;
    private long m_expectedDownFirstBlock;
    private long m_expectedNodesFirstBlock;
    private long m_expectedDowntimeSubset;
    private long m_expectedDownSubset;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_jdbc = new JdbcTemplate(m_dataSource);
        m_now = new Date();
        m_windowStart = new Date(m_now.getTime() - DAY);
        final long began = System.nanoTime();
        loadFixture();
        LOG.info("Loaded {} nodes with {} services each in {} ms", NODES, SERVICES.length, elapsedMillis(began));
    }

    @Test
    public void categoryCoveringEveryNodeIsCorrect() {
        final long began = System.nanoTime();
        final CategoryAvailability cat = m_calculator.calculate("Overall", ALL_NODES, null, m_windowStart, m_now);
        final long millis = elapsedMillis(began);
        LOG.info("Overall category over {} nodes computed in {} ms: {}", NODES, millis, cat);

        assertEquals(NODES, cat.getNodes().size());
        assertEquals((long) NODES * SERVICES.length, cat.getServiceCount());
        assertEquals(m_expectedDownAll, cat.getServicesDown());
        assertEquals(m_expectedDowntimeAll, cat.getDowntimeMillis());

        if (NODES >= 100) {
            // node 20: one closed one-hour outage
            assertNode(cat, 20, SERVICES.length, 0, HOUR);
            // node 100: closed one-hour, open thirty-minute, straddling one-hour
            assertNode(cat, 100, SERVICES.length, 1, 2 * HOUR + 30 * MINUTE);
        }
    }

    @Test
    public void categoryRestrictedByServiceNamesIsCorrect() {
        // HTTP by name, plus everything starting with S: SNMP, SMTP, SSH
        final List<String> services = Arrays.asList("HTTP", "~^S");
        final long began = System.nanoTime();
        final CategoryAvailability cat = m_calculator.calculate("Servers", ALL_NODES, services, m_windowStart, m_now);
        final long millis = elapsedMillis(began);
        LOG.info("Service-restricted category over {} nodes computed in {} ms: {}", NODES, millis, cat);

        assertEquals(NODES, cat.getNodes().size());
        assertEquals((long) NODES * 4, cat.getServiceCount());
        assertEquals(m_expectedDownSubset, cat.getServicesDown());
        assertEquals(m_expectedDowntimeSubset, cat.getDowntimeMillis());
    }

    @Test
    public void categoryRestrictedByRuleIsCorrect() {
        final long began = System.nanoTime();
        final CategoryAvailability cat = m_calculator.calculate("FirstBlock", FIRST_BLOCK, null, m_windowStart, m_now);
        final long millis = elapsedMillis(began);
        LOG.info("Rule-restricted category over {} nodes computed in {} ms: {}", m_expectedNodesFirstBlock, millis, cat);

        assertEquals(m_expectedNodesFirstBlock, cat.getNodes().size());
        assertEquals(m_expectedNodesFirstBlock * SERVICES.length, cat.getServiceCount());
        assertEquals(m_expectedDownFirstBlock, cat.getServicesDown());
        assertEquals(m_expectedDowntimeFirstBlock, cat.getDowntimeMillis());
    }

    private static void assertNode(final CategoryAvailability cat, final int nodeId, final long serviceCount, final long servicesDown, final long downtime) {
        final NodeAvailability node = cat.getNodes().stream().filter(n -> n.getNodeId() == nodeId).findFirst()
                .orElseThrow(() -> new AssertionError("node " + nodeId + " missing"));
        assertEquals(serviceCount, node.getServiceCount());
        assertEquals(servicesDown, node.getServicesDown());
        assertEquals(downtime, node.getDowntimeMillis());
    }

    private static long elapsedMillis(final long beganNanos) {
        return (System.nanoTime() - beganNanos) / 1_000_000L;
    }

    // ---- fixture -----------------------------------------------------------

    private void loadFixture() {
        for (int k = 0; k < SERVICES.length; k++) {
            m_jdbc.update("INSERT INTO service (serviceid, servicename) VALUES (?, ?)", SERVICE_ID_BASE + k, SERVICES[k]);
        }

        final Timestamp created = new Timestamp(m_now.getTime());
        for (int from = 1; from <= NODES; from += BATCH) {
            final int start = from;
            final int count = Math.min(BATCH, NODES - from + 1);

            m_jdbc.batchUpdate("INSERT INTO node (nodeid, nodecreatetime, nodetype, nodelabel, location) VALUES (?, ?, 'A', ?, 'Default')",
                    setter(count, (ps, i) -> {
                        final int id = start + i;
                        ps.setInt(1, id);
                        ps.setTimestamp(2, created);
                        ps.setString(3, "node-" + id);
                    }));

            m_jdbc.batchUpdate("INSERT INTO ipinterface (id, nodeid, ipaddr, ismanaged, issnmpprimary) VALUES (?, ?, ?, 'M', 'P')",
                    setter(count, (ps, i) -> {
                        final int id = start + i;
                        ps.setInt(1, id);
                        ps.setInt(2, id);
                        ps.setString(3, addressOf(id));
                    }));

            m_jdbc.batchUpdate("INSERT INTO ifservices (id, ipinterfaceid, serviceid, status) VALUES (?, ?, ?, 'A')",
                    setter(count * SERVICES.length, (ps, i) -> {
                        final int id = start + (i / SERVICES.length);
                        final int slot = i % SERVICES.length;
                        ps.setInt(1, serviceRowId(id, slot));
                        ps.setInt(2, id);
                        ps.setInt(3, SERVICE_ID_BASE + slot);
                    }));
        }

        loadOutages();
        m_jdbc.execute("ANALYZE");
    }

    /**
     * Deterministic outage pattern. Slots: 0 ICMP, 1 SNMP, 2 HTTP, 3 HTTPS, 4 SMTP, 5 DNS, 6 SSH.
     * The service-restricted category covers slots 1, 2, 4 and 6.
     */
    private void loadOutages() {
        final Timestamp now = ts(0);
        int outageId = 0;
        final List<Object[]> rows = new ArrayList<>();

        for (int id = 1; id <= NODES; id++) {
            final boolean firstBlock = id < 65536;
            if (firstBlock) {
                m_expectedNodesFirstBlock++;
            }
            if (id % 20 == 0) {
                // closed inside the window, one hour, on ICMP
                rows.add(new Object[] { ++outageId, serviceRowId(id, 0), ts(2 * HOUR), ts(HOUR), null });
                m_expectedDowntimeAll += HOUR;
                if (firstBlock) m_expectedDowntimeFirstBlock += HOUR;
            }
            if (id % 50 == 0) {
                // still open, thirty minutes so far, on SNMP
                rows.add(new Object[] { ++outageId, serviceRowId(id, 1), ts(30 * MINUTE), null, null });
                m_expectedDowntimeAll += 30 * MINUTE;
                m_expectedDownAll++;
                m_expectedDowntimeSubset += 30 * MINUTE;
                m_expectedDownSubset++;
                if (firstBlock) { m_expectedDowntimeFirstBlock += 30 * MINUTE; m_expectedDownFirstBlock++; }
            }
            if (id % 7 == 0) {
                // entirely before the window, on HTTP: must not count
                rows.add(new Object[] { ++outageId, serviceRowId(id, 2), ts(48 * HOUR), ts(47 * HOUR), null });
            }
            if (id % 100 == 0) {
                // straddles the window start, one hour inside, on HTTPS
                rows.add(new Object[] { ++outageId, serviceRowId(id, 3), ts(25 * HOUR), ts(23 * HOUR), null });
                m_expectedDowntimeAll += HOUR;
                if (firstBlock) m_expectedDowntimeFirstBlock += HOUR;
            }
            if (id % 3 == 0) {
                // open outage seen from a remote perspective, on SMTP: must not count
                rows.add(new Object[] { ++outageId, serviceRowId(id, 4), ts(3 * HOUR), null, "Default" });
            }
            if (rows.size() >= BATCH) {
                flushOutages(rows);
            }
        }
        flushOutages(rows);
        LOG.info("Inserted {} outages; expecting {} ms downtime and {} open outages over all nodes", outageId, m_expectedDowntimeAll, m_expectedDownAll);
    }

    private void flushOutages(final List<Object[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        m_jdbc.batchUpdate("INSERT INTO outages (outageid, ifserviceid, iflostservice, ifregainedservice, perspective) VALUES (?, ?, ?, ?, ?)", rows);
        rows.clear();
    }

    private Timestamp ts(final long millisAgo) {
        return new Timestamp(m_now.getTime() - millisAgo);
    }

    private static int serviceRowId(final int nodeId, final int slot) {
        return nodeId * 8 + slot;
    }

    private static String addressOf(final int id) {
        return "10." + ((id >> 16) & 0xff) + "." + ((id >> 8) & 0xff) + "." + (id & 0xff);
    }

    @FunctionalInterface
    private interface RowSetter {
        void set(PreparedStatement ps, int i) throws SQLException;
    }

    private static BatchPreparedStatementSetter setter(final int rows, final RowSetter rowSetter) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                rowSetter.set(ps, i);
            }
            @Override
            public int getBatchSize() {
                return rows;
            }
        };
    }
}
