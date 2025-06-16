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
package org.opennms.features.apilayer.events;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link EventSubscriptionService} that accepts EventListener implementations from the API,
 * wraps these in an adapter and delegates them to them to the real service implementation.
 */
public class EventSubscriptionServiceImpl implements EventSubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(EventSubscriptionServiceImpl.class);

    private final org.opennms.netmgt.events.api.EventSubscriptionService delegate;
    private final Map<EventListener, EventListenerAdapter> eventListenerToAdapterMap = new LinkedHashMap<>();

    public EventSubscriptionServiceImpl(org.opennms.netmgt.events.api.EventSubscriptionService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addEventListener(EventListener listener) {
        addListener(listener, delegate::addEventListener);
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        addListener(listener, l -> delegate.addEventListener(l, ueis));
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        addListener(listener, l -> delegate.addEventListener(l, uei));

    }

    @Override
    public void removeEventListener(EventListener listener) {
        removeListener(listener, delegate::removeEventListener);
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        removeListener(listener, l -> delegate.removeEventListener(l, ueis));
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        removeListener(listener, l -> delegate.removeEventListener(l, uei));
    }

    @Override
    public boolean hasEventListener(String uei) {
        return delegate.hasEventListener(uei);
    }

    private void addListener(EventListener listener, Consumer<EventListenerAdapter> addition) {
        final EventListenerAdapter adapter = getOrCreateAdapter(listener);
        addition.accept(adapter);
        adapter.incrementReferenceCount();
    }

    private void removeListener(EventListener listener, Consumer<EventListenerAdapter> removal) {
        final EventListenerAdapter adapter = eventListenerToAdapterMap.get(listener);
        if (adapter != null) {
            removal.accept(adapter);
            if (adapter.decrementReferenceCount() <= 0L) {
                eventListenerToAdapterMap.remove(listener);
                // Stop the listener thread
                delegate.removeEventListener(adapter);
            }
        }
    }

    private EventListenerAdapter getOrCreateAdapter(EventListener listener) {
        return eventListenerToAdapterMap.computeIfAbsent(listener, EventListenerAdapter::new);
    }

    private static class EventListenerAdapter implements org.opennms.netmgt.events.api.EventListener, org.opennms.netmgt.events.api.ThreadAwareEventListener {
        private final EventListener delegate;
        private final AtomicLong references = new AtomicLong(0);

        EventListenerAdapter(EventListener delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public int getNumThreads() {
            return delegate.getNumThreads();
        }

        @Override
        public void onEvent(IEvent event) {
            if (event == null || event.getUei() == null) {
                return;
            }
            try {
                delegate.onEvent(ModelMappers.toEvent(Event.copyFrom(event)));
            } catch (Exception e) {
                LOG.error("Error occurred while handling event with UEI='{}' on: {}. Error: {}",
                        event.getUei(), this, e.getMessage(), e);
            }
        }

        public void incrementReferenceCount() {
            references.incrementAndGet();
        }

        public long decrementReferenceCount() {
            return references.decrementAndGet();
        }

        @Override
        public String toString() {
            return "EventListenerAdapter{" +
                    "delegate=" + delegate +
                    ", references=" + references +
                    '}';
        }
    }
}
