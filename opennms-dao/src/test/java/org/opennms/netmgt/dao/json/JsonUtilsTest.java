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
package org.opennms.netmgt.dao.json;


import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Event;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class JsonUtilsTest {

    private Event sampleEvent;

    @Before
    public void setup() {
        sampleEvent = new Event();
        sampleEvent.setUei("uei.test.event");
        sampleEvent.setEventLabel("Test Event");
        sampleEvent.setDescr("Sample Description");

        Logmsg logmsg = new Logmsg();
        logmsg.setContent("Log message here");
        logmsg.setDest(LogDestType.LOGNDISPLAY);
        sampleEvent.setLogmsg(logmsg);
        sampleEvent.setSeverity("NORMAL");
    }

    @Test
    public void testMarshalToString() {
        String json = JsonUtils.marshal(sampleEvent);

        assertNotNull(json);
        assertTrue(json.contains("uei.test.event"));
        assertTrue(json.contains("Test Event"));
    }

    @Test
    public void testMarshalToWriter() throws Exception {
        StringWriter writer = new StringWriter();
        JsonUtils.marshal(sampleEvent, writer);

        String json = writer.toString();
        assertNotNull(json);
        assertTrue(json.contains("Sample Description"));
    }

    @Test
    public void testMarshalToFile() throws Exception {
        File temp = File.createTempFile("event", ".json");
        JsonUtils.marshal(sampleEvent, temp);

        assertTrue(temp.length() > 0);

        String fileContent = new String(java.nio.file.Files.readAllBytes(temp.toPath()));
        assertTrue(fileContent.contains("uei.test.event"));
    }

    @Test
    public void testUnmarshalFromString() {
        String json = JsonUtils.marshal(sampleEvent);

        Event e = JsonUtils.unmarshal(Event.class, json);
        assertEquals("uei.test.event", e.getUei());
        assertEquals("Test Event", e.getEventLabel());
        assertEquals("NORMAL", e.getSeverity());
    }

    @Test
    public void testUnmarshalFromReader() throws Exception {
        String json = JsonUtils.marshal(sampleEvent);

        Event e = JsonUtils.unmarshal(Event.class, new StringReader(json));
        assertEquals("Sample Description", e.getDescr());
    }

    @Test
    public void testUnmarshalFromInputStream() throws Exception {
        String json = JsonUtils.marshal(sampleEvent);

        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        Event e = JsonUtils.unmarshal(Event.class, is);

        assertEquals("uei.test.event", e.getUei());
    }

    @Test
    public void testUnmarshalFromFile() throws Exception {
        File temp = File.createTempFile("event", ".json");
        JsonUtils.marshal(sampleEvent, temp);

        Event e = JsonUtils.unmarshal(Event.class, temp);

        assertEquals("Test Event", e.getEventLabel());
    }

    @Test
    public void testDuplicateObject() {
        Event copy = JsonUtils.duplicateObject(sampleEvent, Event.class);

        assertNotNull(copy);
        assertEquals(sampleEvent.getUei(), copy.getUei());
        assertEquals(sampleEvent.getEventLabel(), copy.getEventLabel());
        assertNotSame(sampleEvent, copy);    // Deep copy
    }

    @Test
    public void testUnmarshalInvalidJsonThrowsRuntime() {
        String invalidJson = "{ this is wrong json }";

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                JsonUtils.unmarshal(Event.class, invalidJson)
        );

        assertTrue(ex.getMessage().contains("JSON unmarshalling Event failed"));
    }
}