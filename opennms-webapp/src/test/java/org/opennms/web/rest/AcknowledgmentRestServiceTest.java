/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AcknowledgmentRestServiceTest extends AbstractSpringJerseyRestTestCase {
    @Override
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
