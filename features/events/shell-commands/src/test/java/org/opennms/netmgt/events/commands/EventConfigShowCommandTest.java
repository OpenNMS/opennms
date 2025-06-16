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
package org.opennms.netmgt.events.commands;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.config.api.EventConfDao;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;

public class EventConfigShowCommandTest {
    private EventConfDao mockEventConfDao;
    private EventConfigShowCommand eventConfigShowCommand;

    @Before
    public void setUp() {
        mockEventConfDao = Mockito.mock(EventConfDao.class);
        eventConfigShowCommand = new EventConfigShowCommand() {
            {
                eventConfDao = mockEventConfDao;
            }
        };
    }

    @Test
    public void testFindsUEISubstrings() {
        String subUEI = "foo.bar";
        String uei1 = "foo.bar.one";
        String uei2 = "foo.bar.two";

        List<String> ueis = Arrays.asList(uei1, uei2);
        Mockito.when(mockEventConfDao.getEventUEIs()).thenReturn(ueis);

        eventConfigShowCommand.eventUeiMatch = subUEI;

        eventConfigShowCommand.execute();

        Mockito.verify(mockEventConfDao).getEvents(uei1);
        Mockito.verify(mockEventConfDao).getEvents(uei2);
    }

    @Test
    public void testDuplicateUEIsCallsDaoOnce() {
        String ueiValue = "testUei";

        List<String> ueis = Arrays.asList(ueiValue, ueiValue);
        Mockito.when(mockEventConfDao.getEventUEIs()).thenReturn(ueis);

        eventConfigShowCommand.eventUeiMatch = ueiValue;

        eventConfigShowCommand.execute();

        Mockito.verify(mockEventConfDao, times(1)).getEvents(ueiValue);
    }
}
