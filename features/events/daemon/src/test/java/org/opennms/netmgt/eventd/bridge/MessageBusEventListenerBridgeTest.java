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
package org.opennms.netmgt.eventd.bridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.local.LocalMessageBus;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;

public class MessageBusEventListenerBridgeTest {

    private LocalMessageBus messageBus;
    private MessageBusEventListenerBridge bridge;

    @Before
    public void setUp() {
        messageBus = new LocalMessageBus();
        bridge = new MessageBusEventListenerBridge(messageBus);
    }

    @Test
    public void shouldDeliverInternalEventToAnnotatedHandler() {
        TestListener listener = new TestListener();
        bridge.register(listener);

        // "poller/suspendPollingService" maps to "uei.opennms.org/internal/poller/suspendPollingService"
        messageBus.publish(new IpcMessage("poller/suspendPollingService", "test-source",
                System.currentTimeMillis(), 42L, "192.168.1.1",
                Map.of("reason", "manual")));

        assertThat(listener.receivedEvents).hasSize(1);
        IEvent event = listener.receivedEvents.get(0);
        assertThat(event.getUei()).isEqualTo("uei.opennms.org/internal/poller/suspendPollingService");
        assertThat(event.getSource()).isEqualTo("test-source");
        assertThat(event.getNodeid()).isEqualTo(42L);
        assertThat(event.getInterface()).isEqualTo("192.168.1.1");
        assertThat(event.getParm("reason")).isNotNull();
        assertThat(event.getParm("reason").getValue().getContent()).isEqualTo("manual");
    }

    @Test
    public void shouldDeliverToMultipleUeiHandler() {
        MultiUeiListener listener = new MultiUeiListener();
        bridge.register(listener);

        messageBus.publish(new IpcMessage("applicationChanged", "test-source"));
        messageBus.publish(new IpcMessage("applicationCreated", "test-source"));

        assertThat(listener.receivedEvents).hasSize(2);
        assertThat(listener.receivedEvents.get(0).getUei())
                .isEqualTo("uei.opennms.org/internal/applicationChanged");
        assertThat(listener.receivedEvents.get(1).getUei())
                .isEqualTo("uei.opennms.org/internal/applicationCreated");
    }

    @Test
    public void shouldIgnoreNonInternalUeis() {
        MixedUeiListener listener = new MixedUeiListener();
        bridge.register(listener);

        // Only the internal UEI should be subscribed on MessageBus
        // Publishing a message type matching the non-internal UEI shouldn't cause issues
        messageBus.publish(new IpcMessage("poller/resumePollingService", "test-source"));

        assertThat(listener.receivedEvents).hasSize(1);
        assertThat(listener.receivedEvents.get(0).getUei())
                .isEqualTo("uei.opennms.org/internal/poller/resumePollingService");
    }

    @Test
    public void shouldHandleNullNodeIdAndInterface() {
        TestListener listener = new TestListener();
        bridge.register(listener);

        messageBus.publish(new IpcMessage("poller/suspendPollingService", "test-source",
                System.currentTimeMillis(), null, null, Map.of()));

        assertThat(listener.receivedEvents).hasSize(1);
        IEvent event = listener.receivedEvents.get(0);
        assertThat(event.hasNodeid()).isFalse();
        assertThat(event.getInterface()).isNull();
    }

    @Test
    public void shouldRequireEventListenerAnnotation() {
        assertThatThrownBy(() -> bridge.register(new UnannotatedListener()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@EventListener");
    }

    @Test
    public void shouldUnregisterCleanly() {
        TestListener listener = new TestListener();
        bridge.register(listener);
        bridge.unregister(listener);

        messageBus.publish(new IpcMessage("poller/suspendPollingService", "test-source"));

        assertThat(listener.receivedEvents).isEmpty();
    }

    // --- Test fixtures ---

    @EventListener(name = "TestListener")
    public static class TestListener {
        final List<IEvent> receivedEvents = new ArrayList<>();

        @EventHandler(uei = "uei.opennms.org/internal/poller/suspendPollingService")
        public void handleEvent(IEvent event) {
            receivedEvents.add(event);
        }
    }

    @EventListener(name = "MultiUeiListener")
    public static class MultiUeiListener {
        final List<IEvent> receivedEvents = new ArrayList<>();

        @EventHandler(ueis = {
                "uei.opennms.org/internal/applicationChanged",
                "uei.opennms.org/internal/applicationCreated",
                "uei.opennms.org/internal/applicationDeleted"
        })
        public void handleEvent(IEvent event) {
            receivedEvents.add(event);
        }
    }

    @EventListener(name = "MixedUeiListener")
    public static class MixedUeiListener {
        final List<IEvent> receivedEvents = new ArrayList<>();

        @EventHandler(ueis = {
                "uei.opennms.org/nodes/nodeGainedService",
                "uei.opennms.org/internal/poller/resumePollingService"
        })
        public void handleEvent(IEvent event) {
            receivedEvents.add(event);
        }
    }

    public static class UnannotatedListener {
        @EventHandler(uei = "uei.opennms.org/internal/something")
        public void handleEvent(IEvent event) {
        }
    }
}
