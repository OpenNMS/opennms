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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;

/**
 * Every write of event configuration stores a non-blank, trimmed UEI (NMS-20608).
 */
public class EventConfServiceHelperUeiTest {

    private static Event parse(final String ueiElementContent) {
        return parse(ueiElementContent, "L");
    }

    /** {@code label} null leaves out the event-label element. */
    private static Event parse(final String ueiElementContent, final String label) {
        return JaxbUtils.unmarshal(Event.class, "<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">"
                + "<uei>" + ueiElementContent + "</uei>"
                + (label == null ? "" : "<event-label>" + label + "</event-label>")
                + "<descr>d</descr><severity>Normal</severity></event>");
    }

    @Test
    public void saveEventRejectsBlankUei() {
        final EventConfEventDao dao = mock(EventConfEventDao.class);
        assertThrows(IllegalArgumentException.class,
                () -> EventConfServiceHelper.saveEvent(dao, new EventConfSource(), parse("   "), "u", new Date()));
        verifyNoInteractions(dao);
    }

    @Test
    public void saveEventRejectsNullUei() {
        final EventConfEventDao dao = mock(EventConfEventDao.class);
        assertThrows(IllegalArgumentException.class,
                () -> EventConfServiceHelper.saveEvent(dao, new EventConfSource(), new Event(), "u", new Date()));
        verifyNoInteractions(dao);
    }

    @Test
    public void createEntitiesRejectsBlankUeiAnywhereInList() {
        final List<Event> events = List.of(parse("uei.opennms.org/ok"), parse(" \n "));
        assertThrows(IllegalArgumentException.class,
                () -> EventConfServiceHelper.createEventConfEventEntities(new EventConfSource(), events, "u", new Date(), 1));
    }

    @Test
    public void createEntitiesNamesEveryEventWithoutUei() {
        final List<Event> events = List.of(
                parse("uei.opennms.org/ok/1"),
                parse(" \n ", "Foo Down"),
                parse("uei.opennms.org/ok/2"),
                parse("", null),
                parse("  ", "Bar Up"));
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> EventConfServiceHelper.createEventConfEventEntities(new EventConfSource(), events, "u", new Date(), 1));
        assertEquals("Events with no 'uei', by position: #2 (event-label 'Foo Down'), #4, #5 (event-label 'Bar Up')",
                e.getMessage());
    }

    @Test
    public void isBlankUeiAgreesWithEventSetUei() {
        // U+2003 and U+3000 are Unicode spaces that String.trim() keeps; U+0001 is a control character that
        // isBlank() keeps; U+00A0 is neither whitespace for Java nor removed by trim()
        for (final String uei : List.of("uei.opennms.org/x", " uei.opennms.org/x ", "", "  \t\n", "\u0001",
                "\u2003", "\u3000 ", "\u0001\u2003", "\u2003\u0001", "\u00A0", "\u2003uei.opennms.org/x")) {
            boolean setUeiAccepts;
            try {
                new Event().setUei(uei);
                setUeiAccepts = true;
            } catch (IllegalArgumentException e) {
                setUeiAccepts = false;
            }
            final String codePoints = uei.codePoints().mapToObj(c -> String.format("U+%04X", c)).toList().toString();
            assertEquals("for UEI " + codePoints, setUeiAccepts, !EventConfServiceHelper.isBlankUei(uei));
        }
    }

    @Test
    public void createEntitiesKeepsTrimmedUeiFromPaddedXml() {
        final List<EventConfEvent> entities = EventConfServiceHelper.createEventConfEventEntities(
                new EventConfSource(), List.of(parse("\n  uei.opennms.org/x\n")), "u", new Date(), 1);
        assertEquals(1, entities.size());
        assertEquals("uei.opennms.org/x", entities.get(0).getUei());
        assertTrue(entities.get(0).getXmlContent().contains("<uei>uei.opennms.org/x</uei>"));
    }
}
