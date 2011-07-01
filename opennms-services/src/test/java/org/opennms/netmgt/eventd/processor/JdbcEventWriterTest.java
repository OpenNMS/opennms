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
// Modifications:
//
// 2008 Feb 16: Move testSendEventWithService here from EventdSpringTest. - dj@opennms.org
// 2008 Feb 06: Add tests for bugs from bug #2247. - dj@opennms.org
// 2008 Jan 28: Add a test for getHostName when there is no match in the DB. - dj@opennms.org
// 2008 Jan 26: Change to use dependency injection for EventWriter and refactor
//              quite a bit. - dj@opennms.org
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
package org.opennms.netmgt.eventd.processor;

import java.sql.Connection;
import java.sql.SQLException;

import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;
import org.opennms.netmgt.eventd.JdbcEventdServiceManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.trapd.EventConstants;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class tests some of the quirky behaviors of presisting events.
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class JdbcEventWriterTest extends PopulatedTemporaryDatabaseTestCase {
    private JdbcEventWriter m_jdbcEventWriter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        JdbcEventdServiceManager eventdServiceManager = new JdbcEventdServiceManager();
        eventdServiceManager.setDataSource(getDataSource());
        eventdServiceManager.afterPropertiesSet();
        
        m_jdbcEventWriter = new JdbcEventWriter();
        m_jdbcEventWriter.setEventdServiceManager(eventdServiceManager);
        m_jdbcEventWriter.setDataSource(getDataSource());
        m_jdbcEventWriter.setGetNextIdString("SELECT nextval('eventsNxtId')");
        m_jdbcEventWriter.afterPropertiesSet();
    }

    /**
     * tests sequence of newly initialized db
     */
    public void testNextEventId() {
        int nextId = getJdbcTemplate().queryForInt(m_jdbcEventWriter.getGetNextIdString());
        
        // an empty db should produce '1' here
        assertEquals(1, nextId);
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    public void testWriteEventWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");
        bldr.addParam("test", "testVal");
        final String testVal2 = "valWith\u0000Null\u0000";
        bldr.addParam("test2", testVal2);

        byte[] bytes = new byte[] { 0x07, (byte)0xD7, 0x04, 0x0A, 0x01, 0x17, 0x06, 0x00, 0x2B, 0x00, 0x00 };


        SnmpValue snmpVal = SnmpUtils.getValueFactory().getOctetString(bytes);

        assertFalse(snmpVal.isDisplayable());

        bldr.addParam("test3", snmpVal.toString());

        String b64 = EventConstants.toString(EventConstants.XML_ENCODING_BASE64, snmpVal);

        bldr.addParam("test", b64);

        m_jdbcEventWriter.process(null, bldr.getEvent());
        final String parms = jdbcTemplate.queryForObject("SELECT eventParms FROM events LIMIT 1", String.class);
        assertEquals("test=testVal(string,text);test2=valWith%0Null%0(string,text);test3=" + snmpVal.toString() + "(string,text);test=B9cECgEXBgArAAA%61(string,text)", parms);
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    public void testWriteEventDescrWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");

        bldr.setDescription("abc\u0000def");

        m_jdbcEventWriter.process(null, bldr.getEvent());
        final String descr = jdbcTemplate.queryForObject("SELECT eventDescr FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", descr);
    }
    
    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    public void testWriteEventLogmsgWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");

        bldr.setLogMessage("abc\u0000def");

        m_jdbcEventWriter.process(null, bldr.getEvent());
        final String logMessage = jdbcTemplate.queryForObject("SELECT eventLogmsg FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", logMessage);
    }
    
    public void testGetEventHostWithNullHost() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        Event event = new Event();
        
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the hostname for the IP address that was passed", null, m_jdbcEventWriter.getEventHost(event, connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetEventHostWithHostNoNodeId() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        Event event = new Event();
        event.setHost("192.168.1.1");
        
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the hostname for the IP address that was passed", event.getHost(), m_jdbcEventWriter.getEventHost(event, connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetEventHostWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");

        Event event = new Event();
        event.setNodeid(nodeId);
        event.setHost("192.168.1.1");

        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_jdbcEventWriter.getEventHost(event, connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetHostNameWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_jdbcEventWriter.getHostName(1, "192.168.1.1", connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetHostNameWithOneMatchNullHostname() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr) VALUES (?, ?)", nodeId, "192.168.1.1");
        
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the IP address it was passed", "192.168.1.1", m_jdbcEventWriter.getHostName(1, "192.168.1.1", connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetHostNameWithTwoMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeLabel) VALUES (nextVal('nodeNxtId'), now(), ?)", "First Node");
        int nodeId1 = jdbcTemplate.queryForInt("SELECT nodeId FROM node WHERE nodeLabel = ?", "First Node");
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeLabel) VALUES (nextVal('nodeNxtId'), now(), ?)", "Second Node");
        int nodeId2 = jdbcTemplate.queryForInt("SELECT nodeId FROM node WHERE nodeLabel = ?", "Second Node");
        
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId1, "192.168.1.1", "First Interface");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId2, "192.168.1.1", "Second Interface");
        
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the IP address it was passed", "First Interface", m_jdbcEventWriter.getHostName(nodeId1, "192.168.1.1", connection));
        } finally {
            connection.close();
        }
    }
    
    public void testGetHostNameWithNoHostMatch() throws Exception {
        Connection connection = getDataSource().getConnection();
        try {
            assertEquals("getHostName should return the IP address it was passed", "192.168.1.1", m_jdbcEventWriter.getHostName(1, "192.168.1.1", connection));
        } finally {
            connection.close();
        }
    }

    public void testSendEventWithService() throws Exception {
        int serviceId = 1;
        String serviceName = "some bogus service";

        jdbcTemplate.update("insert into service (serviceId, serviceName) values (?, ?)", new Object[] { serviceId, serviceName });
        
        EventBuilder builder = new EventBuilder("uei.opennms.org/foo", "someSource");
        builder.setLogMessage("logndisplay");
        builder.setService(serviceName);
        

        m_jdbcEventWriter.process(null, builder.getEvent());
        
        assertEquals("event count", 1, getJdbcTemplate().queryForInt("select count(*) from events"));
        assertEquals("event service ID", serviceId, getJdbcTemplate().queryForInt("select serviceID from events"));
    }
}
