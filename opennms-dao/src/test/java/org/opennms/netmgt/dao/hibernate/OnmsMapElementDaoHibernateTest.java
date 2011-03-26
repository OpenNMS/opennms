//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
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
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class OnmsMapElementDaoHibernateTest {
	@Autowired
	private OnmsMapElementDao m_onmsMapElementDao;
	
	@Autowired
	private OnmsMapDao m_onmsMapDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
	@Test
	@Transactional
    public void testSaveOnmsMapElement() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap", "admin");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        // then a map element
        OnmsMapElement mapElement = new OnmsMapElement(map, 2,
                OnmsMapElement.NODE_TYPE,
                "Test Node Two",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        m_onmsMapElementDao.save(mapElement);
        m_onmsMapElementDao.flush();
    	m_onmsMapElementDao.clear();

        OnmsMapElement mapElement2 = m_onmsMapElementDao.findElementById(mapElement.getId());
    	assertNotSame(mapElement, mapElement2);
        assertEquals(mapElement.getMap().getId(), mapElement2.getMap().getId());
        assertEquals(mapElement.getElementId(), mapElement2.getElementId());
        assertEquals(mapElement.getType(), mapElement2.getType());
        assertEquals(mapElement.getLabel(), mapElement2.getLabel());
        assertEquals(mapElement.getIconName(), mapElement2.getIconName());
        assertEquals(mapElement.getX(), mapElement2.getX());
        assertEquals(mapElement.getY(), mapElement2.getY());
    }

	@Test
	@Transactional
    public void testSaveOnmsMapElement1() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap1", "admin");
        m_onmsMapDao.save(map);
        m_onmsMapDao.flush();
        m_onmsMapDao.clear();

        // then a map element
        OnmsMapElement mapElement = new OnmsMapElement(map, 2,
                OnmsMapElement.NODE_HIDE_TYPE,
                "Test Node Two",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        m_onmsMapElementDao.save(mapElement);
        m_onmsMapElementDao.flush();
        m_onmsMapElementDao.clear();

        OnmsMapElement mapElement2 = m_onmsMapElementDao.findElementById(mapElement.getId());
        assertNotSame(mapElement, mapElement2);
        assertEquals(mapElement.getMap().getId(), mapElement2.getMap().getId());
        assertEquals(mapElement.getElementId(), mapElement2.getElementId());
        assertEquals(mapElement.getType(), mapElement2.getType());
        assertEquals(mapElement.getLabel(), mapElement2.getLabel());
        assertEquals(mapElement.getIconName(), mapElement2.getIconName());
        assertEquals(mapElement.getX(), mapElement2.getX());
        assertEquals(mapElement.getY(), mapElement2.getY());
    }

	@Test
	@Transactional
    public void testFindById() {
		m_databasePopulator.populateDatabase();

        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of the map element object then this ID may change and this test
        // will fail.
        int id = 63;
        OnmsMapElement mapElement = m_onmsMapElementDao.findElementById(id);
        if (mapElement == null) {
            List<OnmsMapElement> maps = m_onmsMapElementDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (OnmsMapElement current : maps) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No OnmsMapElement record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }
        assertEquals(62, mapElement.getMap().getId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
	@Test
	@Transactional
    public void testFind() {
		m_databasePopulator.populateDatabase();

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
        OnmsMapElement mapElement = m_onmsMapElementDao.findElement(1, OnmsMapElement.NODE_TYPE, map);
        assertEquals(62, mapElement.getMap().getId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
	@Test
	@Transactional
    public void testFindMapElementsByMapId() {
		m_databasePopulator.populateDatabase();

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
        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByMapId(map);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(62, mapElement.getMap().getId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
	@Test
	@Transactional
    public void testFindElementsByElementIdAndType1() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(1, OnmsMapElement.NODE_TYPE);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(62, mapElement.getMap().getId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }

	@Test
	@Transactional
    public void testFindElementsByElementIdAndType2() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(2, OnmsMapElement.NODE_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testFindElementsByElementIdAndType3() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(1, OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testFindElementsByElementIdAndType4() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(2, OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testFindElementsByElementIdAndType5() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(1, OnmsMapElement.NODE_HIDE_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testFindElementsByElementIdAndType6() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByElementIdAndType(2, OnmsMapElement.NODE_HIDE_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testFindElementsByType1() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByType(OnmsMapElement.NODE_TYPE);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(62, mapElement.getMap().getId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }

	@Test
	@Transactional
    public void testFindElementsByType2() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByType(OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

	@Test
	@Transactional
    public void testDeleteElement() {
		m_databasePopulator.populateDatabase();

        Collection<OnmsMapElement> elems = m_onmsMapElementDao.findElementsByType(OnmsMapElement.NODE_TYPE);
        assertEquals(1,elems.size());
        OnmsMapElement element = elems.iterator().next();
        m_onmsMapElementDao.delete(element);
        assertNull(m_onmsMapElementDao.findElementById(59));
    }
    
	@Test
	@Transactional
    public void testDeleteElementsByElementIdAndType() {
		m_databasePopulator.populateDatabase();

        m_onmsMapElementDao.deleteElementsByElementIdAndType(1, OnmsMapElement.NODE_TYPE);
        assertNull(m_onmsMapElementDao.findElementById(59));
    }
    
	@Test
	@Transactional
    public void testDeleteElementsByMapType() {
		m_databasePopulator.populateDatabase();

        m_onmsMapElementDao.deleteElementsByMapType(OnmsMap.USER_GENERATED_MAP);
        m_onmsMapElementDao.deleteElementsByMapType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
        assertEquals(0,m_onmsMapElementDao.findAll().size());
    }
}
