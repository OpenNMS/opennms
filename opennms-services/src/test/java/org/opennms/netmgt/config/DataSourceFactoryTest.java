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
package org.opennms.netmgt.config;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.test.mock.MockLogAppender;

public class DataSourceFactoryTest extends OpenNMSTestCase {

	private DataSource m_testDb = new DataSourceFactory();
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;
    private MockEventIpcManager m_eventMgr;
    
    private String m_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    		"<this:datasource-configuration xmlns:this=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\" \n" + 
    		"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
    		"  xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/opennms-datasources " +
    		"                       http://www.opennms.org/xsd/config/opennms-datasources.xsd \">\n" + 
    		"  <this:data-source  name=\"opennms\" class-name=\"org.postgresql.Driver\" \n" + 
    		"                     url=\"jdbc:postgresql://localhost:5432/opennms\"\n" + 
    		"                     user-name=\"opennms\"\n" + 
    		"                     password=\"opennms\" />\n" + 
    		"</this:datasource-configuration>";

	protected void setUp() throws Exception {
		super.setUp();
        MockLogAppender.setupLogging();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSecondDatabase() throws Exception {
        DataSourceFactory.getInstance();
        
        DataSourceFactory.setInstance("test2", m_testDb);
        
        m_testDb.setLoginTimeout(5);
        
        assertEquals(5, DataSourceFactory.getInstance("test2").getLoginTimeout());
	}
	
	public void testMarshallDataSourceConfig() {
		DataSourceConfiguration config;
	}
}
