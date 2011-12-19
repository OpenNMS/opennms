/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AlarmRestServiceTest extends AbstractSpringJerseyRestTestCase {
	private DatabasePopulator m_databasePopulator;

    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		m_databasePopulator = context.getBean("databasePopulator", DatabasePopulator.class);
        m_databasePopulator.populateDatabase();
	}

	@Test
	public void testAlarms() throws Exception {
		String xml = sendRequest(GET, "/alarms", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
		assertTrue(xml.contains("This is a test alarm"));

		xml = sendRequest(GET, "/alarms/1", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
		assertTrue(xml.contains("This is a test alarm"));
		assertTrue(xml.contains("<nodeLabel>node1</nodeLabel>"));
	}

    @Test
    public void testAlarmQueryBySeverityEquals() throws Exception {
        String xml = null;
        
        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=MAJOR&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));
    }

    @Test
    public void testAlarmQueryBySeverityLessThan() throws Exception {
        String xml = null;

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=le&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=lt&severity=NORMAL&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=lt&severity=WARNING&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));
    }

    @Test
    public void testAlarmQueryBySeverityGreaterThan() throws Exception {
        String xml = null;

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=ge&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=gt&severity=NORMAL&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=gt&severity=CLEARED&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));
    }

    @Test
    public void testComplexQuery() throws Exception {
        String xml = null;
        final Map<String,String> parameters = new HashMap<String,String>();

        createAlarm(OnmsSeverity.CRITICAL);

        for (final OnmsAlarm alarm : getAlarmDao().findAll()) {
            System.err.println("alarm = " + alarm);
        }
        parameters.put("offset", "00");
        parameters.put("limit", "10");
        parameters.put("orderBy", "lastEventTime");
        parameters.put("order", "desc");

        // original requirements: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&lastEventTime=2011-08-19T11:11:11.000-07:00&comparator=gt&severity=MAJOR&comparator=eq

        // modified version: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3D%20MAJOR
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity = 3");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity >= 3");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity >= NORMAL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));

        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity < NORMAL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertFalse(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertTrue(xml.contains("count=\"0\""));
        
        // original requirements: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&lastEventTime=2011-08-19T11:11:11.000-07:00&comparator=gt&severity=MAJOR&comparator=eq&ackUser=myuser&comparator=eq
        
        // acked - modified version: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3E%20MAJOR%20AND%20alarmAckUser%20%3D%20'admin'
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity > MAJOR AND alarmAckUser = 'admin'");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertFalse(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        
        // unacked - modified version: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3E%20MAJOR%20AND%20alarmAckUser%20IS%20NULL
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity > MAJOR AND alarmAckUser IS NULL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("count=\"0\""));
        
        // unacked - modified version: http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3C%20MAJOR%20AND%20alarmAckUser%20IS%20NULL
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity < MAJOR AND alarmAckUser IS NULL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
    }

    private void createAlarm(final OnmsSeverity severity) {
        final OnmsEvent event = getEventDao().findAll().get(0);
        
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(1);
        alarm.setNode(m_databasePopulator.getNode1());
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        alarm.setSeverity(severity);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("admin");

        getAlarmDao().save(alarm);
        getAlarmDao().flush();
    }
    
    private EventDao getEventDao() {
        return m_databasePopulator.getEventDao();
    }

    private AlarmDao getAlarmDao() {
        return m_databasePopulator.getAlarmDao();
    }

    private DistPollerDao getDistPollerDao() {
        return m_databasePopulator.getDistPollerDao();
    }
}
