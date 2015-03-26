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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.events.api.EventConstants;
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
public class HibernateEventWriterTest {

    @Autowired
    private EventWriter m_jdbcEventWriter;

    @Autowired
    private EventUtil m_eventUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
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

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_jdbcEventWriter.process(null, event);
        assertTrue(event.getDbid() > 0);

        final String parms = jdbcTemplate.queryForObject("SELECT eventParms FROM events LIMIT 1", String.class);
        assertEquals("test=testVal(string,text);test2=valWith%0Null%0(string,text);test3=" + snmpVal.toString() + "(string,text);test=B9cECgEXBgArAAA%61(string,text)", parms);
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
    public void testWriteEventDescrWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");

        bldr.setDescription("abc\u0000def");

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_jdbcEventWriter.process(null, event);
        assertTrue(event.getDbid() > 0);

        final String descr = jdbcTemplate.queryForObject("SELECT eventDescr FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", descr);
    }
    
    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    @Test
	public void testWriteEventLogmsgWithNull() throws Exception {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");

        bldr.setLogMessage("abc\u0000def");

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_jdbcEventWriter.process(null, event);
        assertTrue(event.getDbid() > 0);

        final String logMessage = jdbcTemplate.queryForObject("SELECT eventLogmsg FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", logMessage);
    }
    
    @Test
    public void testGetEventHostWithNullHost() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", null, m_eventUtil.getEventHost(event));
    }

    @Test
    public void testGetEventHostWithHostNoNodeId() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        event.setHost("192.168.1.1");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", event.getHost(), m_eventUtil.getEventHost(event));
    }
    
    @Test
    public void testGetEventHostWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        long nodeId = jdbcTemplate.queryForLong("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");

        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        event.setNodeid(nodeId);
        event.setHost("192.168.1.1");

        assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_eventUtil.getEventHost(event));
    }
    
    @Test
    public void testGetHostNameWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_eventUtil.getHostName(1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithOneMatchNullHostname() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime) VALUES (nextVal('nodeNxtId'), now())");
        int nodeId = jdbcTemplate.queryForInt("SELECT nodeId FROM node LIMIT 1");
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr) VALUES (?, ?)", nodeId, "192.168.1.1");
    
        assertEquals("getHostName should return the IP address it was passed", "192.168.1.1", m_eventUtil.getHostName(1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithTwoMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeLabel) VALUES (nextVal('nodeNxtId'), now(), ?)", "First Node");
        int nodeId1 = jdbcTemplate.queryForInt("SELECT nodeId FROM node WHERE nodeLabel = ?", "First Node");
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeLabel) VALUES (nextVal('nodeNxtId'), now(), ?)", "Second Node");
        int nodeId2 = jdbcTemplate.queryForInt("SELECT nodeId FROM node WHERE nodeLabel = ?", "Second Node");
        
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
        bldr.setLogMessage("logndisplay");
        bldr.setService(serviceName);

        Event event = bldr.getEvent();
        assertEquals(new Integer(0), event.getDbid());
        m_jdbcEventWriter.process(null, event);
        assertTrue(event.getDbid() > 0);
        
        assertEquals("event count", 1, jdbcTemplate.queryForInt("select count(*) from events"));
        assertEquals("event service ID", serviceId, jdbcTemplate.queryForInt("select serviceID from events"));
    }
}
