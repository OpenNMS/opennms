/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.memory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.opennms.core.ipc.twin.api.TwinPublisher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MemoryTwinPublisher implements TwinPublisher {

    private final Map<SessionKey, SessionImpl<?>> sessions = Maps.newConcurrentMap();

    private final Set<Subscription<?>> pending = Sets.newConcurrentHashSet();

    public MemoryTwinPublisher() {
    }

    private <T> Optional<SessionImpl<T>> findSession(final SessionKey key, final Class<T> clazz) {
        var session = this.sessions.get(key);
        if (session == null && key.hasLocation()) {
            session = this.sessions.get(key.withoutLocation());
        }

        if (session == null) {
            return Optional.empty();
        }
        if (session.clazz != clazz) {
            throw new IllegalStateException("Session has different class: " + key);
        }

        return Optional.of((SessionImpl<T>) session);
    }

    @Override
    public synchronized <T> Session<T> register(final String key, final Class<T> clazz, final String location) throws IOException {
        final var sessionKey = new SessionKey(key, location);

        if (this.sessions.containsKey(sessionKey)) {
            return (Session<T>) this.sessions.get(sessionKey);
        }

        if (sessionKey.hasLocation() && this.sessions.containsKey(sessionKey.withoutLocation())) {
            throw new IllegalStateException("Session already registered without location: " + sessionKey);
        }

        final var session = new SessionImpl<>(sessionKey, clazz);
        this.sessions.put(sessionKey, session);

        // Find all pending subscriptions for this session and attach them
        final var pending = this.pending.iterator();
        while (pending.hasNext()) {
            final var subscription = pending.next();
            if (Objects.equals(subscription.key, key) && location == null || Objects.equals(subscription.location, location)) {
                if (subscription.clazz != clazz) {
                    throw new IllegalStateException("Session has different class: " + sessionKey);
                }

                session.subscribe((Subscription<T>) subscription);
                pending.remove();
            }
        }

        return session;
    }

    public synchronized  <T> Subscription<T> subscribe(final String key, final String location, final Class<T> clazz, final Consumer<T> consumer) {
        final var subscription = new Subscription<T>(key, location, clazz, consumer);

        // If the key is already registered, we subscribe to the session and publish the current value.
        // Otherwise, the subscription is stored as pending.
        this.findSession(new SessionKey(key, location), clazz)
            .ifPresentOrElse((session) -> session.subscribe(subscription),
                             () -> this.pending.add(subscription));

        return subscription;
    }

    public static class SessionKey {
        public final String key;
        public final String location;

        public SessionKey(final String key, final String location) {
            this.key = Objects.requireNonNull(key);
            this.location = location;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SessionKey)) {
                return false;
            }
            final SessionKey that = (SessionKey) o;
            return Objects.equals(this.key, that.key) &&
                   Objects.equals(this.location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.location);
        }

        public SessionKey withoutLocation() {
            return new SessionKey(this.key, null);
        }

        public boolean hasLocation() {
            return this.location != null;
        }
    }

    private class SessionImpl<T> implements TwinPublisher.Session<T> {
        private final SessionKey key;
        private final Class<T> clazz;

        private final AtomicReference<T> current = new AtomicReference<>();

        private final Set<Subscription<T>> subscriptions = Sets.newConcurrentHashSet();

        private SessionImpl(final SessionKey key,
                            final Class<T> clazz) {
            this.key = Objects.requireNonNull(key);
            this.clazz = Objects.requireNonNull(clazz);
        }

        @Override
        public void publish(T obj) throws IOException {
            this.current.set(obj);
            this.subscriptions.forEach(consumer -> consumer.publish(obj));
        }

        @Override
        public void close() throws IOException {
            MemoryTwinPublisher.this.sessions.remove(this.key);

            this.current.set(null);
        }

        public void subscribe(final Subscription<T> subscription) {
            this.subscriptions.add(subscription);

            final T current = this.current.get();
            if (current != null) {
                subscription.publish(current);
            }
        }
    }

    private class Subscription<T> implements Closeable {
        public final String key;
        public final String location;
        public final Class<T> clazz;
        public final Consumer<T> consumer;

        private Subscription(final String key, final String location, final Class<T> clazz, final Consumer<T> consumer) {
            this.key = Objects.requireNonNull(key);
            this.location = Objects.requireNonNull(location);
            this.clazz = Objects.requireNonNull(clazz);
            this.consumer = Objects.requireNonNull(consumer);
        }

        private void publish(final T t) {
            this.consumer.accept(t);
        }

        @Override
        public void close() throws IOException {
            MemoryTwinPublisher.this.pending.remove(this);
            MemoryTwinPublisher.this.findSession(new SessionKey(this.key, this.location), this.clazz)
                                    .ifPresent(session -> session.subscriptions.remove(this));
        }
    }

    @Override
    public void close() {
    }
}
