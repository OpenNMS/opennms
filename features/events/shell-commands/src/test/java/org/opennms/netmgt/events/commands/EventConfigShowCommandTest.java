/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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
