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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.EventConfEventDto;
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
import java.util.List;
import java.util.Map;


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
        Response resp = eventConfRestApi.filterEventConf(null, "Cisco", null, securityContext);
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
    public void testFilterEventConf_NoFilters_ShouldReturnNoContent() {
        // Call without any filters
        Response resp = eventConfRestApi.filterEventConf(null, null, null,  securityContext);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

}
