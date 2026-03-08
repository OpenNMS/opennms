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

import java.util.Collection;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin adapter that composes an {@link EventForwarder} and an
 * {@link EventSubscriptionService} into a single {@link EventIpcManager}
 * (and {@link EventIpcBroadcaster}) instance.
 *
 * <p>This allows existing daemon code that depends on {@code EventIpcManager}
 * to work unchanged when backed by Kafka-based implementations.</p>
 *
 * <p>{@link EventIpcBroadcaster#broadcastNow} is a no-op because in Kafka mode
 * broadcasting is handled by the Kafka consumer poll loop in
 * {@link KafkaEventSubscriptionService}.</p>
 */
public class KafkaEventIpcManagerAdapter implements EventIpcManager, EventIpcBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventIpcManagerAdapter.class);

    private final EventForwarder eventForwarder;
    private final EventSubscriptionService subscriptionService;

    public KafkaEventIpcManagerAdapter(EventForwarder eventForwarder,
                                       EventSubscriptionService subscriptionService) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder, "eventForwarder");
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
    }

    // -------- EventForwarder delegation --------

    @Override
    public void sendNow(Event event) {
        eventForwarder.sendNow(event);
    }

    @Override
    public void sendNow(Log eventLog) {
        eventForwarder.sendNow(eventLog);
    }

    @Override
    public void sendNowSync(Event event) {
        eventForwarder.sendNowSync(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        eventForwarder.sendNowSync(eventLog);
    }

    // -------- EventProxy delegation (delegates to sendNow) --------

    @Override
    public void send(Event event) throws EventProxyException {
        eventForwarder.sendNow(event);
    }

    @Override
    public void send(Log eventLog) throws EventProxyException {
        eventForwarder.sendNow(eventLog);
    }

    // -------- EventSubscriptionService delegation --------

    @Override
    public void addEventListener(EventListener listener) {
        subscriptionService.addEventListener(listener);
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        subscriptionService.addEventListener(listener, ueis);
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        subscriptionService.addEventListener(listener, uei);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        subscriptionService.removeEventListener(listener);
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        subscriptionService.removeEventListener(listener, ueis);
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        subscriptionService.removeEventListener(listener, uei);
    }

    @Override
    public boolean hasEventListener(String uei) {
        return subscriptionService.hasEventListener(uei);
    }

    // -------- EventIpcBroadcaster (no-op) --------

    @Override
    public void broadcastNow(Event event, boolean synchronous) {
        LOG.debug("broadcastNow() is a no-op in Kafka mode; event UEI={}, synchronous={}",
                event != null ? event.getUei() : "null", synchronous);
    }
}
