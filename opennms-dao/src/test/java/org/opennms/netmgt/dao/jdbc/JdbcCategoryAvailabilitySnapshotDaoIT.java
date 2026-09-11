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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.CategoryAvailabilitySnapshotDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
public class JdbcCategoryAvailabilitySnapshotDaoIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final long DAY = 24L * 60L * 60L * 1000L;

    @Autowired
    private CategoryAvailabilitySnapshotDao m_dao;

    private MockDatabase m_db;
    private Date m_now;
    private Date m_start;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_db = database;
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        // the node table must hold the nodes a snapshot refers to
        m_db.populate(new MockNetwork().createStandardNetwork());
        m_now = new Date();
        m_start = new Date(m_now.getTime() - DAY);
    }

    @Test
    public void savesAndReadsBackSummaryAndNodes() {
        m_dao.save(snapshot("Web Servers", node(1, 4, 1, 3_600_000L), node(2, 2, 0, 0L), node(3, 4, 0, 1_800_000L)));

        final Optional<CategoryAvailability> summary = m_dao.findSummary("Web Servers");
        assertTrue(summary.isPresent());
        assertEquals(3, summary.get().getNodeCount());
        assertEquals(10, summary.get().getServiceCount());
        assertEquals(1, summary.get().getServicesDown());
        assertEquals(5_400_000L, summary.get().getDowntimeMillis());
        assertEquals(m_now.getTime(), summary.get().getComputedAt().getTime());
        assertEquals(DAY, summary.get().getWindowMillis());
        assertTrue(summary.get().getNodes().isEmpty());
        assertFalse(summary.get().hasNodes());
        assertEquals(NodeAvailability.percentage(5_400_000L, DAY, 10), summary.get().getAvailability(), 0.0);

        final CategoryAvailability full = m_dao.findWithNodes("Web Servers").get();
        assertTrue(full.hasNodes());
        assertEquals(3, full.getNodes().size());
        assertEquals(node(1, 4, 1, 3_600_000L), full.getNodes().get(0));
        assertEquals(NodeAvailability.percentage(3_600_000L, DAY, 4), full.getNodes().get(0).getAvailability(), 0.0);
        assertEquals(summary.get().getAvailability(), full.getAvailability(), 0.0);

        assertEquals(Arrays.asList(1, 2, 3), m_dao.findNodeIds("Web Servers"));
        assertEquals(node(2, 2, 0, 0L), m_dao.findNode("Web Servers", 2).get());
        assertFalse(m_dao.findNode("Web Servers", 99).isPresent());
    }

    @Test
    public void pagesNodesInNodeIdOrder() {
        m_dao.save(snapshot("All", node(3, 1, 0, 0), node(1, 1, 0, 0), node(2, 1, 0, 0)));
        assertEquals(Arrays.asList(1, 2, 3), ids(m_dao.findNodes("All", 0, 0)));
        assertEquals(Arrays.asList(1, 2), ids(m_dao.findNodes("All", 0, 2)));
        assertEquals(Arrays.asList(3), ids(m_dao.findNodes("All", 2, 2)));
        assertEquals(Collections.emptyList(), ids(m_dao.findNodes("All", 5, 2)));
    }

    @Test
    public void saveReplacesThePreviousSnapshot() {
        m_dao.save(snapshot("Web Servers", node(1, 4, 0, 0), node(2, 2, 0, 0)));
        m_dao.save(snapshot("Web Servers", node(2, 2, 1, 60_000L)));

        final CategoryAvailability full = m_dao.findWithNodes("Web Servers").get();
        assertEquals(1, full.getNodeCount());
        assertEquals(Arrays.asList(2), m_dao.findNodeIds("Web Servers"));
        assertEquals(60_000L, full.getDowntimeMillis());
    }

    @Test
    public void emptyCategoryIsStoredWithoutNodes() {
        m_dao.save(snapshot("Nothing"));
        final CategoryAvailability summary = m_dao.findSummary("Nothing").get();
        assertEquals(0, summary.getNodeCount());
        assertEquals(100.0, summary.getAvailability(), 0.0);
        assertTrue(m_dao.findWithNodes("Nothing").get().hasNodes());
        assertEquals(0, m_dao.findNodes("Nothing", 0, 0).size());
    }

    @Test
    public void listsSummariesSortedAndRetainsOnlyKnownLabels() {
        m_dao.save(snapshot("Web Servers", node(1, 1, 0, 0)));
        m_dao.save(snapshot("Email Servers", node(2, 1, 0, 0)));
        m_dao.save(snapshot("Old Category", node(3, 1, 0, 0)));

        assertEquals(Arrays.asList("Email Servers", "Old Category", "Web Servers"), labels(m_dao.findAllSummaries()));

        m_dao.retainOnly(Arrays.asList("Web Servers", "Email Servers"));
        assertEquals(Arrays.asList("Email Servers", "Web Servers"), labels(m_dao.findAllSummaries()));
        assertTrue(m_dao.findNodeIds("Old Category").isEmpty());

        m_dao.deleteByLabel("Web Servers");
        assertEquals(Arrays.asList("Email Servers"), labels(m_dao.findAllSummaries()));
    }

    @Test
    public void unknownCategoryIsEmpty() {
        assertFalse(m_dao.findSummary("Nope").isPresent());
        assertFalse(m_dao.findWithNodes("Nope").isPresent());
        assertTrue(m_dao.findNodes("Nope", 0, 0).isEmpty());
        assertTrue(m_dao.findNodeIds("Nope").isEmpty());
        assertFalse(m_dao.findNode("Nope", 1).isPresent());
    }

    @Test
    public void deletingANodeRemovesItFromSnapshots() {
        m_dao.save(snapshot("Web Servers", node(1, 4, 0, 0), node(2, 2, 0, 0)));
        m_db.update("DELETE FROM node WHERE nodeid = ?", 2);
        assertEquals(Arrays.asList(1), m_dao.findNodeIds("Web Servers"));
    }

    private CategoryAvailability snapshot(final String label, final NodeAvailability... nodes) {
        return new CategoryAvailability(label, m_start, m_now, m_now, Arrays.asList(nodes));
    }

    private static NodeAvailability node(final int id, final long services, final long down, final long downtime) {
        return new NodeAvailability(id, services, down, downtime, DAY);
    }

    private static List<Integer> ids(final List<NodeAvailability> nodes) {
        final List<Integer> ids = new ArrayList<>();
        nodes.forEach(n -> ids.add(n.getNodeId()));
        return ids;
    }

    private static List<String> labels(final List<CategoryAvailability> summaries) {
        final List<String> labels = new ArrayList<>();
        summaries.forEach(s -> labels.add(s.getLabel()));
        return labels;
    }
}
