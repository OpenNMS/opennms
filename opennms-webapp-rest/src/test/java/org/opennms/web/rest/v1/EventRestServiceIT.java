/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class EventRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void testBetween() throws Exception {
        String xml;
        xml = sendRequest(GET, "/events/between", parseParamData("begin=2010-01-01T00:00:00Z"), 200);
        assertTrue(xml.contains("<createTime>"));
        xml = sendRequest(GET, "/events/between", parseParamData("begin=2010-01-01T00:00:00Z&end=2010-01-01T01:00:00Z"), 200);
        assertTrue(xml.contains("totalCount=\"0\""));
        xml = sendRequest(GET, "/events/between", parseParamData("end=2010-01-01T01:00:00Z"), 200);
        assertTrue(xml.contains("totalCount=\"0\""));
    }

    @Test
    public void canPublishEventToBus() throws Exception {
        // Create some test event
        Event e = new Event();
        e.setUei("some.uei");
        e.setHost("from-some-host");

        // Setup the anticipator
        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        anticipator.anticipateEvent(e);

        // POST the event to the REST API
        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.ACCEPTED.getStatusCode());

        // Verify
        m_eventMgr.finishProcessingEvents();
        anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Test
    public void testBadDbidEvent() throws Exception {
        final Event e = new Event();
        e.setDbid(-1);
        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPostBadStateEvent() throws Exception {
        final Event e = new Event();
        final Operaction action = new Operaction();
        action.setState("monkey");
        e.addOperaction(action);
        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testStrangeDate() throws Exception {
        final String xml = "<event xmlns=\"http://xmlns.opennms.org/xsd/event\">\n" +
                "   <uei>some.uei</uei>\n" +
                // /* works */ "   <time>Thursday, January 1, 1970 12:00:00 AM GMT</time>\n" +
                /* fails */ "   <time>Wednesday, November 08, 2017  3:07 PM EST</time>\n" +
                "   <host>from-some-host</host>\n" +
                "</event>";
        sendData(POST, MediaType.APPLICATION_XML, "/events", xml, Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
