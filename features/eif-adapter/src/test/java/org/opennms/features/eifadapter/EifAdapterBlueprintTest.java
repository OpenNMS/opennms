/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.eifadapter;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.collect.Lists;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.xml.event.Event;

public class EifAdapterBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected EventIpcManager eventIpcManager;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-eif-adapter.xml";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        MockEventIpcManager mockEventIpcManager = new MockEventIpcManager();
        services.put(EventIpcManager.class.getName(), asService(mockEventIpcManager, null));
    }

    @Test
    public void canParseEifPacketsAndGenerateEvents() throws Exception {
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
        FileInputStream eifPacketCapture = new FileInputStream(new File("src/test/resources/eif_packets_simple_test.dat"));
        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write( IOUtils.toByteArray(eifPacketCapture) );
        outToServer.flush();
        clientSocket.close();
        
        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 6);
        for (Event event : receivedEvents) {
            assertTrue("UEI must match regex.", event.getUei().matches("^org.opennms.eif/EIF_TEST_EVENT_TYPE_\\w$"));
            assertTrue("situation_name must match regex.",event.getParm("situation_name").getValue().getContent().
                    matches("^Situation \\d{2}"));
        }
    }

    @Test
    public void canParseEifWithSemicolonsInSlotsAndGenerateEvents() throws Exception {
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
        FileInputStream eifPacketCapture = new FileInputStream(new File("src/test/resources/eif_packets_semicolon_test.dat"));
        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write( IOUtils.toByteArray(eifPacketCapture) );
        outToServer.flush();
        clientSocket.close();

        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 6);
        for (Event event : receivedEvents) {
            assertTrue("UEI must match regex.", event.getUei().matches("^org.opennms.eif/EIF_TEST_EVENT_TYPE_\\w$"));
            assertTrue("situation_name must match regex.",event.getParm("situation_name").getValue().getContent().
                    matches("^Situation \\d{2}"));
        }
    }
}
