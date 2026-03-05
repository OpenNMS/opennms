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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class KafkaEventIpcManagerAdapterTest {

    private EventForwarder eventForwarder;
    private EventSubscriptionService subscriptionService;
    private KafkaEventIpcManagerAdapter adapter;

    @Before
    public void setUp() {
        eventForwarder = mock(EventForwarder.class);
        subscriptionService = mock(EventSubscriptionService.class);
        adapter = new KafkaEventIpcManagerAdapter(eventForwarder, subscriptionService);
    }

    // ---- Verify it implements the right interfaces ----

    @Test
    public void shouldImplementEventIpcManager() {
        assertThat(adapter).isInstanceOf(EventIpcManager.class);
    }

    @Test
    public void shouldImplementEventIpcBroadcaster() {
        assertThat(adapter).isInstanceOf(EventIpcBroadcaster.class);
    }

    // ---- EventForwarder delegation ----

    @Test
    public void sendNowEventShouldDelegateToEventForwarder() {
        Event event = new Event();
        adapter.sendNow(event);
        verify(eventForwarder).sendNow(event);
    }

    @Test
    public void sendNowLogShouldDelegateToEventForwarder() {
        Log log = new Log();
        adapter.sendNow(log);
        verify(eventForwarder).sendNow(log);
    }

    @Test
    public void sendNowSyncEventShouldDelegateToEventForwarder() {
        Event event = new Event();
        adapter.sendNowSync(event);
        verify(eventForwarder).sendNowSync(event);
    }

    @Test
    public void sendNowSyncLogShouldDelegateToEventForwarder() {
        Log log = new Log();
        adapter.sendNowSync(log);
        verify(eventForwarder).sendNowSync(log);
    }

    // ---- EventProxy delegation (send -> sendNow) ----

    @Test
    public void sendEventShouldDelegateToSendNow() throws Exception {
        Event event = new Event();
        adapter.send(event);
        verify(eventForwarder).sendNow(event);
    }

    @Test
    public void sendLogShouldDelegateToSendNow() throws Exception {
        Log log = new Log();
        adapter.send(log);
        verify(eventForwarder).sendNow(log);
    }

    // ---- EventSubscriptionService delegation ----

    @Test
    public void addEventListenerShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        adapter.addEventListener(listener);
        verify(subscriptionService).addEventListener(listener);
    }

    @Test
    public void addEventListenerWithUeisShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        Collection<String> ueis = Arrays.asList("uei.opennms.org/a", "uei.opennms.org/b");
        adapter.addEventListener(listener, ueis);
        verify(subscriptionService).addEventListener(listener, ueis);
    }

    @Test
    public void addEventListenerWithSingleUeiShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        String uei = "uei.opennms.org/test";
        adapter.addEventListener(listener, uei);
        verify(subscriptionService).addEventListener(listener, uei);
    }

    @Test
    public void removeEventListenerShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        adapter.removeEventListener(listener);
        verify(subscriptionService).removeEventListener(listener);
    }

    @Test
    public void removeEventListenerWithUeisShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        Collection<String> ueis = Arrays.asList("uei.opennms.org/a", "uei.opennms.org/b");
        adapter.removeEventListener(listener, ueis);
        verify(subscriptionService).removeEventListener(listener, ueis);
    }

    @Test
    public void removeEventListenerWithSingleUeiShouldDelegateToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        String uei = "uei.opennms.org/test";
        adapter.removeEventListener(listener, uei);
        verify(subscriptionService).removeEventListener(listener, uei);
    }

    @Test
    public void hasEventListenerShouldDelegateToSubscriptionService() {
        String uei = "uei.opennms.org/test";
        when(subscriptionService.hasEventListener(uei)).thenReturn(true);
        assertThat(adapter.hasEventListener(uei)).isTrue();
        verify(subscriptionService).hasEventListener(uei);
    }

    // ---- EventIpcBroadcaster (no-op) ----

    @Test
    public void broadcastNowShouldBeNoOp() {
        Event event = new Event();
        adapter.broadcastNow(event, true);
        verifyNoInteractions(eventForwarder);
        verifyNoInteractions(subscriptionService);
    }
}
