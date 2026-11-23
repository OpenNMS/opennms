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

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Test;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;

/**
 * JSON request bodies go through the CXF Jackson 1 JAXB provider (applicationContext-cxf-common.xml), which
 * the ITs bypass by passing Event objects directly. It must apply the eventconf package's StringTrimAdapter
 * too (NMS-20608).
 */
public class EventConfJsonUeiTrimTest {

    private static final String EVENT_JSON =
            "{\"uei\":\"\\n  uei.opennms.org/x \\n\",\"event-label\":\"L\",\"descr\":\"d\",\"severity\":\"Normal\"}";

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> T read(final Class<T> type, final String json) throws Exception {
        return (T) new JacksonJaxbJsonProvider().readFrom((Class) type, type, new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE, null, new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void jsonEventUeiIsTrimmed() throws Exception {
        assertEquals("uei.opennms.org/x", read(Event.class, EVENT_JSON).getUei());
    }

    @Test
    public void jsonEditRequestEventUeiIsTrimmed() throws Exception {
        final EventConfEventEditRequest req = read(EventConfEventEditRequest.class,
                "{\"enabled\":true,\"event\":" + EVENT_JSON + "}");
        assertEquals("uei.opennms.org/x", req.getEvent().getUei());
    }
}
