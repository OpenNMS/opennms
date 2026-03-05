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
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.junit.Test;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.model.IEvent;

/**
 * TDD tests for {@link KafkaAnnotationEventListenerAdapter}.
 *
 * Verifies that the adapter correctly discovers {@link EventHandler}-annotated
 * methods and registers a delegate {@link EventListener} with the subscription
 * service for the declared UEIs.
 */
public class KafkaAnnotationEventListenerAdapterTest {

    private static final String NODE_DOWN_UEI = "uei.opennms.org/nodes/nodeDown";
    private static final String NODE_UP_UEI = "uei.opennms.org/nodes/nodeUp";

    // -------- annotated test fixtures --------

    @org.opennms.netmgt.events.api.annotations.EventListener(name = "TestDaemon", logPrefix = "test")
    public static class AnnotatedDaemon {
        @EventHandler(uei = NODE_DOWN_UEI)
        public void handleNodeDown(IEvent event) {
        }

        @EventHandler(uei = NODE_UP_UEI)
        public void handleNodeUp(IEvent event) {
        }
    }

    @org.opennms.netmgt.events.api.annotations.EventListener(name = "SingleHandler")
    public static class SingleHandlerDaemon {
        @EventHandler(uei = NODE_DOWN_UEI)
        public void handleNodeDown(IEvent event) {
        }
    }

    // No @EventListener annotation on this class
    public static class NotAnnotatedDaemon {
        @EventHandler(uei = NODE_DOWN_UEI)
        public void handleNodeDown(IEvent event) {
        }
    }

    @org.opennms.netmgt.events.api.annotations.EventListener(name = "MultiUeiHandler")
    public static class MultiUeiDaemon {
        @EventHandler(ueis = {NODE_DOWN_UEI, NODE_UP_UEI})
        public void handleBoth(IEvent event) {
        }
    }

    // -------- tests --------

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRegisterListenerForAnnotatedUeis() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        AnnotatedDaemon daemon = new AnnotatedDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        // Verify addEventListener was called with a listener and a collection containing both UEIs
        verify(mockService).addEventListener(
                any(EventListener.class),
                argThat((Collection<String> ueis) ->
                        ueis.size() == 2
                                && ueis.contains(NODE_DOWN_UEI)
                                && ueis.contains(NODE_UP_UEI))
        );
    }

    @Test
    public void shouldUseAnnotationNameForDelegateName() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        AnnotatedDaemon daemon = new AnnotatedDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        assertThat(adapter.getName()).isEqualTo("TestDaemon");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDiscoverSingleUei() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        SingleHandlerDaemon daemon = new SingleHandlerDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        verify(mockService).addEventListener(
                any(EventListener.class),
                argThat((Collection<String> ueis) ->
                        ueis.size() == 1 && ueis.contains(NODE_DOWN_UEI))
        );
    }

    @Test
    public void shouldRejectNonAnnotatedListener() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        NotAnnotatedDaemon daemon = new NotAnnotatedDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);

        assertThatThrownBy(adapter::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be annotated");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDiscoverMultipleUeisFromUeisAttribute() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        MultiUeiDaemon daemon = new MultiUeiDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        verify(mockService).addEventListener(
                any(EventListener.class),
                argThat((Collection<String> ueis) ->
                        ueis.size() == 2
                                && ueis.contains(NODE_DOWN_UEI)
                                && ueis.contains(NODE_UP_UEI))
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDispatchEventToCorrectHandler() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);

        // Use a real daemon that tracks invocations
        TrackingDaemon daemon = new TrackingDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        // Create a mock event with the nodeDown UEI
        IEvent nodeDownEvent = mock(IEvent.class);
        org.mockito.Mockito.when(nodeDownEvent.getUei()).thenReturn(NODE_DOWN_UEI);

        // Invoke the delegate listener's onEvent directly
        adapter.onEvent(nodeDownEvent);

        assertThat(daemon.nodeDownCount).isEqualTo(1);
        assertThat(daemon.nodeUpCount).isEqualTo(0);

        // Now dispatch a nodeUp event
        IEvent nodeUpEvent = mock(IEvent.class);
        org.mockito.Mockito.when(nodeUpEvent.getUei()).thenReturn(NODE_UP_UEI);
        adapter.onEvent(nodeUpEvent);

        assertThat(daemon.nodeDownCount).isEqualTo(1);
        assertThat(daemon.nodeUpCount).isEqualTo(1);
    }

    @org.opennms.netmgt.events.api.annotations.EventListener(name = "TrackingDaemon")
    public static class TrackingDaemon {
        int nodeDownCount = 0;
        int nodeUpCount = 0;

        @EventHandler(uei = NODE_DOWN_UEI)
        public void handleNodeDown(IEvent event) {
            nodeDownCount++;
        }

        @EventHandler(uei = NODE_UP_UEI)
        public void handleNodeUp(IEvent event) {
            nodeUpCount++;
        }
    }
}
