/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
