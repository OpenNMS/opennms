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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The REST layer rejects a blank UEI before it calls the persistence service, so a bad request never opens a
 * transaction or locks a source (NMS-20608).
 */
public class EventConfRestServiceUeiTest {

    private static final String NS = "http://xmlns.opennms.org/xsd/eventconf";

    private EventConfPersistenceService persistence;
    private EventConfRestService rest;

    @Before
    public void setUp() {
        persistence = mock(EventConfPersistenceService.class);
        rest = new EventConfRestService();
        ReflectionTestUtils.setField(rest, "eventConfPersistenceService", persistence);
    }

    private static Event parse(final String ueiElementContent) {
        return JaxbUtils.unmarshal(Event.class, "<event xmlns=\"" + NS + "\"><uei>" + ueiElementContent + "</uei>"
                + "<event-label>Label</event-label><descr>d</descr><severity>Normal</severity></event>");
    }

    private static EventConfEventEditRequest edit(final Event event) {
        final EventConfEventEditRequest req = new EventConfEventEditRequest();
        req.setEvent(event);
        req.setEnabled(true);
        return req;
    }

    @Test
    public void editWithBlankUeiIsRejectedBeforeTheService() throws Exception {
        final Response resp = rest.updateEventConfEvent(1L, 2L, edit(parse("  \n ")), null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        assertEquals(Map.of("error", "Event 'uei' is required"), resp.getEntity());
        verifyNoInteractions(persistence);
    }

    @Test
    public void editMapsOtherIllegalArgumentToServerError() throws Exception {
        doThrow(new IllegalArgumentException("from deep inside the update"))
                .when(persistence).updateEventConfEvent(eq(1L), eq(2L), any(), anyString());
        final Response resp = rest.updateEventConfEvent(1L, 2L, edit(parse("uei.opennms.org/ok")), null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void uploadWithBlankUeiIsRejectedBeforeTheTransaction() throws Exception {
        final Attachment att = mock(Attachment.class);
        final ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn("blank.events.xml");
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(new ByteArrayInputStream(("<events xmlns=\"" + NS + "\">"
                + "<event><uei> </uei><event-label>Foo Down</event-label><descr>d</descr><severity>Normal</severity></event>"
                + "</events>").getBytes(StandardCharsets.UTF_8)));

        final Response resp = rest.uploadEventConfFiles(List.of(att), null);

        verify(persistence, never()).persistEventConfFile(any(), any());
        final List<Map<String, Object>> errors = (List<Map<String, Object>>) ((Map<String, Object>) resp.getEntity()).get("errors");
        assertEquals(1, errors.size());
        assertEquals("blank.events", errors.get(0).get("file"));
        assertTrue(errors.get(0).get("error").toString().contains("#1 (event-label 'Foo Down')"));
    }
}
