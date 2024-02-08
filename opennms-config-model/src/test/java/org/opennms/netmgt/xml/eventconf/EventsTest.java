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
package org.opennms.netmgt.xml.eventconf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

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
        when(mockPartition.group(mockEvent)).thenReturn(Collections.unmodifiableList(Arrays.asList(".1.3.6.1.2.1.10.166.3")));

        events.addEvent(mockEvent);
        when(mockEvent.getPriority()).thenReturn(1);

        events.initialize(mockPartition, mockEventOrdering);

        assertEquals(events.getEvents().size(), 1);
        assertTrue(events.getEvents().contains(mockEvent));
    }

}
