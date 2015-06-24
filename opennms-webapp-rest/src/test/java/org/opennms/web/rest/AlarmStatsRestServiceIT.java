/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.xml.XmlTest.assertXpathMatches;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class AlarmStatsRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmStatsRestServiceTest.class);

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private AlarmDao m_alarmDao;

    private int count = 0;

	@Override
	protected void afterServletStart() throws Exception {
	    count = 0;
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
        for (final OnmsEvent event : m_eventDao.findAll()) {
            m_eventDao.delete(event);
        }
        m_eventDao.flush();
        for (final OnmsAlarm alarm : m_alarmDao.findAll()) {
            m_alarmDao.delete(alarm);
        }
        m_alarmDao.flush();
	}

    @Test
    public void testGetAlarmStats() throws Exception {
        createAlarm(OnmsSeverity.CLEARED, "admin");
        createAlarm(OnmsSeverity.MAJOR, "admin");
        createAlarm(OnmsSeverity.CRITICAL, "admin");
        createAlarm(OnmsSeverity.CRITICAL, null);
        createAlarm(OnmsSeverity.MINOR, null);
        createAlarm(OnmsSeverity.NORMAL, null);

        final String xml = sendRequest(GET, "/stats/alarms", 200);

        assertTrue(xml.contains(" totalCount=\"6\""));
        assertTrue(xml.contains(" unacknowledgedCount=\"3\""));
        assertTrue(xml.contains(" acknowledgedCount=\"3\""));
    }

    @Test
    public void testGetAlarmStatsBySeverity() throws Exception {
        createAlarm(OnmsSeverity.CLEARED, "admin");
        createAlarm(OnmsSeverity.MAJOR, "admin");
        createAlarm(OnmsSeverity.CRITICAL, "admin");
        createAlarm(OnmsSeverity.CRITICAL, null);
        createAlarm(OnmsSeverity.MINOR, null);
        createAlarm(OnmsSeverity.NORMAL, null);

        final String xml = sendRequest(GET, "/stats/alarms", parseParamData("comparator=ge&severity=MAJOR"), 200);

        assertTrue(xml.contains(" totalCount=\"3\""));
        assertTrue(xml.contains(" unacknowledgedCount=\"1\""));
        assertTrue(xml.contains(" acknowledgedCount=\"2\""));
    }

    @Test
    public void testNewestAndOldestBySeverity() throws Exception {
    	final OnmsAlarm oldestAckedAlarm   = createAlarm(OnmsSeverity.WARNING, "admin");
    	final OnmsAlarm newestAckedAlarm   = createAlarm(OnmsSeverity.WARNING, "admin");
    	final OnmsAlarm oldestUnackedAlarm = createAlarm(OnmsSeverity.WARNING, null);
    	final OnmsAlarm newestUnackedAlarm = createAlarm(OnmsSeverity.WARNING, null);
        
        final String xml = sendRequest(GET, "/stats/alarms/by-severity", 200);

        final String oldestAckedXml   = getXml("oldestAcked", xml);
        final String newestAckedXml   = getXml("newestAcked", xml);
        final String oldestUnackedXml = getXml("oldestUnacked", xml);
        final String newestUnackedXml = getXml("newestUnacked", xml);

        assertXpathMatches("should contain WARNING with ID#" + oldestAckedAlarm.getId(), oldestAckedXml, "//alarm[@severity='WARNING' and @id='" + oldestAckedAlarm.getId() + "']");
        assertTrue(oldestAckedXml.contains("<firstEventTime>2010-01-01T00:00:00"));

        assertXpathMatches("should contain WARNING with ID#" + newestAckedAlarm.getId(), newestAckedXml, "//alarm[@severity='WARNING' and @id='" + newestAckedAlarm.getId() + "']");
        assertTrue(newestAckedXml.contains("<firstEventTime>2010-01-01T01:00:00"));

        assertXpathMatches("should contain WARNING with ID#" + oldestUnackedAlarm.getId(), oldestUnackedXml, "//alarm[@severity='WARNING' and @id='" + oldestUnackedAlarm.getId() + "']");
        assertTrue(oldestUnackedXml.contains("<firstEventTime>2010-01-01T02:00:00"));

        assertXpathMatches("should contain WARNING with ID#" + newestUnackedAlarm.getId(), newestUnackedXml, "//alarm[@severity='WARNING' and @id='" + newestUnackedAlarm.getId() + "']");
        assertTrue(newestUnackedXml.contains("<firstEventTime>2010-01-01T03:00:00"));
    }

    private String getXml(final String tag, final String xml) {
        final Pattern p = Pattern.compile("(<" + tag + ">.*?</" + tag + ">)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        final Matcher m = p.matcher(xml);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    @Test
    public void testGetAlarmStatsSeverityList() throws Exception {
        createAlarm(OnmsSeverity.CLEARED, "admin");
        createAlarm(OnmsSeverity.MAJOR, "admin");
        createAlarm(OnmsSeverity.CRITICAL, "admin");
        createAlarm(OnmsSeverity.CRITICAL, null);
        createAlarm(OnmsSeverity.MINOR, null);
        createAlarm(OnmsSeverity.NORMAL, null);

        String xml = sendRequest(GET, "/stats/alarms/by-severity", 200);

        assertTrue(xml.contains("<severities>"));
        assertTrue(xml.contains("<alarmStatistics"));
        assertTrue(xml.contains("severity="));
        
        xml = sendRequest(GET, "/stats/alarms/by-severity", parseParamData("severities=MINOR,NORMAL"), 200);
        
        assertFalse(xml.contains("CLEARED"));
        assertFalse(xml.contains("CRITICAL"));
        assertTrue(xml.contains("MINOR"));
        assertTrue(xml.contains("NORMAL"));
    }

    private OnmsAlarm createAlarm(final OnmsSeverity severity, final String ackUser) {
        final OnmsEvent event = createEvent();
        
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(m_distPollerDao.load("localhost"));
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(1);
        alarm.setNode(m_databasePopulator.getNode1());
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.UNPINGABLE_ADDRESS);
        alarm.setSeverity(severity);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        
        if (ackUser != null) {
            alarm.setAlarmAckTime(new Date());
            alarm.setAlarmAckUser(ackUser);
        }
        
        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        
        LOG.debug("CreateAlarm: {}", alarm);

        return alarm;
    }

    protected OnmsEvent createEvent() {
        final Calendar c = new GregorianCalendar();
        c.set(2010, Calendar.JANUARY, 1, 0, 0, 0);

        long time = c.getTimeInMillis();
        time = (time - (time % 1000));
        final Date date = new Date(time + (count * 60 * 60 * 1000));

        final OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventUei("uei.opennms.org/test/" + count);
        event.setEventCreateTime(date);
        event.setEventTime(date);
        event.setEventDescr("Test event " + count);
        event.setEventDisplay("Y");
        event.setEventLog("Y");
        event.setEventHost("es-with-the-most-es");
        event.setEventLogMsg("Test event " + count + " (log)");
        event.setEventSeverity(OnmsSeverity.MAJOR.getId());
        event.setEventSource("AlarmStatsRestServiceTest");
        event.setNode(m_databasePopulator.getNode1());

        m_eventDao.save(event);
        m_eventDao.flush();

        count++;

        return event;
    }
}
