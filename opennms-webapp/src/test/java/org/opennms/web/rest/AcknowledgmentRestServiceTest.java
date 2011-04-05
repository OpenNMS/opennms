package org.opennms.web.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AcknowledgmentRestServiceTest extends AbstractSpringJerseyRestTestCase {
	protected void afterServletStart() {
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		final DatabasePopulator dbp = context.getBean("databasePopulator", DatabasePopulator.class);
		dbp.populateDatabase();
//		m_notifDao = context.getBean("notificationDao", NotificationDao.class);
	}

	@Test
	public void testAcknowlegeNotification() throws Exception {
	    final Pattern p = Pattern.compile("^.*<answeredBy>(.*?)</answeredBy>.*$", Pattern.DOTALL & Pattern.MULTILINE);
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "notifId=1&action=ack");
	    String xml = sendRequest(GET, "/notifications/1", new HashMap<String,String>(), 200);
	    Matcher m = p.matcher(xml);
	    assertTrue(m.matches());
	    assertTrue(m.group(1).equals("admin"));
        sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "notifId=1&action=unack");
        xml = sendRequest(GET, "/notifications/1", new HashMap<String,String>(), 200);
        m = p.matcher(xml);
        assertFalse(m.matches());
	}

	@Test
	public void testAcknowlegeAlarm() throws Exception {
	    final Pattern p = Pattern.compile("^.*<ackTime>(.*?)</ackTime>.*$", Pattern.DOTALL & Pattern.MULTILINE);
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=ack");
	    String xml = sendRequest(GET, "/alarms/1", new HashMap<String,String>(), 200);
	    Matcher m = p.matcher(xml);
	    assertTrue(m.matches());
	    assertTrue(m.group(1).length() > 0);
        sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=unack");
        xml = sendRequest(GET, "/alarms/1", new HashMap<String,String>(), 200);
        m = p.matcher(xml);
        assertFalse(m.matches());
	}


}
