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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.secret.model;

import junit.framework.TestCase;

public class DataSourceTest extends TestCase {
    private DataSource m_dataSource;
    
    public void populate() {
        m_dataSource = new DataSource("test_id", "test_name", "test_source", "test_datasource");
    }
    
    /*
     * Test constructor for 'org.opennms.secret.model.DataSource()'
     */
    public void testConstructorEmpty() {
        DataSource a = new DataSource();
        
        assertNull("uninitialized id should be null", a.getId());
        assertNull("uninitialized name should be null", a.getName());
        assertNull("uninitialized source should be null", a.getSource());
        assertNull("uninitialized datasource should be null", a.getDataSource());
    }

    /*
     * Test constructor for 'org.opennms.secret.model.DataSource(String, String, String, String)'
     */
    public void testConstructorArgs() {
        DataSource a = new DataSource("test_id", "test_name", "test_source", "test_datasource");
        
        assertEquals("id field", "test_id", a.getId());
        assertEquals("name field", "test_name", a.getName());
        assertEquals("source field", "test_source", a.getSource());
        assertEquals("datasource field", "test_datasource", a.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getDataSource()'
     */
    public void testGetDataSource() {
        populate();
        assertEquals("datasource field", "test_datasource", m_dataSource.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setDataSource(String)'
     */
    public void testSetDataSource() {
        populate();
        assertEquals("datasource field", "test_datasource", m_dataSource.getDataSource());
        m_dataSource.setDataSource("test_datasource_changed");
        assertEquals("datasource field", "test_datasource_changed", m_dataSource.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getId()'
     */
    public void testGetId() {
        populate();
        assertEquals("id field", "test_id", m_dataSource.getId());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setId(String)'
     */
    public void testSetId() {
        populate();
        assertEquals("id field", "test_id", m_dataSource.getId());
        m_dataSource.setId("test_id_changed");
        assertEquals("id field", "test_id_changed", m_dataSource.getId());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getName()'
     */
    public void testGetName() {
        populate();
        assertEquals("name field", "test_name", m_dataSource.getName());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setName(String)'
     */
    public void testSetName() {
        populate();
        assertEquals("name field", "test_name", m_dataSource.getName());
        m_dataSource.setName("test_name_changed");
        assertEquals("name field", "test_name_changed", m_dataSource.getName());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getSource()'
     */
    public void testGetSource() {
        populate();
        assertEquals("source field", "test_source", m_dataSource.getSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setSource(String)'
     */
    public void testSetSource() {
        populate();
        assertEquals("source field", "test_source", m_dataSource.getSource());
        m_dataSource.setSource("test_source_changed");
        assertEquals("source field", "test_source_changed", m_dataSource.getSource());
    }

}
