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
package org.opennms.netmgt.eventd.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

public class HibernateEventWriterSanityTest {

    private Event event(final String uei, final String dest) {
        final Event event = new Event();
        event.setUei(uei);
        if (dest != null) {
            final Logmsg logmsg = new Logmsg();
            logmsg.setDest(dest);
            event.setLogmsg(logmsg);
        }
        return event;
    }

    @Test
    public void persistsAnEventThatMatchedADefinition() {
        assertTrue(HibernateEventWriter.checkEventSanityAndDoWeProcess(
                event("uei.opennms.org/nodes/nodeLostService", "logndisplay"), "test"));
    }

    @Test
    public void skipsAnEventThatMatchedNothing() {
        assertFalse(HibernateEventWriter.checkEventSanityAndDoWeProcess(
                event("uei.opennms.org/nodes/nodeLostService", null), "test"));
    }

    @Test
    public void skipsWhatTheDefinitionAsksNotToPersist() {
        assertFalse(HibernateEventWriter.checkEventSanityAndDoWeProcess(
                event("uei.opennms.org/internal/capsd/updateServer", "donotpersist"), "test"));
        assertFalse(HibernateEventWriter.checkEventSanityAndDoWeProcess(
                event("uei.opennms.org/internal/capsd/updateServer", "suppress"), "test"));
    }

    /** The reported symptom: one unmatched event took the whole log with it. */
    @Test
    public void oneUnmatchedEventDoesNotDiscardTheRestOfTheLog() {
        final List<Event> log = List.of(
                event("uei.opennms.org/nodes/dataCollectionSucceeded", "logndisplay"),
                event("uei.opennms.org/nodes/nodeLostService", null),
                event("uei.opennms.org/nodes/nodeRegainedService", "logndisplay"));

        final List<Event> toPersist = log.stream()
                .filter(e -> HibernateEventWriter.checkEventSanityAndDoWeProcess(e, "test"))
                .collect(Collectors.toList());

        assertEquals(2, toPersist.size());
        assertEquals("uei.opennms.org/nodes/dataCollectionSucceeded", toPersist.get(0).getUei());
        assertEquals("uei.opennms.org/nodes/nodeRegainedService", toPersist.get(1).getUei());
    }
}
