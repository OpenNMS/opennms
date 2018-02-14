/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.eventreceiver;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.collect.Lists;

/**
 * Simple test that verifies the Blueprint syntax.
 *
 * NOTE: These tests do not run reliably and tend to fail fairly
 * often so they are disabled by default.
 *
 * @author jwhite
 */
@Ignore
public class AMQPEventReceiverBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected EventIpcManager eventIpcManager;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-event-receiver.xml";
    }

    @Override
    protected String setConfigAdminInitialConfiguration(Properties props) {
        props.put("source", "direct:source");
     
        // Return the PID
        return "org.opennms.features.amqp.eventreceiver";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        MockEventIpcManager mockEventIpcManager = new MockEventIpcManager();
        services.put(EventIpcManager.class.getName(), asService(mockEventIpcManager, null));

        MockNodeDao mockNodeDao = new MockNodeDao();
        services.put(NodeDao.class.getName(), asService(mockNodeDao, null));
    }

    @Test
    public void canReceiveEvent() throws Exception {
        // Register an event listener
        final List<Event> receivedEvents = Lists.newArrayList();
        eventIpcManager.addEventListener(new EventListener() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void onEvent(Event e) {
                receivedEvents.add(e);
            }
        });

        // Send a event, simulating an event on the source "queue"
        Event event = new Event();
        template.sendBody("direct:source", event);

        // Wait until the event is received by our listener
        assertMockEndpointsSatisfied();
        for (int i = 0; i < 60; i+=5) {
            if (receivedEvents.size() != 0) {
                break;
            }
            Thread.sleep(5);
        }
        assertEquals(1, receivedEvents.size());
    }
}
