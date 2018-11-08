/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.eventforwarder;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

/**
 * Simple test that verifies the Blueprint syntax.
 *
 * @author jwhite
 */
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
public class AMQPEventForwarderBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected ForwardingEventListener forwardingEventListener;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-event-forwarder.xml";
    }

    @Override
    protected String setConfigAdminInitialConfiguration(Properties props) {
        props.put("destination", "mock:destination");
     
        // Return the PID
        return "org.opennms.features.amqp.eventforwarder";
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
    public void canForwardEvent() throws Exception {
        getMockEndpoint("mock:destination").expectedMessageCount(1);

        // Forward a single event
        Event event = new Event();
        forwardingEventListener.onEvent(event);

        assertMockEndpointsSatisfied();
    }
}
