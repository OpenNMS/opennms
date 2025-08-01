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
package org.opennms.netmgt.config.priority;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Events;

public class PriorityTest {

    Events events = new Events();
    EventOrdering eo = new EventOrdering();

    @Test
    public void naturalOrderSortIsPriority() {
        Event event1 = getEvent("uei1", 10);
        Event event2 = getEvent("uei2", 100);
        Event event3 = getEvent("uei3", 50);
        Event event4 = getEvent("uei4", 200);
        List<Event> defs = Arrays.asList(event1, event2, event3, event4);
        events.setEvents(defs);

        assertThat(events.getEvents().get(0).getUei(), is("uei1"));
        assertThat(events.getEvents().get(1).getUei(), is("uei2"));
        Collections.sort(events.getEvents());
        assertThat(events.getEvents().get(0).getUei(), is("uei4"));
        assertThat(events.getEvents().get(1).getUei(), is("uei2"));
        assertThat(events.getEvents().get(2).getUei(), is("uei3"));
        assertThat(events.getEvents().get(3).getUei(), is("uei1"));
    }

    @Test
    public void canConsiderMatch() {
        // All same priority, will be sorted in order they are created becuase
        // they are indexed with eo.next()
        Event event1 = getEvent("uei1", 100);
        Event event2 = getEvent("uei2", 100);
        Event event3 = getEvent("uei3", 100);
        Event event4 = getEvent("uei4", 100);
        List<Event> defs = Arrays.asList(event1, event2, event3, event4);
        events.setEvents(defs);

        Collections.sort(events.getEvents());
        assertThat(events.getEvents().get(0).getUei(), is("uei1"));
        assertThat(events.getEvents().get(1).getUei(), is("uei2"));
        assertThat(events.getEvents().get(2).getUei(), is("uei3"));
        assertThat(events.getEvents().get(3).getUei(), is("uei4"));
    }

    private Event getEvent(String uei, int priority) {
        Event e = new Event();
        e.setUei(uei);
        e.setPriority(priority);
        e.setIndex(eo.next());
        return e;
    }

}
