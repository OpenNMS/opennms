package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import java.util.Date;

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

public class AlarmStatsRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private DatabasePopulator m_databasePopulator;
    private WebApplicationContext m_context;

	@Override
	protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging();
        m_context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        m_databasePopulator = m_context.getBean("databasePopulator", DatabasePopulator.class);
        m_databasePopulator.populateDatabase();
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

        assertTrue(xml.contains(" totalCount=\"7\""));
        assertTrue(xml.contains(" unacknowledgedCount=\"4\""));
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

    private void createAlarm(final OnmsSeverity severity, final String ackUser) {
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
        
        if (ackUser != null) {
            alarm.setAlarmAckTime(new Date());
            alarm.setAlarmAckUser(ackUser);
        }
        
        getAlarmDao().save(alarm);
        getAlarmDao().flush();
    }

    private AlarmDao getAlarmDao() {
        return m_context.getBean("alarmDao", AlarmDao.class);
    }

    private DistPollerDao getDistPollerDao() {
        return m_context.getBean("distPollerDao", DistPollerDao.class);
    }

    private EventDao getEventDao() {
        return m_context.getBean("eventDao", EventDao.class);
    }

	/*
	@Test
	public void testPostAService() throws Exception {
		
		sendPost("/NCS", serviceXML);
		
		String url = "/NCS/ServiceElementComponent/NA-SvcElemComp:9876%3Avcid(50)";		
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}

	@Test
	public void testDeleteAComponent() throws Exception {
		
		sendPost("/NCS", serviceXML);
		
		String url = "/NCS/ServiceElementComponent/NA-SvcElemComp:9876%3Avcid(50)";		
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
		
		sendRequest(DELETE, url, 200);
		
		sendRequest(GET, url, 400);
		
		sendRequest(GET, "/NCS/Service/NA-Service:123", 200);
		
	}
	
	@Test
	public void testGetANonExistingService() throws Exception {
		
		// This service should not exist
		String url = "/NCS/Service/hello:world";

		// Testing GET Collection
		sendRequest(GET, url, 400);
		
		//assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));

	}
	
	@Test
	public void testFindAServiceByAttribute() throws Exception {
		
		sendPost("/NCS", serviceXML);
		
		String url = "/NCS/attributes";
		// Testing GET Collection
		String xml = sendRequest(GET, url, 200);
		
		assertTrue(xml.contains("jnxVpnPwVpnName"));
	}
	*/

}
