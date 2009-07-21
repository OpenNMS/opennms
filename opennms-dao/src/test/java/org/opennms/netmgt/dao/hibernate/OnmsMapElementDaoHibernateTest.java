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

import java.util.Collection;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;

public class OnmsMapElementDaoHibernateTest extends AbstractTransactionalDaoTestCase {
    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }

    public void testSaveOnmsMapElement() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap", "admin");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
        getOnmsMapDao().clear();

        // then a map element
        OnmsMapElement mapElement = new OnmsMapElement(map, 2,
                OnmsMapElement.NODE_TYPE,
                "Test Node Two",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        getOnmsMapElementDao().save(mapElement);
        getOnmsMapElementDao().flush();
    	getOnmsMapElementDao().clear();

        // Now pull it back up and make sure it saved.
        Object [] args = { mapElement.getId() };
        assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from element where id = ?", args));

        OnmsMapElement mapElement2 = getOnmsMapElementDao().findMapElementById(mapElement.getId());
    	assertNotSame(mapElement, mapElement2);
        assertEquals(mapElement.getMapId(), mapElement2.getMapId());
        assertEquals(mapElement.getElementId(), mapElement2.getElementId());
        assertEquals(mapElement.getType(), mapElement2.getType());
        assertEquals(mapElement.getLabel(), mapElement2.getLabel());
        assertEquals(mapElement.getIconName(), mapElement2.getIconName());
        assertEquals(mapElement.getX(), mapElement2.getX());
        assertEquals(mapElement.getY(), mapElement2.getY());
    }

    public void testFindById() {
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of the map element object then this ID may change and this test
        // will fail.
        OnmsMapElement mapElement = getOnmsMapElementDao().findMapElementById(58);
        assertEquals(1, mapElement.getMapId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
    public void testFind() {
        OnmsMap map = getOnmsMapDao().findMapById(1);
        OnmsMapElement mapElement = getOnmsMapElementDao().findMapElement(1, OnmsMapElement.NODE_TYPE, map);
        assertEquals(1, mapElement.getMapId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
    public void testFindMapElementsByMapId() {
        OnmsMap map = getOnmsMapDao().findMapById(1);
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findMapElementsByMapId(map);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(1, mapElement.getMapId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }
    
    public void testFindElementsByElementIdAndType1() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByElementIdAndType(1, OnmsMapElement.NODE_TYPE);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(1, mapElement.getMapId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }

    public void testFindElementsByElementIdAndType2() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByElementIdAndType(2, OnmsMapElement.NODE_TYPE);
        assertEquals(0,elems.size());
    }

    public void testFindElementsByElementIdAndType3() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByElementIdAndType(1, OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

    public void testFindElementsByElementIdAndType4() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByElementIdAndType(2, OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

    public void testFindElementsByType1() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByType(OnmsMapElement.NODE_TYPE);
        assertEquals(1,elems.size());
        OnmsMapElement mapElement = elems.iterator().next();
        assertEquals(1, mapElement.getMapId());
        assertEquals(1, mapElement.getElementId());
        assertEquals(OnmsMapElement.NODE_TYPE, mapElement.getType());
        assertEquals("Test Node", mapElement.getLabel());
        assertEquals(OnmsMapElement.defaultNodeIcon, mapElement.getIconName());
        assertEquals(0, mapElement.getX());
        assertEquals(10, mapElement.getY());
    }

    public void testFindElementsByType2() {
        Collection<OnmsMapElement> elems = getOnmsMapElementDao().findElementsByType(OnmsMapElement.MAP_TYPE);
        assertEquals(0,elems.size());
    }

    public void testDeleteElementsByElementIdAndType() {
        getOnmsMapElementDao().deleteElementsByElementIdAndType(1, OnmsMapElement.NODE_TYPE);
        assertNull(getOnmsMapElementDao().findMapElementById(58));
    }
}
