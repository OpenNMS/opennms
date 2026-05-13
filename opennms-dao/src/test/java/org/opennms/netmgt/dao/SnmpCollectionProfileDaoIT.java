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

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventForwarder.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpCollectionProfileDaoIT {

    @Autowired
    private SnmpCollectionProfileDao profileDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionProfile profile;

    @Before
    @Transactional
    public void setUp() {
        // Clean up previous profiles
        profileDao.deleteAll(profileDao.findAll());
        profileDao.flush();

        // Add a default SNMP collection profile
        profile = new SnmpCollectionProfile();
        profile.setName("JUnitProfile");
        profile.setEnabled(true);
        profile.setCreatedTime(new Date());
        profile.setLastModified(new Date());
        profile.setRrdStep(300);
        profile.setRrdRras("RRA:AVERAGE:0.5:1:600");
        profile.setStorageFlag("select");
        profile.setSourceNames("SourceA,SourceB");
        profile.setMaxVarsPerPdu(10);
        profileDao.saveOrUpdate(profile);
        profileDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        profileDao.deleteAll(profileDao.findAll());
        profileDao.flush();
    }

    @Test
    @Transactional
    public void testFindByName() {
        SnmpCollectionProfile found = profileDao.findByName("JUnitProfile");
        assertNotNull(found);
        assertEquals("JUnitProfile", found.getName());
        assertEquals(Integer.valueOf(300), found.getRrdStep());
        assertEquals("RRA:AVERAGE:0.5:1:600", found.getRrdRras());
        assertEquals("select", found.getStorageFlag());
        assertEquals("SourceA,SourceB", found.getSourceNames());
        assertEquals(Integer.valueOf(10), found.getMaxVarsPerPdu());
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testFindByNameReturnsNullIfNotExist() {
        SnmpCollectionProfile found = profileDao.findByName("Nonexistent");
        assertNull(found);
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionProfile found = profileDao.get(profile.getId());
        assertNotNull(found);
        assertEquals(profile.getName(), found.getName());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionProfile> enabledList = profileDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(p -> "JUnitProfile".equals(p.getName())));
        assertTrue(enabledList.stream().allMatch(SnmpCollectionProfile::getEnabled));
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        // Add a disabled profile
        SnmpCollectionProfile disabled = new SnmpCollectionProfile();
        disabled.setName("DisabledProfile");
        disabled.setEnabled(false);
        disabled.setCreatedTime(new Date());
        disabled.setLastModified(new Date());
        disabled.setRrdStep(100);
        disabled.setRrdRras("RRA:MIN:0.5:12:1440");
        disabled.setStorageFlag("all");
        disabled.setSourceNames("SourceZ");
        disabled.setMaxVarsPerPdu(2);
        profileDao.saveOrUpdate(disabled);
        profileDao.flush();

        List<SnmpCollectionProfile> enabledList = profileDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionProfile::getEnabled));
        assertFalse(enabledList.stream().anyMatch(p -> "DisabledProfile".equals(p.getName())));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionProfile> all = profileDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(p -> "JUnitProfile".equals(p.getName())));
    }
}