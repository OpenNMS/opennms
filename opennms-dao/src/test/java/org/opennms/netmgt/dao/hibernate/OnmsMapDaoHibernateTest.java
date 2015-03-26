/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class OnmsMapDaoHibernateTest implements InitializingBean {
	@Autowired
	private OnmsMapDao m_onmsMapDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
    private static boolean m_populated = false;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() {
        try {
            if (!m_populated) {
                m_databasePopulator.populateDatabase();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

	@Test
	@Transactional
    public void testSaveOnmsMap() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap", "admin");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
    	m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
    	assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap2() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap2", "admin",969,726);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode().trim(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());

    }

	@Test
	@Transactional
    public void testSaveOnmsMap3() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap3", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner().trim());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap4() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap4", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.USER_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.USER_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap5() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap5", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.AUTOMATICALLY_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getType(), OnmsMap.AUTOMATICALLY_GENERATED_MAP);
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

	@Test
	@Transactional
    public void testSaveOnmsMap6() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap6", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.AUTOMATICALLY_GENERATED_MAP,800,600);
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        OnmsMap map2 = m_onmsMapDao.findMapById(map.getId());
        
        map2.setType(OnmsMap.AUTOMATIC_SAVED_MAP);
        m_onmsMapDao.save(map2);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
       
        OnmsMap map3 = m_onmsMapDao.findMapById(map.getId());
        assertNotSame(map2, map3);
        assertEquals(map2.getName(), map3.getName());
        assertEquals(map2.getOwner(), map3.getOwner());
        assertEquals(map2.getType(), OnmsMap.AUTOMATIC_SAVED_MAP);
        assertEquals(map2.getAccessMode(), map3.getAccessMode().trim());
        assertEquals(map2.getUserLastModifies(), map3.getUserLastModifies());
        assertEquals(map2.getLastModifiedTime(), map3.getLastModifiedTime());
        assertEquals(map2.getCreateTime(), map3.getCreateTime());
        assertEquals(map2.getWidth(), map3.getWidth());
        assertEquals(map2.getHeight(), map3.getHeight());
    }

	@Test
	@Transactional
    public void testFindById() {
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of this object then this ID may change and this test will fail.
        //
        int id = 62;
        OnmsMap map = m_onmsMapDao.findMapById(id);
        if (map == null) {
            List<OnmsMap> maps = m_onmsMapDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (OnmsMap current : maps) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No OnmsMap record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }
        assertNotNull(map);
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByName() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByName("DB_Pop_Test_Map");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByNameAndTypeOk() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.USER_GENERATED_MAP);

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByNameAndTypeKo() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.AUTOMATICALLY_GENERATED_MAP);

        assertEquals(0, maps.size());
    }


	@Test
	@Transactional
    public void testFindMapsLike() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsLike("Pop_Test");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

	@Test
	@Transactional
    public void testFindMapsByType() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByType("X");
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindAutoMaps() {
        Collection<OnmsMap> maps = m_onmsMapDao.findAutoMaps();
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindSaveMaps() {
        Collection<OnmsMap> maps = m_onmsMapDao.findSaveMaps();
        assertEquals(0, maps.size());
    }

	@Test
	@Transactional
    public void testFindUserMaps() {
        Collection<OnmsMap> maps = m_onmsMapDao.findUserMaps();
        assertEquals(1, maps.size());
    }

	@Test
	@Transactional
    public void testDeleteOnmsMap() {
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of this object then this ID may change and this test will fail.
        //
        int id = 62;
        OnmsMap map = m_onmsMapDao.findMapById(id);
        if (map == null) {
            List<OnmsMap> maps = m_onmsMapDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (OnmsMap current : maps) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No OnmsMap record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }

        assertNotNull(map);
        m_onmsMapDao.delete(map);

        assertNull(m_onmsMapDao.findMapById(61));
    }

	@Test
	@Transactional
    public void testFindMapByOwner() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByOwner("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }
    
	@Test
	@Transactional
    public void testFindMapbyGroup() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByGroup("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());        
    }

	@Test
	@Transactional
    public void testFindMapbyGroup1() {
        Collection<OnmsMap> maps = m_onmsMapDao.findMapsByGroup("");
        assertEquals(0, maps.size());
    }

    
	@Test
	@Transactional
    public void testFindVisibleMapByGroup() {
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
        Collection<OnmsMap> maps = m_onmsMapDao.findVisibleMapsByGroup("testGroup");
        assertEquals(2, maps.size());
    }

	@Test
	@Transactional
    public void testFindVisibleMapByGroup2() {
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();
        Collection<OnmsMap> maps = m_onmsMapDao.findVisibleMapsByGroup("wrongGroup");
        assertEquals(1, maps.size());
    }
}
