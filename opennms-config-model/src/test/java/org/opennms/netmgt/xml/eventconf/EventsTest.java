/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class EventsTest {

    private Events events;

    private Event mockEvent;

    private Partition mockPartition;

    private EventOrdering mockEventOrdering;

    @Before
    public void setUp() {
        events = new Events();
        mockEvent = Mockito.mock(Event.class);
        mockPartition = Mockito.mock(Partition.class);
        mockEventOrdering = Mockito.mock(EventOrdering.class);
    }

    @Test
    public void testDoesNotDuplicateEventsWithPriority() {
        when(mockPartition.group(mockEvent)).thenReturn(List.of(".1.3.6.1.2.1.10.166.3"));

        events.addEvent(mockEvent);
        when(mockEvent.getPriority()).thenReturn(1);

        events.initialize(mockPartition, mockEventOrdering);

        assertEquals(events.getEvents().size(), 1);
        assertTrue(events.getEvents().contains(mockEvent));
    }

}
