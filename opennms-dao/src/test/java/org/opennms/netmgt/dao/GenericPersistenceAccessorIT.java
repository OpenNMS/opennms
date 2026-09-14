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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class GenericPersistenceAccessorIT implements InitializingBean {

    @Autowired
    private GenericPersistenceAccessor m_genericPersistenceAccessor;

    @Autowired
    private DatabasePopulator m_populator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_populator.populateDatabase();
    }

    @After
    public void tearDown() {
        m_populator.resetDatabase();
    }

    @Test
    @Transactional
    public void testFindWithoutParameters() {
        final List<OnmsNode> nodes = m_genericPersistenceAccessor.find("from OnmsNode");
        assertFalse(nodes.isEmpty());
    }

    /**
     * HQL positional parameters are 1-based ordinals (?1, ?2, ...) under Hibernate 5 and must be
     * bound accordingly; Spring's raw HibernateTemplate.find binds 0-based and fails on these.
     */
    @Test
    @Transactional
    public void testFindWithPositionalParameters() {
        List<OnmsNode> nodes = m_genericPersistenceAccessor.find("from OnmsNode as n where n.label = ?1", "node1");
        assertEquals(1, nodes.size());
        assertEquals("node1", nodes.get(0).getLabel());

        nodes = m_genericPersistenceAccessor.find("from OnmsNode as n where n.label = ?1 and n.foreignId = ?2", "node1", "1");
        assertEquals(1, nodes.size());

        // swapped values must not match: proves each ordinal binds to its own argument
        nodes = m_genericPersistenceAccessor.find("from OnmsNode as n where n.label = ?1 and n.foreignId = ?2", "1", "node1");
        assertTrue(nodes.isEmpty());

        nodes = m_genericPersistenceAccessor.find("from OnmsNode as n where n.label = ?1", "no-such-node");
        assertTrue(nodes.isEmpty());
    }
}
