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
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class HibernateCriteriaConverterIT implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateCriteriaConverterIT.class);

    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    AlarmDao m_alarmDao;

    @Autowired
    SessionFactory m_sessionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_populator.populateDatabase();
        MockLogAppender.setupLogging(true);
        LOG.debug("==============================================");
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeQuery() throws Exception {
        List<OnmsNode> nodes;

        // first, try with OnmsCriteria
        final OnmsCriteria crit = new OnmsCriteria(OnmsNode.class);
        crit.add(org.hibernate.criterion.Restrictions.isNotNull("id"));
        nodes = m_nodeDao.findMatching(crit);
        assertEquals(6, nodes.size());

        // then the same with the builder
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id");
        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(6, nodes.size());

        cb.eq("label", "node1").join("ipInterfaces", "ipInterface").eq("ipInterface.ipAddress", "192.168.1.1");
        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(1, nodes.size());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeIlikeQuery() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id").eq("label", "node1").alias("ipInterfaces", "iface").ilike("iface.ipAddress", "1%");
        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(3, nodes.size());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testDistinctQuery() {
        List<OnmsNode> nodes = null;

        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id").distinct();
        cb.eq("label", "node1").join("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN).eq("ipInterface.ipAddress", "192.168.1.1");

        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(1, nodes.size());
        assertEquals(Integer.valueOf(1), nodes.get(0).getId());
    }

    /**
     * Mirrors the v1 /rest/alarms criteria: an eager fetch alongside distinct().
     * The distinct rewrite replaces the criteria object, so fetch modes have to
     * be applied afterwards or the association falls back to a lazy proxy that
     * is read in a separate statement. See NMS-20161.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testDistinctPreservesEagerFetch() {
        final CriteriaBuilder cb = alarmCriteriaBuilder();
        cb.distinct();

        // the populated alarm and its event must not already be in the session,
        // or a lazy association would resolve from the first-level cache
        m_sessionFactory.getCurrentSession().clear();

        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(cb.toCriteria());
        assertEquals(1, alarms.size());

        final OnmsAlarm alarm = alarms.get(0);
        assertNotNull(alarm.getLastEvent());
        assertFalse("lastEvent should be join-fetched, not a lazy proxy",
                    alarm.getLastEvent() instanceof HibernateProxy);
    }

    /**
     * Applying the fetch modes to the outer criteria must not reintroduce the
     * duplicate rows that distinct() is there to collapse.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testDistinctWithEagerFetchStillDeduplicates() {
        m_sessionFactory.getCurrentSession().clear();
        final List<OnmsAlarm> notDistinct = m_alarmDao.findMatching(alarmCriteriaBuilder().toCriteria());

        final CriteriaBuilder cb = alarmCriteriaBuilder();
        cb.distinct();
        m_sessionFactory.getCurrentSession().clear();
        final List<OnmsAlarm> distinct = m_alarmDao.findMatching(cb.toCriteria());

        assertTrue("the to-many join should multiply the single alarm into several rows",
                   notDistinct.size() > 1);
        assertEquals(1, distinct.size());
    }

    /**
     * Ordering is applied to the outer criteria after the distinct rewrite
     * (NMS-7830); the fetch modes must not disturb that.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testDistinctWithEagerFetchKeepsOrdering() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.fetch("assetRecord", FetchType.EAGER);
        cb.alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        cb.orderBy("label").desc();
        cb.distinct();

        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(6, nodes.size());
        for (int i = 1; i < nodes.size(); i++) {
            assertFalse("nodes should be ordered by label descending",
                        nodes.get(i - 1).getLabel().compareTo(nodes.get(i).getLabel()) < 0);
        }
    }

    /**
     * A to-many association cannot be join-fetched by a distinct() criteria: the
     * join would return one outer row per element and undo the rewrite. Such a
     * fetch is dropped, leaving the association to load lazily as it did before
     * fetch modes reached the outer criteria.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testDistinctDropsToManyEagerFetch() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.fetch("ipInterfaces", FetchType.EAGER);
        cb.distinct();

        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(6, nodes.size());
        assertFalse("the interfaces should still be reachable, just not join-fetched",
                    nodes.get(0).getIpInterfaces().isEmpty());
    }

    /**
     * limit() becomes setMaxResults() on the outer criteria, so it counts rows.
     * Dropping the to-many fetch is what keeps those rows one-per-entity.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testDistinctWithToManyEagerFetchStillPages() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.fetch("ipInterfaces", FetchType.EAGER);
        cb.orderBy("label").desc();
        cb.distinct();
        cb.limit(2);

        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(2, nodes.size());
        assertEquals("limit should count nodes, not joined interface rows", 2, idsOf(nodes).size());
    }

    /**
     * Only the distinct() path drops the fetch. Without it, a to-many join fetch
     * multiplies the rows as it always has.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testToManyEagerFetchSurvivesWithoutDistinct() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.fetch("ipInterfaces", FetchType.EAGER);

        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(6, idsOf(nodes).size());
        assertTrue("the join fetch should return one row per interface",
                   nodes.size() > idsOf(nodes).size());
    }

    private Set<Integer> idsOf(final List<OnmsNode> nodes) {
        return nodes.stream().map(OnmsNode::getId).collect(Collectors.toSet());
    }

    /**
     * The to-many join on node.ipInterfaces is what makes distinct()
     * load-bearing here: without it the single alarm comes back once per
     * interface.
     */
    private CriteriaBuilder alarmCriteriaBuilder() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
        cb.fetch("lastEvent", FetchType.EAGER);
        cb.alias("node", "node", JoinType.LEFT_JOIN);
        cb.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        return cb;
    }
}
