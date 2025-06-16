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
package org.opennms.features.eifadapter;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.collect.Lists;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class EifAdapterBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected EventIpcManager eventIpcManager;

    @Autowired
    protected NodeDao nodeDao;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-eif-adapter.xml";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        MockEventIpcManager mockEventIpcManager = new MockEventIpcManager();
        MockNodeDao mockNodeDao = new MockNodeDao();
        services.put(EventIpcManager.class.getName(), asService(mockEventIpcManager, null));
        services.put(NodeDao.class.getName(), asService(mockNodeDao, null));
    }

    @Test
    public void testCanParseEifPacketsAndGenerateEvents() throws Exception {
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
        FileInputStream eifPacketCapture = new FileInputStream(new File("src/test/resources/eif_packets_simple_test.dat"));
        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write( IOUtils.toByteArray(eifPacketCapture) );
        outToServer.flush();
        clientSocket.close();
        
        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 6);
        for (IEvent event : receivedEvents) {
            assertTrue("UEI must match regex.", event.getUei().matches("^uei.opennms.org/vendor/IBM/EIF/EIF_TEST_EVENT_TYPE_\\w$"));
            assertTrue("situation_name must match regex.",event.getParm("situation_name").getValue().getContent().
                    matches("^Situation \\d{2}"));
        }
    }

    @Test
    public void testCanParseEifWithSemicolonsInSlotsAndGenerateEvents() throws Exception {
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
        FileInputStream eifPacketCapture = new FileInputStream(new File("src/test/resources/eif_packets_semicolon_test.dat"));
        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write( IOUtils.toByteArray(eifPacketCapture) );
        outToServer.flush();
        clientSocket.close();

        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 6);
        for (IEvent event : receivedEvents) {
            assertTrue("UEI must match regex.", event.getUei().matches("^uei.opennms.org/vendor/IBM/EIF/EIF_TEST_EVENT_TYPE_\\w$"));
            assertTrue("situation_name must match regex.",event.getParm("situation_name").getValue().getContent().
                    matches("^Situation \\d{2}"));
        }
    }

   @Test
    public void testCanParseEifWith36ByteOffset() throws Exception {
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
        BufferedReader eifPacketCapture = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(new File("src/test/resources/eif_36_byte_offset_test.dat")), StandardCharsets.UTF_8
                )
        );
        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write( IOUtils.toByteArray(eifPacketCapture) );
        outToServer.flush();
        clientSocket.close();

        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 1);
        for (IEvent event : receivedEvents) {
            assertTrue("UEI must match regex.", event.getUei().matches("^uei.opennms.org/vendor/IBM/EIF/EIF_TEST_EVENT_TYPE_G$"));
        }
    }
}
