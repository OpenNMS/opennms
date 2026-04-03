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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
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
}
