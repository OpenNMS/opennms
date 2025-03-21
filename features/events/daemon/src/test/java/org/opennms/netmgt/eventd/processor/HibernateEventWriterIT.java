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
package org.opennms.netmgt.eventd.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockDistPollerDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/mockSinkConsumerManager.xml"
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

        // Test for long parameter name - see NMS-10525
        final String longParamName = "param5 - with a really long name - " + IntStream.range(1, 1024)
                .mapToObj(i ->"x")
                .collect(Collectors.joining());
        builder.addParam(longParamName, longParamName);

        m_eventWriter.process(builder.getLog());

        final List<Map<String, Object>> parameters = jdbcTemplate.queryForList("SELECT name, value FROM event_parameters WHERE eventID = " + builder.getEvent().getDbid() + " ORDER BY name");
        assertEquals(5, parameters.size());

        assertEquals("param1", parameters.get(0).get("name"));
        assertEquals("value1", parameters.get(0).get("value"));

        assertEquals("param2", parameters.get(1).get("name"));
        assertEquals("1337", parameters.get(1).get("value"));

        assertEquals("param3", parameters.get(2).get("name"));
        assertEquals("true", parameters.get(2).get("value"));

        assertEquals("param4", parameters.get(3).get("name"));
        assertEquals("42.23", parameters.get(3).get("value"));

        assertEquals(longParamName, parameters.get(4).get("name"));
        assertEquals(longParamName, parameters.get(4).get("value"));
    }

    /**
     * In NMS-10525, we switched the event parameter name column from varchar(256) to text.
     *
     * This field is part of the primary key for which PostgreSQL automatically creates an index.
     * There is a limit on the size of the text field for it to be indexed using a B-tree index.
     *
     * This test verifies this limit.
     */
    @Test
    public void canHaveLargeParameterNames() {
        final int expectedParamNameLengthSupported = 2712;
        int largestLengthSupported = 0;
        for(int N : Arrays.asList(1,2,8,32,256,1024,2712,2713,8096)) {
            try {
                // Generate a event that has a parameter with both key and value of length N
                final EventBuilder builder = new EventBuilder("testUei", "testSource");
                builder.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
                final String longString = IntStream.range(1, N)
                        .mapToObj(i ->"x")
                        .collect(Collectors.joining());
                builder.addParam(longString, longString);

                // Save the event
                Log log = builder.getLog();
                Event e = log.getEvents().getEvent(0);
                m_eventWriter.process(log);

                // Retrieve the strings back out of the datatabase
                final List<Map<String, Object>> parameters = jdbcTemplate.queryForList(
                        "SELECT name, value FROM event_parameters WHERE eventID = " + e.getDbid() + " ORDER BY name");

                // Validate
                assertEquals(longString, parameters.get(0).get("name"));
                assertEquals(longString, parameters.get(0).get("value"));

                // This length was OK
                largestLengthSupported = Math.max(largestLengthSupported, N);
            } catch (Exception e) {
                // pass
            }
        }
        assertThat(largestLengthSupported, greaterThanOrEqualTo(expectedParamNameLengthSupported));
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
        assertEquals(new Long(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        final List<Map<String, Object>> parameters = jdbcTemplate.queryForList("SELECT name, value FROM event_parameters WHERE eventID = " + event.getDbid() + " ORDER BY name");
        assertEquals(3, parameters.size());

        assertEquals("test", parameters.get(0).get("name"));
        assertEquals("B9cECgEXBgArAAA=", parameters.get(0).get("value"));

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
        assertEquals(new Long(0), event.getDbid());

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
        assertEquals(new Long(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        String minionId = jdbcTemplate.queryForObject("SELECT systemId FROM events LIMIT 1", String.class);
        String originalSystemId = jdbcTemplate.queryForObject("SELECT id FROM monitoringsystems LIMIT 1", String.class);
        assertEquals(originalSystemId, minionId);

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
        assertEquals(new Long(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);

        final String logMessage = jdbcTemplate.queryForObject("SELECT eventLogmsg FROM events LIMIT 1", String.class);
        assertEquals("abc%0def", logMessage);
    }
    
    @Test
    public void testGetEventHostWithNullHost() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), 'test')");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", null, m_eventUtil.getEventHost(event));
    }

    @Test
    public void testGetEventHostWithHostNoNodeId() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), 'test')");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        // don't convert to using event builder as this is testing eventd persist functionality and needs to try 'invalid' events
        Event event = new Event();
        event.setHost("192.168.1.1");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", event.getHost(), m_eventUtil.getEventHost(event));
    }
    
    @Test
    public void testGetEventHostWithOneMatch() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), 'test')");
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
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), 'test')");
        int nodeId = jdbcTemplate.queryForObject("SELECT nodeId FROM node LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT into ipInterface (nodeId, ipAddr, ipHostname) VALUES (?, ?, ?)", nodeId, "192.168.1.1", "First Interface");
        
        assertEquals("getHostName should return the hostname for the IP address that was passed", "First Interface", m_eventUtil.getHostName(1, "192.168.1.1"));
    }
    
    @Test
    public void testGetHostNameWithOneMatchNullHostname() throws Exception {
        jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', nextVal('nodeNxtId'), now(), 'test')");
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
        assertEquals(new Long(0), event.getDbid());
        m_eventWriter.process(bldr.getLog());
        assertTrue(event.getDbid() > 0);
        
        assertEquals("event count", new Integer(1), jdbcTemplate.queryForObject("select count(*) from events", Integer.class));
        assertEquals("event service ID", new Integer(serviceId), jdbcTemplate.queryForObject("select serviceID from events", Integer.class));
    }

    public HibernateEventWriter getHibernateEventWriter() {
        return (HibernateEventWriter) m_eventWriter;
    }
}
