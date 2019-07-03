/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.opennms.integration.api.v1.config.events.EventConfExtension;
import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.integration.api.v1.config.events.LogMessage;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.Events;

public class EventConfExtensionManagerTest {

    @Test
    public void canPrioritizeEvents() {
        EventConfDao eventConfDao = mock(EventConfDao.class);
        EventConfExtensionManager eventConfExtensionMgr = new EventConfExtensionManager(eventConfDao);

        // No events yet
        Events events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(0));

        LogMessage logMessage = mock(LogMessage.class);

        // Expose an extension
        EventConfExtension ext1 = mock(EventConfExtension.class);
        EventDefinition eventDefinitionA = mock(EventDefinition.class);
        when(eventDefinitionA.getUei()).thenReturn("uei/A");
        when(eventDefinitionA.getLabel()).thenReturn("Label A");
        when(eventDefinitionA.getPriority()).thenReturn(100);
        when(eventDefinitionA.getSeverity()).thenReturn(Severity.CRITICAL);
        when(eventDefinitionA.getLogMessage()).thenReturn(logMessage);
        when(ext1.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinitionA));
        eventConfExtensionMgr.onBind(ext1, new HashMap());

        // One event
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(1));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/A"));

        // Expose another extension
        EventConfExtension ext2 = mock(EventConfExtension.class);
        EventDefinition eventDefinitionB = mock(EventDefinition.class);
        when(eventDefinitionB.getUei()).thenReturn("uei/B");
        when(eventDefinitionB.getLabel()).thenReturn("Label B");
        when(eventDefinitionB.getPriority()).thenReturn(10);
        when(eventDefinitionB.getSeverity()).thenReturn(Severity.NORMAL);
        when(eventDefinitionB.getLogMessage()).thenReturn(logMessage);
        when(ext2.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinitionB));
        eventConfExtensionMgr.onBind(ext2, new HashMap());

        // Aggregated events
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(2));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/B"));
        assertThat(events.getEvents().get(1).getUei(), equalTo("uei/A"));

        // Now remove an extension
        eventConfExtensionMgr.onUnbind(ext1, new HashMap());

        // Back to one event
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(1));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/B"));

        // Now remove the other extension
        eventConfExtensionMgr.onUnbind(ext2, new HashMap());

        // No events
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(0));
    }

    @Test
    public void canPreserveEventNumberAndOrdering() {
        String ueiBase = "uei.opennms.org/test/VoluminousEventConfExtension/";
        EventConfDao eventConfDao = mock(EventConfDao.class);
        EventConfExtensionManager eventConfExtensionMgr = new EventConfExtensionManager(eventConfDao);

        // No events yet
        Events events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(0));

        // Make a bunch. Validate their number and ordering.
        final int NUM_EVENTS = 104;
        EventConfExtension ext3 = new VoluminousEventConfExtension(ueiBase, NUM_EVENTS);
        eventConfExtensionMgr.onBind(ext3, new HashMap<>());
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(NUM_EVENTS));
        for (int i = 0; i < NUM_EVENTS; i++) {
            assertThat(events.getEvents().get(i).getUei(), equalTo(String.format("%s%d", ueiBase, i)));
        }
    }
}
