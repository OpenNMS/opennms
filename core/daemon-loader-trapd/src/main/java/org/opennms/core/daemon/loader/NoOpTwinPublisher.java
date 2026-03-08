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
package org.opennms.core.daemon.loader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;

/**
 * In-process TwinPublisher/TwinSubscriber for standalone daemon containers.
 * <p>
 * Connects publish → subscribe within the same JVM so that:
 * <ol>
 *   <li>Trapd.onStart() registers a session and publishes trap config</li>
 *   <li>TrapListener.subscribe() receives the config and opens the UDP socket</li>
 * </ol>
 * Handles both orderings: subscribe-before-register and register-before-subscribe.
 */
public class NoOpTwinPublisher implements TwinPublisher {

    private final Map<String, SessionImpl<?>> sessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<Consumer<?>>> pendingSubscribers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException {
        SessionImpl<T> session = new SessionImpl<>(key);
        sessions.put(key, session);

        // Deliver any pending subscribers that subscribed before this session was registered
        CopyOnWriteArrayList<Consumer<?>> pending = pendingSubscribers.remove(key);
        if (pending != null) {
            for (Consumer<?> consumer : pending) {
                session.addSubscriber((Consumer<T>) consumer);
            }
        }

        return session;
    }

    @Override
    public void close() {
        sessions.clear();
        pendingSubscribers.clear();
    }

    /**
     * Create a TwinSubscriber that reads from this publisher's sessions.
     */
    public TwinSubscriber createSubscriber() {
        return new LocalSubscriber();
    }

    @SuppressWarnings("unchecked")
    synchronized <T> void subscribe(String key, Consumer<T> consumer) {
        SessionImpl<T> session = (SessionImpl<T>) sessions.get(key);
        if (session != null) {
            session.addSubscriber(consumer);
        } else {
            // Session doesn't exist yet — store as pending.
            // When register() is called later, the subscriber will be attached.
            pendingSubscribers.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(consumer);
        }
    }

    private static class SessionImpl<T> implements Session<T> {
        private final String key;
        private final CopyOnWriteArrayList<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();

        SessionImpl(String key) {
            this.key = Objects.requireNonNull(key);
        }

        @Override
        public void publish(T obj) {
            for (Consumer<T> sub : subscribers) {
                sub.accept(obj);
            }
        }

        @Override
        public void close() {
            subscribers.clear();
        }

        void addSubscriber(Consumer<T> consumer) {
            subscribers.add(consumer);
        }
    }

    private class LocalSubscriber implements TwinSubscriber {
        @Override
        public <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer) {
            NoOpTwinPublisher.this.subscribe(key, consumer);
            return () -> {};
        }

        @Override
        public void close() {
        }
    }
}
