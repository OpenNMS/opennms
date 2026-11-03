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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;

/** Parallel unmarshalling must not change what loads, or in what order. */
public class EventConfParallelLoadTest {

    private static final String XML =
            "<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">"
            + "<uei>%s</uei><event-label>%s</event-label>"
            + "<descr>a description</descr>"
            + "<logmsg dest=\"logndisplay\">a log message</logmsg>"
            + "<severity>Warning</severity></event>";

    private EventConfSource source(final String name, final int fileOrder) {
        final EventConfSource source = new EventConfSource();
        source.setName(name);
        source.setFileOrder(fileOrder);
        return source;
    }

    private EventConfEvent event(final EventConfSource source, final String uei) {
        final EventConfEvent event = new EventConfEvent();
        event.setSource(source);
        event.setUei(uei);
        event.setXmlContent(String.format(XML, uei, uei));
        return event;
    }

    @Test
    public void loadsEveryDefinitionItWasGiven() {
        final EventConfSource source = source("test.events", 1);
        final List<EventConfEvent> events = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            events.add(event(source, "uei.opennms.org/test/event" + i));
        }

        final DefaultEventConfDao dao = new DefaultEventConfDao();
        dao.loadEventsFromDB(events, List.of());

        assertEquals(500, dao.getEventUEIs().size());
        for (int i = 0; i < 500; i++) {
            assertNotNull(dao.getEvents("uei.opennms.org/test/event" + i));
        }
    }

    /** Order decides which definition wins a match. */
    @Test
    public void keepsTheOrderTheQueryReturned() {
        final EventConfSource source = source("test.events", 1);
        final List<EventConfEvent> events = new ArrayList<>();
        final List<String> expected = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            final String uei = "uei.opennms.org/test/ordered" + i;
            events.add(event(source, uei));
            expected.add(uei);
        }

        final DefaultEventConfDao dao = new DefaultEventConfDao();
        dao.loadEventsFromDB(events, List.of());

        final List<String> loaded = new ArrayList<>();
        for (final org.opennms.netmgt.xml.eventconf.Event e : dao.getAllEvents()) {
            loaded.add(e.getUei());
        }
        assertEquals(expected, loaded);
    }

    @Test
    public void skipsWhatItCannotParseAndKeepsTheRest() {
        final EventConfSource source = source("test.events", 1);
        final EventConfEvent broken = event(source, "uei.opennms.org/test/broken");
        broken.setXmlContent("<event><this is not xml");
        final EventConfEvent empty = event(source, "uei.opennms.org/test/empty");
        empty.setXmlContent("   ");

        final DefaultEventConfDao dao = new DefaultEventConfDao();
        dao.loadEventsFromDB(
                List.of(event(source, "uei.opennms.org/test/good"), broken, empty), List.of());

        assertNotNull(dao.getEvents("uei.opennms.org/test/good"));
        assertNull(dao.getEvents("uei.opennms.org/test/broken"));
        assertEquals(1, dao.getEventUEIs().size());
    }
}
