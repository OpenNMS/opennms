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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.EventConfigSourceDao;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfSourceDaoIT implements InitializingBean {

    @Autowired
    private EventConfigSourceDao m_dao;

    private EventConfSource m_source;

    @Before
    @Transactional
    public void setUp() {
        m_source = new EventConfSource();
        m_source.setName("JUnit Source");
        m_source.setVendor("TestVendor");
        m_source.setEnabled(true);
        m_source.setFileOrder(42);
        m_source.setDescription("JUnit Description");
        m_source.setEventCount(5);
        m_source.setCreatedTime(new Date());
        m_source.setLastModified(new Date());
        m_source.setUploadedBy("JUnit");

        m_dao.saveOrUpdate(m_source);
    }

    @Test
    @Transactional
    public void testFindByName() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("JUnit Description", found.getDescription());
    }

    @Test
    @Transactional
    public void testVendorIsPersisted() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("TestVendor", found.getVendor());
    }

    @Test
    @Transactional
    public void testFileOrderIsPersisted() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals(Integer.valueOf(42), found.getFileOrder());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<EventConfSource> list = m_dao.findAllEnabled();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.getName().equals("JUnit Source")));
    }

    @Test
    @Transactional
    public void testGetIdToNameMap() {
        Map<Long, String> map = m_dao.getIdToNameMap();
        assertNotNull(map);
        assertTrue(map.containsValue("JUnit Source"));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
}