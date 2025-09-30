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
package org.opennms.web.rest.v2;


import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.EventConfEventDto;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.EventConfRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"

})

@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfRestServiceIT {

    @Autowired
    private EventConfRestApi eventConfRestApi;

    private SecurityContext securityContext;

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("integration-user");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }

    @Test
    @Transactional
    public void testEventsConfWithEventConfXMLFiles() throws Exception {
        String[] filenames = {"eventconf.xml", "opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);

            attachments.add(att);
        }

        Response resp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked") List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked") List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertTrue("eventconf.xml should be excluded", success.stream().noneMatch(m -> "eventconf.xml".equals(m.get("file"))));
        assertEquals(2, success.size());
        assertEquals("opennms.alarm.events.xml", success.get(0).get("file"));
        assertEquals(3, success.get(0).get("eventCount"));
        assertEquals("opennms", success.get(0).get("vendor"));
        assertEquals("Cisco.airespace.xml", success.get(1).get("file"));
        assertEquals(101, success.get(1).get("eventCount"));
        assertEquals("Cisco", success.get(1).get("vendor"));
        assertTrue(errors.isEmpty());
    }


    @Test
    @Transactional
    public void testEventsConfWithoutEventConfXMLFiles() throws Exception {
        String[] filenames = {"opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }

        Response resp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked") List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked") List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertEquals(filenames.length, success.size());
        assertEquals("opennms.alarm.events.xml", success.get(0).get("file"));
        assertEquals(3, success.get(0).get("eventCount"));
        assertEquals("opennms", success.get(0).get("vendor"));
        assertEquals("Cisco.airespace.xml", success.get(1).get("file"));
        assertEquals(101, success.get(1).get("eventCount"));
        assertEquals("Cisco", success.get(1).get("vendor"));
        assertTrue(errors.isEmpty());
    }

    @Test
    @Transactional
    public void testEmptyAttachments_ShouldReturnEmptyResults() throws Exception {
        List<Attachment> attachments = new ArrayList<>();
        Response resp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertTrue(success.isEmpty());
        assertTrue(errors.isEmpty());
    }

    @Test
    @Transactional
    public void testNullSecurityContext_ShouldSetUnknownUser() throws Exception {
        String filename = "Cisco.airespace.xml";
        InputStream is = getClass().getResourceAsStream("/EVENTS-CONF/" + filename);
        assertNotNull(is);

        Attachment att = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(is);

        List<Attachment> attachments = List.of(att);

        Response resp = eventConfRestApi.uploadEventConfFiles(attachments, null);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");

        assertEquals(1, success.size());
        assertEquals("Cisco", success.get(0).get("vendor"));
    }

    @Test
    public void testMalformedEventFile_ShouldAppearInErrors() throws Exception {
        String filename = "test.invalid.xml";
        InputStream is = getClass().getResourceAsStream("/EVENTS-CONF/" + filename);

        Attachment att = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(is);

        List<Attachment> attachments = List.of(att);

        Response resp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertTrue(success.isEmpty());
        assertEquals(1, errors.size());
        assertEquals(filename, errors.get(0).get("file"));
        assertTrue(errors.get(0).get("error").toString().contains("Exception"));
    }

    @Test
    @Transactional
    public void testFilterEventConf_ShouldReturnFilteredResults() throws Exception {
        // Step 1: Seed DB with events from known XMLs
        String[] filenames = {"opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }
        Response uploadResp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), uploadResp.getStatus());

        // Step 2: Call the filter API
        Response resp = eventConfRestApi.filterEventConf(null, "Cisco", null, 0, 10, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        List<EventConfEventDto> results = (List<EventConfEventDto>) resp.getEntity();

        // Step 3: Assertions
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream()
                .allMatch(e -> "Cisco".equals(e.getVendor())));
    }

    @Test
    @Transactional
    public void testFilterEventConf_NoFilters_ShouldReturnAllResults() {
        // Call without any filters
        Response resp = eventConfRestApi.filterEventConf(null, null, null,0, 10,  securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }


    @Test
    public void testEventConfSourcesEnabledDisabled() throws Exception {
        String[] filenames = {"eventconf.xml", "opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }
        eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        List<Long> sourcesIds = eventConfSourceDao.findAll().stream().map(EventConfSource::getId).toList();
        // Disable eventConfSources and eventConfEvents.
        EventConfSrcEnableDisablePayload eventConfSrcDisablePayload = new EventConfSrcEnableDisablePayload(false, true, sourcesIds);
        eventConfRestApi.enableDisableEventConfSources(eventConfSrcDisablePayload, securityContext);
        List<EventConfSource> eventConfSources = eventConfSourceDao.findAll();
        assertTrue(eventConfSources.stream().noneMatch(EventConfSource::getEnabled));
        List<EventConfEvent> eventConfEvents = eventConfEventDao.findAll();
        assertTrue(eventConfEvents.stream().noneMatch(EventConfEvent::getEnabled));

        // Enable eventConfSources and eventConfEvents.
        EventConfSrcEnableDisablePayload eventConfSrcEnablePayload = new EventConfSrcEnableDisablePayload(true, true, sourcesIds);
        eventConfRestApi.enableDisableEventConfSources(eventConfSrcEnablePayload, securityContext);
        List<EventConfSource> enableEventConfSources = eventConfSourceDao.findAll();
        assertFalse(enableEventConfSources.stream().noneMatch(EventConfSource::getEnabled));
        List<EventConfEvent> enableEventConfEvents = eventConfEventDao.findAll();
        assertFalse(enableEventConfEvents.stream().noneMatch(EventConfEvent::getEnabled));
    }


    @Test
    @Transactional
    public void testEnableDisableEventConfSourcesEvents() throws Exception {
        EventConfSource  m_source = new EventConfSource();
        m_source.setName("testEventEnabledFlagTest");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(1);
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor1");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(2);
        m_source.setLastModified(new Date());

        eventConfSourceDao.saveOrUpdate(m_source);
        eventConfSourceDao.flush();

        insertEvent(m_source,"uei.opennms.org/internal/trigger", "Trigger configuration changed testing", "The Trigger configuration has been changed and should be reloaded", "Normal");

        insertEvent(m_source,"uei.opennms.org/internal/clear", "Clear discovery failed testing", "The Clear discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.", "Minor");

        EventConfSource source = eventConfSourceDao.findByName("testEventEnabledFlagTest");

        EventConfEvent triggerEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/trigger");
        EventConfEvent clearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");

        // Disable events
        EnableDisableConfSourceEventsPayload disablePayload = new EnableDisableConfSourceEventsPayload();
        disablePayload.setEventsIds(List.of(triggerEvent.getId(), clearEvent.getId()));
        disablePayload.setEnable(false);

        eventConfRestApi.enableDisableEventConfSourcesEvents(source.getId(), disablePayload, securityContext);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        // Verify disabled state
        EventConfEvent disabledTriggerEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/trigger");
        EventConfEvent disabledClearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");

        assertFalse(disabledTriggerEvent.getEnabled());
        assertFalse(disabledClearEvent.getEnabled());

        // Enable events
        EnableDisableConfSourceEventsPayload enablePayload = new EnableDisableConfSourceEventsPayload();
        enablePayload.setEventsIds(List.of(triggerEvent.getId(), clearEvent.getId()));
        enablePayload.setEnable(true);

        eventConfRestApi.enableDisableEventConfSourcesEvents(source.getId(), enablePayload, securityContext);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        // Verify enabled state
        EventConfEvent enabledTriggerEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/trigger");
        EventConfEvent enabledClearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");

        assertTrue(enabledTriggerEvent.getEnabled());
        assertTrue(enabledClearEvent.getEnabled());
    }

    @Test
    public void testEnableDisableEventConfSources_InvalidPayload() throws Exception {
        EventConfSrcEnableDisablePayload payload = new EventConfSrcEnableDisablePayload(null, null, Collections.emptyList());
        Response response = eventConfRestApi.enableDisableEventConfSources(payload, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("enabled"));
    }

    @Test
    @Transactional
    public void testFilterEventConfEventBySourceId_ShouldReturnBADRequest() {
        // Invalid Source Id
        Response resp = eventConfRestApi.filterConfEventsBySourceId(-1L, "","","",0, 0, 10, securityContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        // Invalid offset
        resp = eventConfRestApi.filterConfEventsBySourceId(1L, "","","",0, -1, 10, securityContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        // Invalid limit
        resp = eventConfRestApi.filterConfEventsBySourceId(1L, "","","",0, 0, 0, securityContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    @Transactional
    public void testFilterEventConfEventBySourceId_ShouldReturnNoContent() {
        // Source Id not exits
        Response resp = eventConfRestApi.filterConfEventsBySourceId(15200L, "","","",0, 0, 10, securityContext);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    @Transactional
    public void testFilterEventConfEventBySourceId_ShouldReturnOkResponse() throws Exception {
        // Step 1: Seed DB with events from known XMLs
        String[] filenames = {"opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }

        Response uploadResp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), uploadResp.getStatus());

        List<EventConfSource> eventConfSourceList = eventConfSourceDao.findAll();

        EventConfSource eventConfSource = eventConfSourceDao.findByName("Cisco.airespace.xml");
        assertNotNull("Event Source not found against name Cisco.airespace ", eventConfSource);

        // Valid Source Id
        Response resp = eventConfRestApi.filterConfEventsBySourceId(eventConfSource.getId(), "","","",0, 0, 10, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }


    @Test
    public void testDeleteEventConfSources_Success() throws Exception {
        String[] filenames = {"eventconf.xml", "opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }
        eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        List<Long> sourcesIds = eventConfSourceDao.findAll().stream().map(EventConfSource::getId).toList();
        // Delete eventConfSources.
        EventConfSourceDeletePayload payload = new EventConfSourceDeletePayload();
        payload.setSourceIds(sourcesIds);

        eventConfRestApi.deleteEventConfSources(payload, securityContext);
        List<EventConfSource> eventConfSources = eventConfSourceDao.findAll();
        assertEquals(0, eventConfSources.size());

    }

    @Test
    public void testDeleteEventConfSources_EmptySourceIds() throws Exception {
        EventConfSourceDeletePayload payload = new EventConfSourceDeletePayload();
        payload.setSourceIds(Collections.emptyList());

        Response response = eventConfRestApi.deleteEventConfSources(payload, securityContext);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("At least one sourceId"));

    }
    private void insertEvent(EventConfSource m_source,String uei, String label, String description, String severity) {
        EventConfEvent event = new EventConfEvent();
        event.setUei(uei);
        event.setEventLabel(label);
        event.setDescription(description);
        event.setXmlContent("<event><uei>" + uei + "</uei></event>");
        event.setSource(m_source);
        event.setEnabled(true);
        event.setCreatedTime(new Date());
        event.setLastModified(new Date());
        event.setModifiedBy("JUnitTest");

        eventConfEventDao.saveOrUpdate(event);
    }
    @Test
    @Transactional
    public void testGetEventConfSourcesNames() throws Exception {
        String[] filenames = {"opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);

            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);

            attachments.add(att);
        }

        Response uploadResp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), uploadResp.getStatus());

        Response resp = eventConfRestApi.getEventConfSourcesNames(securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        List<String> sourceNames = (List<String>) resp.getEntity();

        assertNotNull(sourceNames);
        assertFalse(sourceNames.isEmpty());
        assertTrue(sourceNames.stream().anyMatch(name -> name.contains("Cisco.airespace.xml")));

        // test when no sources exists in db
        final var  eventConfSources = eventConfSourceDao.findAll();
        eventConfSourceDao.deleteAll(eventConfSources);
        Response sourceNamesResponse = eventConfRestApi.getEventConfSourcesNames(securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), sourceNamesResponse.getStatus());
        @SuppressWarnings("unchecked")
        final var  eventConfEmptySourceNames = (List<String>) sourceNamesResponse.getEntity();
        assertNotNull(eventConfEmptySourceNames);
        assertTrue("Expected empty list when no EventConfSources exist", eventConfEmptySourceNames.isEmpty());
    }


    @Test
    @Transactional
    public void testAddEventConfSourceEvent_ShouldAddNewEventConfEvent() throws Exception {
        String[] filenames = {"opennms.alarm.events.xml", "Cisco.airespace.xml"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/EVENTS-CONF/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);
            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }

        Response uploadResp = eventConfRestApi.uploadEventConfFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), uploadResp.getStatus());

        EventConfSource eventConfSource = eventConfSourceDao.findByName("Cisco.airespace.xml");
        assertNotNull("Event Source not found against name Cisco.airespace ", eventConfSource);

        String xmlEvent = """
                        <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
                   <uei>uei.opennms.org/vendor/test/test1</uei>
                   <event-label>Test1:  Adding new test  event</event-label>
                   <descr>Add new test event</descr>
                   <severity>Warning</severity>
                </event>
                """;

        Event event = JaxbUtils.unmarshal(Event.class, xmlEvent);

        Response resp = eventConfRestApi.addEventConfSourceEvent(eventConfSource.getId(), event, securityContext);
        assertEquals(Response.Status.CREATED.getStatusCode(), resp.getStatus());
    }
}
