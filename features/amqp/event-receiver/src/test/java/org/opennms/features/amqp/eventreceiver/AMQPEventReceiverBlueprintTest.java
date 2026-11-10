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
package org.opennms.features.amqp.eventreceiver;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

/**
 * Simple test that verifies the Blueprint syntax.
 *
 * @author jwhite
 */
@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
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
        final List<IEvent> receivedEvents = Lists.newArrayList();
        eventIpcManager.addEventListener(new EventListener() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void onEvent(IEvent e) {
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
