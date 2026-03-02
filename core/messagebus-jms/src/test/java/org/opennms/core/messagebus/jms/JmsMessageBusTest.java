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
package org.opennms.core.messagebus.jms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageHandler;
import org.opennms.core.test.activemq.ActiveMQBroker;

public class JmsMessageBusTest {

    @Rule
    public ActiveMQBroker broker = new ActiveMQBroker("vm://localhost?broker.persistent=false");

    private JmsMessageBus messageBus;

    @Before
    public void setUp() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
        messageBus = new JmsMessageBus(factory);
        messageBus.start();
    }

    @After
    public void tearDown() {
        messageBus.stop();
    }

    @Test
    public void shouldPublishAndReceiveMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IpcMessage> received = new AtomicReference<>();

        messageBus.subscribe("reloadDaemonConfig", new MessageHandler() {
            @Override
            public String getName() { return "test-handler"; }

            @Override
            public void onMessage(IpcMessage message) {
                received.set(message);
                latch.countDown();
            }
        });

        // Small delay to ensure JMS subscription is active
        Thread.sleep(200);

        IpcMessage outgoing = new IpcMessage("reloadDaemonConfig", "webui",
                Map.of("daemonName", "pollerd"));
        messageBus.publish(outgoing);

        boolean dispatched = latch.await(5, TimeUnit.SECONDS);
        assertThat(dispatched).isTrue();
        assertThat(received.get()).isNotNull();
        assertThat(received.get().getType()).isEqualTo("reloadDaemonConfig");
        assertThat(received.get().getSource()).isEqualTo("webui");
        assertThat(received.get().getParameter("daemonName")).isEqualTo("pollerd");
    }

    @Test
    public void shouldNotReceiveMessagesForOtherTypes() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        messageBus.subscribe("typeA", new MessageHandler() {
            @Override
            public String getName() { return "handlerA"; }

            @Override
            public void onMessage(IpcMessage message) {
                latch.countDown();
            }
        });

        Thread.sleep(200);

        // Publish to a different type
        messageBus.publish(new IpcMessage("typeB", "source"));

        boolean dispatched = latch.await(1, TimeUnit.SECONDS);
        assertThat(dispatched).isFalse();
    }

    @Test
    public void shouldUnsubscribeHandler() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        MessageHandler handler = new MessageHandler() {
            @Override
            public String getName() { return "temp-handler"; }

            @Override
            public void onMessage(IpcMessage message) {
                latch.countDown();
            }
        };

        messageBus.subscribe("testType", handler);
        Thread.sleep(200);
        messageBus.unsubscribe(handler);
        Thread.sleep(200);

        messageBus.publish(new IpcMessage("testType", "source"));

        boolean dispatched = latch.await(1, TimeUnit.SECONDS);
        assertThat(dispatched).isFalse();
    }

    @Test
    public void shouldPreserveNodeIdAndInterfaceAddress() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IpcMessage> received = new AtomicReference<>();

        messageBus.subscribe("nodeEvent", new MessageHandler() {
            @Override
            public String getName() { return "node-handler"; }

            @Override
            public void onMessage(IpcMessage message) {
                received.set(message);
                latch.countDown();
            }
        });

        Thread.sleep(200);

        IpcMessage outgoing = new IpcMessage("nodeEvent", "pollerd",
                System.currentTimeMillis(), 42L, "192.168.1.1", Map.of());
        messageBus.publish(outgoing);

        boolean dispatched = latch.await(5, TimeUnit.SECONDS);
        assertThat(dispatched).isTrue();
        assertThat(received.get().getNodeId()).isEqualTo(42L);
        assertThat(received.get().getInterfaceAddress()).isEqualTo("192.168.1.1");
    }
}
