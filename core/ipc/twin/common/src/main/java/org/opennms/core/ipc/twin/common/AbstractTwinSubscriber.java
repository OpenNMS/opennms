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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public abstract class AbstractTwinSubscriber implements TwinSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinSubscriber.class);

    private final MinionIdentity minionIdentity;

    private final Multimap<String, SessionImpl<?>> sessionMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Map<String, TwinTracker> objMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                                                                              .setNameFormat("abstract-twin-subscriber-%d")
                                                                                              .build());

    protected AbstractTwinSubscriber(MinionIdentity minionIdentity) {
        this.minionIdentity = minionIdentity;
    }

    protected abstract void sendRpcRequest(TwinRequest twinRequest);


    @Override
    public <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer) {
        SessionImpl<T> session = new SessionImpl<T>(key, clazz, consumer);
        sessionMap.put(key, session);
        String location = minionIdentity != null ? minionIdentity.getLocation() : null;

        TwinTracker twinTracker = objMap.get(key);
        // If there is an existing object, send that update to subscriber
        if (twinTracker != null) {
            try {
                session.accept(twinTracker.getObj());
            } catch (Exception e) {
                LOG.error("Exception while sending response to consumer", e);
            }
        } else {
            TwinRequest twinRequest = new TwinRequest(key, location);
            sendRpcRequest(twinRequest);
        }

        LOG.info("Subscribed to object updates with key {}", key);
        return session;
    }

    protected void accept(TwinUpdate twinUpdate) {

        // If Response is targeted to a location, ignore if it doesn't belong to the location of subscriber.
        if (twinUpdate.getLocation() != null && !twinUpdate.getLocation().equals(getLocation())) {
            return;
        }

        // Got empty response
        if (twinUpdate.getObject() == null || twinUpdate.getSessionId() == null) {
            return;
        }

        // Consume in our own thread instead of using broker's callback thread.
        executorService.execute(() -> {
            validateAndHandleUpdate(twinUpdate);
        });
    }

    private void validateAndHandleUpdate(TwinUpdate twinUpdate) {
        TwinTracker twinTracker = objMap.get(twinUpdate.getKey());
        if (twinTracker == null) {
            if (!twinUpdate.isPatch()) {
                updateSessions(twinUpdate, twinUpdate.getObject());
            }
            return;
        }
        if (isObjectUpdated(twinTracker, twinUpdate)) {
            LOG.trace("Received object update with key {}", twinUpdate.getKey());
            // No need to update if version we are getting is less than what we have with the same session.
            if (twinTracker.getSessionId().equals(twinUpdate.getSessionId())
                    && twinTracker.getVersion() > twinUpdate.getVersion()) {
                return;
            }
            // If this is from new session, reset tracker.
            if (!twinTracker.getSessionId().equals(twinUpdate.getSessionId())) {
                if (!twinUpdate.isPatch()) {
                    updateSessions(twinUpdate, twinUpdate.getObject());
                }
                return;
            }
            // We can't apply patch when the version jumps more than one version.
            if (twinUpdate.getVersion() != twinTracker.getVersion() + 1) {
                sendRpcRequest(new TwinRequest(twinUpdate.getKey(), twinUpdate.getLocation()));
                return;
            }
            byte[] patchedBytes = applyPatch(twinTracker, twinUpdate);
            // Invoke RPC when we can't apply patch properly.
            if (patchedBytes != null) {
                // Update twin object for sessions.
                updateSessions(twinUpdate, patchedBytes);
            } else {
                sendRpcRequest(new TwinRequest(twinUpdate.getKey(), twinUpdate.getLocation()));
            }
        }
    }

    private byte[] applyPatch(TwinTracker twinTracker, TwinUpdate twinUpdate) {
        if (!twinUpdate.isPatch()) {
            return twinUpdate.getObject();
        }
        try {
            JsonNode resultingDiff = objectMapper.readTree(twinUpdate.getObject());
            JsonNode original = objectMapper.readTree(twinTracker.getObj());
            JsonPatch patch = JsonPatch.fromJson(resultingDiff);
            JsonNode resultNode = patch.apply(original);
            return resultNode.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Not able to apply patch for key {}", twinUpdate.getKey(), e);
        }
        return null;
    }

    private boolean isObjectUpdated(TwinTracker twinTracker, TwinUpdate twinUpdate) {
        if (twinUpdate.isPatch()) {
            return true;
        }
        return !Arrays.equals(twinTracker.getObj(), twinUpdate.getObject());
    }

    private void updateSessions(TwinUpdate twinUpdate, byte[] newObjBytes) {
        // Update twin object in local cache.
        objMap.put(twinUpdate.getKey(), new TwinTracker(newObjBytes, twinUpdate.getVersion(), twinUpdate.getSessionId()));
        // Send update to each session.
        synchronized(sessionMap) {
            final var sessions = sessionMap.get(twinUpdate.getKey());
            if (sessions != null) {
                sessions.forEach(session -> {
                    try {
                        session.accept(newObjBytes);
                    } catch (Exception e) {
                        LOG.error("Exception while sending update to Session {} for key {}", session, twinUpdate.getKey(), e);
                    }
                });
            }
        }
    }


    protected TwinUpdate mapTwinResponseToProto(byte[] responseBytes) {
        TwinUpdate twinUpdate = new TwinUpdate();
        try {
            TwinResponseProto twinResponseProto = TwinResponseProto.parseFrom(responseBytes);

            if (!Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                twinUpdate.setLocation(twinResponseProto.getLocation());
            }
            if(!Strings.isNullOrEmpty(twinResponseProto.getSessionId())) {
                twinUpdate.setSessionId(twinResponseProto.getSessionId());
            }
            twinUpdate.setKey(twinResponseProto.getConsumerKey());
            if (twinResponseProto.getTwinObject() != null) {
                twinUpdate.setObject(twinResponseProto.getTwinObject().toByteArray());
            }
            twinUpdate.setPatch(twinResponseProto.getIsPatchObject());
            twinUpdate.setVersion(twinResponseProto.getVersion());
            return twinUpdate;
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Failed to parse response from proto", e);
            throw new RuntimeException(e);
        }
    }

    protected TwinRequestProto mapTwinRequestToProto(TwinRequest twinRequest) {
        TwinRequestProto.Builder builder = TwinRequestProto.newBuilder();
        builder.setConsumerKey(twinRequest.getKey()).setLocation(getMinionIdentity().getLocation())
                .setSystemId(getMinionIdentity().getId());
        return builder.build();
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
