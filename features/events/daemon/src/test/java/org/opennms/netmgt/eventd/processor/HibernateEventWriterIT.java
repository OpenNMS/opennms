/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * This class tests some of the quirky behaviors of persisting events.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class HibernateEventWriterIT {

    @Autowired
    private EventWriter m_eventWriter;

    @Autowired
    private EventUtil m_eventUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testWriteEventWithParameters() throws Exception {
        final EventBuilder builder = new EventBuilder("testUei", "testSource");
        builder.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        builder.addParam("param1", "value1");
        builder.addParam("param2", 1337);
        builder.addParam("param3", true);
        builder.addParam("param4", 23.42);
        builder.addParam("param4", 42.23); // Test for duplicated values - last should win

        m_eventWriter.process(builder.getLog());

        final List<Map<String, Object>> parameters = jdbcTemplate.queryForList("SELECT name, value FROM event_parameters WHERE eventID = " + builder.getEvent().getDbid() + " ORDER BY name");
        assertEquals(4, parameters.size());

        assertEquals("param1", parameters.get(0).get("name"));
        assertEquals("value1", parameters.get(0).get("value"));

        assertEquals("param2", parameters.get(1).get("name"));
        assertEquals("1337", parameters.get(1).get("value"));

        assertEquals("param3", parameters.get(2).get("name"));
        assertEquals("true", parameters.get(2).get("value"));

        assertEquals("param4", parameters.get(3).get("name"));
        assertEquals("42.23", parameters.get(3).get("value"));
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
    public void testWriteEventWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        bldr.addParam("test", "testVal");
        final String testVal2 = "valWith\u0000Null\u0000";
        bldr.addParam("test2", testVal2);

        byte[] bytes = new byte[] { 0x07, (byte)0xD7, 0x04, 0x0A, 0x01, 0x17, 0x06, 0x00, 0x2B, 0x00, 0x00 };


        SnmpValue snmpVal = SnmpUtils.getValueFactory().getOctetString(bytes);

        assertFalse(snmpVal.isDisplayable());

        bldr.addParam("test3", snmpVal.toString());

        String b64 = EventConstants.toString(EventConstants.XML_ENCODING_BASE64, snmpVal);

        bldr.addParam("test", b64);

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        final List<Map<String, Object>> parameters = jdbcTemplate.queryForList("SELECT name, value FROM event_parameters WHERE eventID = " + event.getDbid() + " ORDER BY name");
        assertEquals(3, parameters.size());

        assertEquals("test", parameters.get(0).get("name"));
        assertEquals("B9cECgEXBgArAAA%61", parameters.get(0).get("value"));

        assertEquals("test2", parameters.get(1).get("name"));
        assertEquals("valWith%0Null%0", parameters.get(1).get("value"));

        assertEquals("test3", parameters.get(2).get("name"));
        assertEquals(snmpVal.toString(), parameters.get(2).get("value"));
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
    public void testWriteEventDescrWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);

        bldr.setDescription("abc\u0000def");

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());

        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        final String descr = jdbcTemplate.queryForObject("SELECT eventDescr FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", descr);
    }

    /**
     * Tests writing events with various distPoller values.
     * 
     * @throws SQLException
     */
    @Test
    public void testEventDistPoller() throws Exception {
        String systemId = UUID.randomUUID().toString();
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setDistPoller(systemId);
        bldr.setLogMessage("test");

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        String minionId = jdbcTemplate.queryForObject("SELECT systemId FROM events LIMIT 1", String.class);
        assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, minionId);

        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute(String.format("INSERT INTO monitoringsystems (id, location, type) VALUES ('%s', 'Hello World', '%s')", systemId, OnmsMonitoringSystem.TYPE_MINION));

        event = bldr.getEvent();
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        minionId = jdbcTemplate.queryForObject("SELECT systemId FROM events LIMIT 1", String.class);
        assertEquals(systemId, minionId);
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
	public void testWriteEventLogmsgWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);

        bldr.setLogMessage("abc\u0000def");

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        final String logMessage = jdbcTemplate.queryForObject("SELECT eventLogmsg FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", logMessage);
    }
    
    @Test
    public void testGetEventHostWithNullHost() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", null, m_eventUtil.getEventHost(event));
    }

    @Test
    public void testGetEventHostWithHostNoNodeId() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        event.setHost("192.168.1.1");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", event.getHost(), m_eventUtil.getEventHost(event));
    }
    
    @Test
    public void testGetEventHostWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now())");
        long nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Long.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");

        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        event.setNodeid(nodeId);
        event.setHost("192.168.1.1");

        assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_eventUtil.getEventHost(event));
    }
    
    @Test
    public void testGetHostNameWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_eventUtil.getHostName(1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithOneMatchNullHostname() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr) VALUES (?, ?)", nodeId, "192.168.1.1");
    
        assertEquals("getHostName should return the IP address it was passed", "192.168.1.1", m_eventUtil.getHostName(1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithTwoMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), ?)", "First Node");
        int nodeId1 = jdbcTemplate.queryForObject("SELECT nodeId FROM node WHERE nodeLabel = ?", new Object[] { "First Node" }, Integer.class);
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), ?)", "Second Node");
        int nodeId2 = jdbcTemplate.queryForObject("SELECT nodeId FROM node WHERE nodeLabel = ?", new Object[] { "Second Node" }, Integer.class);
        
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId1, "192.168.1.1", "First Interface");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId2, "192.168.1.1", "Second Interface");
        
        assertEquals("getHostName should return the IP address it was passed", "First Interface", m_eventUtil.getHostName(nodeId1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithNoHostMatch() throws Exception {
            assertEquals("getHostName should return the IP address it was passed", "192.168.1.1", m_eventUtil.getHostName(1, "192.168.1.1"));
    }

    @Test
    public void testSendEventWithService() throws Exception {
        int serviceId = 1;
        String serviceName = "some bogus service";

        jdbcTemplate.update("insert into service (serviceId, serviceName) values (?, ?)", new Object[] { serviceId, serviceName });
        
        EventBuilder bldr = new EventBuilder("uei.opennms.org/foo", "someSource");
        bldr.setLogMessage(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        bldr.setService(serviceName);

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);
        
        assertEquals("event count", new Integer(1), jdbcTemplate.queryForObject("select count(*) from events", Integer.class));
        assertEquals("event service ID", new Integer(serviceId), jdbcTemplate.queryForObject("select serviceID from events", Integer.class));
    }
}
