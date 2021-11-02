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

package org.opennms.core.ipc.twin.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public abstract class AbstractTwinSubscriber implements TwinSubscriber {

    private final Multimap<String, SessionImpl<?>> sessionMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Map<String, byte[]> objMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MinionIdentity minionIdentity;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinSubscriber.class);
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("abstract-twin-subscriber-%d")
            .build();
    private ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

    protected AbstractTwinSubscriber(MinionIdentity minionIdentity) {
        this.minionIdentity = minionIdentity;
    }

    public AbstractTwinSubscriber() {
    }

    /**
     * @param twinRequest Send RpcRequest from @{@link AbstractTwinSubscriber}
     */
    protected abstract void sendRpcRequest(TwinRequestBean twinRequest);


    @Override
    public <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer) {
        SessionImpl<T> session = new SessionImpl<T>(key, clazz, consumer);
        sessionMap.put(key, session);
        String location = minionIdentity != null ? minionIdentity.getLocation() : null;
        TwinRequestBean twinRequestBean = new TwinRequestBean(key, location);

        // If there is an existing object, send that update to subscriber.
        byte[] objValue = objMap.get(key);
        if (objValue != null) {
            TwinResponseBean twinResponseBean = new TwinResponseBean(key, location, objValue);
            try {
                session.accept(objValue);
            } catch (IOException e) {
                LOG.error("Exception while sending response to consumer", e);
            }
        }

        sendRpcRequest(twinRequestBean);

        LOG.info("Subscribed to object updates with key {}", key);
        return session;
    }

    protected void accept(TwinResponseBean twinResponse) {

        // If Response is targeted to a location, ignore if it doesn't belong to the location of subscriber.
        if(twinResponse.getLocation() != null && !twinResponse.getLocation().equals(getLocation())) {
            return;
        }

        // Got empty response
        if (twinResponse.getObject() == null) {
            return;
        }

        // Swap the incoming object with the cached one while retrieving the old value in a (almost) atomic way
        final var old = this.objMap.put(twinResponse.getKey(), twinResponse.getObject());
        if (Arrays.equals(twinResponse.getObject(), old)) {
            return;
        }

        LOG.trace("Received object update with key {}", twinResponse.getKey());

        // Send update to each session.
        final var sessions = this.sessionMap.get(twinResponse.getKey());
        if (sessions == null) {
            LOG.trace("Session with key {} doesn't exist yet", twinResponse.getKey());
            return;
        }

        // Consume in our own thread instead of using broker's callback thread.
        executorService.execute(() -> {
                sessions.forEach(session -> {
                    try {
                        session.accept(twinResponse.getObject());
                    } catch (Exception e) {
                        LOG.error("Exception while sending response to Session {} for key {}", session, twinResponse.getKey(), e);
                    }
                });
        });
    }

    public void close() throws IOException {
        executorService.shutdown();
        objMap.clear();
        sessionMap.clear();
    }

    public MinionIdentity getMinionIdentity() {
        return minionIdentity;
    }

    private String getLocation() {
        if (minionIdentity != null) {
            return minionIdentity.getLocation();
        }
        return null;
    }

    private class SessionImpl<T> implements Closeable {

        private final String key;
        private final Consumer<T> consumer;
        private final Class<T> clazz;

        public SessionImpl(String key, Class<T> clazz, Consumer<T> consumer) {
            this.key = key;
            this.clazz = clazz;
            this.consumer = consumer;
        }

        @Override
        public void close() throws IOException {
            sessionMap.remove(key, this);
            LOG.info("Closed session with key {} ", key);
        }

        public void accept(byte[] objValue) throws IOException {
                final T value = objectMapper.readValue(objValue, clazz);
                LOG.trace("Updated consumer with key {}", key);
                consumer.accept(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionImpl<?> session = (SessionImpl<?>) o;
            return Objects.equals(key, session.key) && Objects.equals(consumer, session.consumer) && Objects.equals(clazz, session.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, consumer, clazz);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SessionImpl.class.getSimpleName() + "[", "]")
                    .add("key='" + key + "'")
                    .add("consumer=" + consumer)
                    .add("clazz=" + clazz)
                    .toString();
        }
    }
}
