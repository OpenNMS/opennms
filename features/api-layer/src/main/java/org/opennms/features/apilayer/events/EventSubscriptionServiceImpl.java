/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
        public void onEvent(Event event) {
            if (event == null || event.getUei() == null) {
                return;
            }
            try {
                delegate.onEvent(ModelMappers.toEvent(event));
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
