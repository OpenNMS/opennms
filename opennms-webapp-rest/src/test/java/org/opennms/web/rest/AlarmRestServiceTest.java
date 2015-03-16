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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.xml.XmlTest.assertXpathDoesNotMatch;
import static org.opennms.core.test.xml.XmlTest.assertXpathMatches;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
        "classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmRestServiceTest extends AbstractSpringJerseyRestTestCase {
    @Autowired
    TransactionTemplate m_template;

    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
        final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        m_databasePopulator = context.getBean("databasePopulator", DatabasePopulator.class);
        m_template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                m_databasePopulator.populateDatabase();
            }
        });
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarms() throws Exception {
        String xml = sendRequest(GET, "/alarms", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms/1", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));
        assertTrue(xml.contains("<nodeLabel>node1</nodeLabel>"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmQueryByNode() throws Exception {
        String xml = sendRequest(GET, "/alarms", parseParamData("nodeId=6&limit=1"), 200);
        assertTrue(xml.contains("<alarms"));
        xml = sendRequest(GET, "/alarms", parseParamData("node.id=6&limit=1"), 200);
        assertTrue(xml.contains("<alarms"));
        xml = sendRequest(GET, "/alarms", parseParamData("node.label=node1&limit=1"), 200);
        assertTrue(xml.contains("node1"));
        xml = sendRequest(GET, "/alarms", parseParamData("ipInterface.ipAddress=192.168.1.2&limit=1"), 200);
        assertTrue(xml.contains("node1"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmQueryBySeverityEquals() throws Exception {
        String xml = null;

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=MAJOR&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));
    }

    @Test
    @JUnitTemporaryDatabase
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
    @JUnitTemporaryDatabase
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
    @Transactional
    @JUnitTemporaryDatabase
    public void testAlarmUpdates() throws Exception {
        createAlarm(OnmsSeverity.MAJOR);

        OnmsAlarm alarm = getLastAlarm();
        alarm.setAlarmAckTime(null);
        alarm.setAlarmAckUser(null);
        getAlarmDao().saveOrUpdate(alarm);
        final Integer alarmId = alarm.getId();

        sendPut("/alarms", "ack=true&alarmId=" + alarmId, 303, "/alarms/" + alarmId);
        String xml = sendRequest(GET, "/alarms/" + alarmId, 200);
        assertTrue(xml.contains("ackUser>admin<"));

        sendPut("/alarms/" + alarmId, "clear=true", 303, "/alarms/" + alarmId);
        xml = sendRequest(GET, "/alarms/" + alarmId, 200);
        assertTrue(xml.contains("severity=\"CLEARED\""));

        sendPut("/alarms/" + alarmId, "escalate=true", 303, "/alarms/" + alarmId);
        xml = sendRequest(GET, "/alarms/" + alarmId, 200);
        assertTrue(xml.contains("severity=\"NORMAL\""));

        alarm = getLastAlarm();
        alarm.setSeverity(OnmsSeverity.MAJOR);
        alarm.setAlarmAckTime(null);
        alarm.setAlarmAckUser(null);
        getAlarmDao().saveOrUpdate(alarm);

        // Log in as a normal REST user and attempt to resolve an alarm as a different user.
        // This should fail with a 403 forbidden.
        setUser("foo", new String[] { "ROLE_REST" });
        sendPut("/alarms/" + alarmId, "ack=true&ackUser=bar", 403, null);
    }

    private OnmsAlarm getLastAlarm() {
        final NavigableSet<OnmsAlarm> alarms = new TreeSet<OnmsAlarm>(new Comparator<OnmsAlarm>() {
            @Override
            public int compare(final OnmsAlarm a, final OnmsAlarm b) {
                return a.getId().compareTo(b.getId());
            }
        });
        alarms.addAll(getAlarmDao().findAll());
        return alarms.last();
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testComplexQuery() throws Exception {
        String xml = null;
        final Map<String, String> parameters = new HashMap<String, String>();

        createAlarm(OnmsSeverity.CRITICAL);

        for (final OnmsAlarm alarm : getAlarmDao().findAll()) {
            System.err.println("alarm = " + alarm);
        }
        parameters.put("offset", "00");
        parameters.put("limit", "10");
        parameters.put("orderBy", "lastEventTime");
        parameters.put("order", "desc");

        // original requirements:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&lastEventTime=2011-08-19T11:11:11.000-07:00&comparator=gt&severity=MAJOR&comparator=eq

        // modified version:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3D%20MAJOR
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity = 3");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathMatches(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathDoesNotMatch(xml, "//alarm[@severity='CRITICAL' and @id='2']");

        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity >= 3");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathMatches(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathMatches(xml, "//alarm[@severity='CRITICAL' and @id='2']");

        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity >= NORMAL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathMatches(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathMatches(xml, "//alarm[@severity='CRITICAL' and @id='2']");

        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity < NORMAL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertFalse(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathDoesNotMatch(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathDoesNotMatch(xml, "//alarm[@severity='CRITICAL' and @id='2']");
        // assertTrue(xml.contains("count=\"0\""));
        assertXpathMatches(xml, "//alarms[@totalCount='0']");

        // original requirements:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&lastEventTime=2011-08-19T11:11:11.000-07:00&comparator=gt&severity=MAJOR&comparator=eq&ackUser=myuser&comparator=eq

        // acked - modified version:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3E%20MAJOR%20AND%20alarmAckUser%20%3D%20'admin'
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity > MAJOR AND alarmAckUser = 'admin'");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertFalse(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathDoesNotMatch(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertTrue(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathMatches(xml, "//alarm[@severity='CRITICAL' and @id='2']");

        // unacked - modified version:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3E%20MAJOR%20AND%20alarmAckUser%20IS%20NULL
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity > MAJOR AND alarmAckUser IS NULL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertTrue(xml.contains("count=\"0\""));
        assertXpathMatches(xml, "//alarms[@totalCount='0']");

        // unacked - modified version:
        // http://localhost:8980/opennms/rest/alarms?offset=00&limit=10&orderBy=lastEventTime&order=desc&query=lastEventTime%20%3E%20'2011-08-19T11%3A11%3A11.000-07%3A00'%20AND%20severity%20%3C%20MAJOR%20AND%20alarmAckUser%20IS%20NULL
        parameters.put("query", "lastEventTime > '2011-08-19T11:11:11.000-07:00' AND severity < MAJOR AND alarmAckUser IS NULL");
        xml = sendRequest(GET, "/alarms", parameters, 200);
        // assertTrue(xml.contains("<alarm severity=\"NORMAL\" id=\"1\""));
        assertXpathMatches(xml, "//alarm[@severity='NORMAL' and @id='1']");
        // assertFalse(xml.contains("<alarm severity=\"CRITICAL\" id=\"2\""));
        assertXpathDoesNotMatch(xml, "//alarm[@severity='CRITICAL' and @id='2']");
    }

    private OnmsAlarm createAlarm(final OnmsSeverity severity) {
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
        return alarm;
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmClearAsAdmin() throws Exception {
        setUser("admin", new String[]{ "ROLE_ADMIN" });
        final OnmsAlarm alarm = createAlarm(OnmsSeverity.MAJOR);
        sendRequest(PUT, "/alarms/" + alarm.getId(), parseParamData("clear=true"), 303);
        final String xml = sendRequest(GET, "/alarms/" + alarm.getId(), 200);
        final OnmsAlarm fromRest = JaxbUtils.unmarshal(OnmsAlarm.class, xml);
        assertEquals(fromRest.getSeverity(), OnmsSeverity.CLEARED);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmClearAsUser() throws Exception {
        setUser("ranger", new String[]{ "ROLE_USER" });
        final OnmsAlarm alarm = createAlarm(OnmsSeverity.MAJOR);
        sendRequest(PUT, "/alarms/" + alarm.getId(), parseParamData("clear=true"), 403);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmClearAsRest() throws Exception {
        setUser("ranger", new String[]{ "ROLE_REST" });
        final OnmsAlarm alarm = createAlarm(OnmsSeverity.MAJOR);
        sendRequest(PUT, "/alarms/" + alarm.getId(), parseParamData("clear=true"), 303);
        final String xml = sendRequest(GET, "/alarms/" + alarm.getId(), 200);
        final OnmsAlarm fromRest = JaxbUtils.unmarshal(OnmsAlarm.class, xml);
        assertEquals(fromRest.getSeverity(), OnmsSeverity.CLEARED);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmClearAsMobile() throws Exception {
        setUser("ranger", new String[]{ "ROLE_MOBILE" });
        final OnmsAlarm alarm = createAlarm(OnmsSeverity.MAJOR);
        sendRequest(PUT, "/alarms/" + alarm.getId(), parseParamData("clear=true"), 303);
        final String xml = sendRequest(GET, "/alarms/" + alarm.getId(), 200);
        final OnmsAlarm fromRest = JaxbUtils.unmarshal(OnmsAlarm.class, xml);
        assertEquals(fromRest.getSeverity(), OnmsSeverity.CLEARED);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarmClearAsReadOnly() throws Exception {
        setUser("ranger", new String[]{ "ROLE_MOBILE", "ROLE_READONLY" });
        final OnmsAlarm alarm = createAlarm(OnmsSeverity.MAJOR);
        sendRequest(PUT, "/alarms/" + alarm.getId(), parseParamData("clear=true"), 403);
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
