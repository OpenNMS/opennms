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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class IPhoneRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private EventDao m_eventDao;

    private DistPollerDao m_distPollerDao;

    @Override
    protected void afterServletStart() throws Exception {
        final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        DatabasePopulator dbp = context.getBean("databasePopulator", DatabasePopulator.class);
        dbp.populateDatabase();
        m_distPollerDao = context.getBean("distPollerDao", DistPollerDao.class);
        m_eventDao = context.getBean("eventDao", EventDao.class);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAcknowlegement() throws Exception {
        final Pattern p = Pattern.compile("^.*<ackTime>(.*?)</ackTime>.*$", Pattern.DOTALL & Pattern.MULTILINE);
        sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=ack");
        String xml = sendRequest(GET, "/alarms/1", new HashMap<String, String>(), 200);
        Matcher m = p.matcher(xml);
        assertTrue(m.matches());
        assertTrue(m.group(1).length() > 0);
        sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=unack");
        xml = sendRequest(GET, "/alarms/1", new HashMap<String, String>(), 200);
        m = p.matcher(xml);
        assertFalse(m.matches());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAlarms() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("orderBy", "lastEventTime");
        parameters.put("order", "desc");
        parameters.put("alarmAckUser", "null");
        parameters.put("limit", "1");
        String xml = sendRequest(GET, "/alarms", parameters, 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms/1", parameters, 200);
        assertTrue(xml.contains("This is a test alarm"));
        assertTrue(xml.contains("<nodeLabel>node1</nodeLabel>"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testEvents() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        String xml = sendRequest(GET, "/events", parameters, 200);
        assertTrue(xml.contains("uei.opennms.org/test"));

        parameters.put("orderBy", "lastEventTime");
        parameters.put("order", "desc");
        parameters.put("limit", "1");
        xml = sendRequest(GET, "/events/1", parameters, 200);
        assertTrue(xml.contains("uei.opennms.org/test"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodes() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("comparator", "ilike");
        parameters.put("match", "any");
        parameters.put("label", "1%");
        parameters.put("ipInterface.ipAddress", "1%");
        parameters.put("ipInterface.ipHostName", "1%");
        String xml = sendRequest(GET, "/nodes", parameters, 200);
        assertTrue(xml.contains("<node "));
        assertTrue(xml.contains("label=\"node1\""));

        parameters.clear();
        parameters.put("comparator", "ilike");
        parameters.put("match", "any");
        parameters.put("label", "8%");
        parameters.put("ipInterface.ipAddress", "8%");
        parameters.put("ipInterface.ipHostName", "8%");
        xml = sendRequest(GET, "/nodes", parameters, 200);
        assertTrue(xml.contains("totalCount=\"0\""));

        parameters.clear();
        parameters.put("limit", "50");
        parameters.put("orderBy", "ifLostService");
        parameters.put("order", "desc");
        xml = sendRequest(GET, "/outages/forNode/1", parameters, 200);
        assertTrue(xml.contains("SNMP"));

        parameters.clear();
        parameters.put("orderBy", new String[] { "ipHostName", "ipAddress" });
        xml = sendRequest(GET, "/nodes/1/ipinterfaces", parameters, 200);
        assertTrue(xml.contains("192.168.1.1"));

        parameters.clear();
        parameters.put("orderBy", new String[] { "ifName", "ipAddress", "ifDesc" });
        xml = sendRequest(GET, "/nodes/1/snmpinterfaces", parameters, 200);
        assertTrue(xml.contains("Initial ifAlias value"));

        parameters.clear();
        parameters.put("limit", "50");
        parameters.put("node.id", "1");
        xml = sendRequest(GET, "/events", parameters, 200);
        assertTrue(xml.contains("totalCount=\"0\""));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testSnmpInterfacesForNodeId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("orderBy", new String[] { "ifName", "ipAddress", "ifDesc" });
        String xml = sendRequest(GET, "/nodes/1/snmpinterfaces", parameters, 200);
        assertTrue(xml.contains("Initial ifAlias value"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testEventsForNodeId() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(1);

        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.get("localhost"));
        event.setEventUei("uei.opennms.org/test");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setNode(node);
        m_eventDao.save(event);
        m_eventDao.flush();

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("limit", "50");
        parameters.put("node.id", "1");
        String xml = sendRequest(GET, "/events", parameters, 200);
        assertTrue(xml.contains("totalCount=\"1\""));
        assertTrue(xml.contains("uei.opennms.org/test"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testOutages() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("orderBy", "ifLostService");
        parameters.put("order", "desc");
        parameters.put("ifRegainedService", "null");
        String xml = sendRequest(GET, "/outages", parameters, 200);
        assertTrue(xml.contains("count=\"1\""));
        assertTrue(xml.contains("id=\"2\""));
        assertTrue(xml.contains("192.168.1.1"));
        assertFalse(xml.contains("<ipAddress/>"));
    }
}
