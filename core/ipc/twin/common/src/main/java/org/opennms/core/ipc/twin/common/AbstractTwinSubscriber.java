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
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;

public abstract class AbstractTwinSubscriber implements TwinSubscriber {

    private final Multimap<String, SessionImpl<?>> sessionMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Map<String, TwinTracker> objMap = new ConcurrentHashMap<>();
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
    protected abstract void sendRpcRequest(TwinRequest twinRequest);


    @Override
    public <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer) {
        SessionImpl<T> session = new SessionImpl<T>(key, clazz, consumer);
        sessionMap.put(key, session);
        String location = minionIdentity != null ? minionIdentity.getLocation() : null;
        TwinRequest twinRequest = new TwinRequest(key, location);
        sendRpcRequest(twinRequest);
        TwinTracker twinTracker = objMap.get(key);
        // If there is an existing object, send that update to subscriber
        if (twinTracker != null) {
            try {
                session.accept(twinTracker.getObj());
            } catch (Exception e) {
                LOG.error("Exception while sending response to consumer", e);
            }
        }


        LOG.info("Subscribed to object updates with key {}", key);
        return session;
    }

    protected void accept(TwinUpdate twinResponse) {

        // If Response is targeted to a location, ignore if it doesn't belong to the location of subscriber.
        if (twinResponse.getLocation() != null && !twinResponse.getLocation().equals(getLocation())) {
            return;
        }

        // Got empty response
        if (twinResponse.getObject() == null) {
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
            validateAndSendResponse(twinResponse);
        });
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
        }
        return twinUpdate;
    }

    protected TwinRequestProto mapTwinRequestToProto(TwinRequest twinRequest) {
        TwinRequestProto.Builder builder = TwinRequestProto.newBuilder();
        builder.setConsumerKey(twinRequest.getKey()).setLocation(getMinionIdentity().getLocation())
                .setSystemId(getMinionIdentity().getId());
        return builder.build();
    }

    private void validateAndSendResponse(TwinUpdate twinResponse) {
        TwinTracker twinTracker = objMap.get(twinResponse.getKey());
        if (twinResponse.getObject() != null &&
                isObjectUpdated(twinTracker, twinResponse)) {
            LOG.trace("Received object update with key {}", twinResponse.getKey());

            // No need to update if version we are getting is less than what we have with the same session.
            if (twinTracker != null && twinTracker.getSessionId() != null
                    && twinTracker.getSessionId().equals(twinResponse.getSessionId())
                    && twinTracker.getVersion() > twinResponse.getVersion()) {
                return;
            }
            byte[] patchedBytes = applyPatch(twinTracker, twinResponse);
            // Invoke RPC when we can't apply patch properly.
            if (patchedBytes == null) {
                sendRpcRequest(new TwinRequest(twinResponse.getKey(), twinResponse.getLocation()));
                return;
            }
            // Update twin object in local cache.
            objMap.put(twinResponse.getKey(), new TwinTracker(patchedBytes, twinResponse.getVersion(), twinResponse.getSessionId()));
            // Send update to each session.
            if (sessionMap.containsKey(twinResponse.getKey())) {
                sessionMap.get(twinResponse.getKey()).forEach(session -> {
                    try {
                        session.accept(patchedBytes);
                    } catch (Exception e) {
                        LOG.error("Exception while sending response to Session {} for key {}", session, twinResponse.getKey(), e);
                    }
                });
            }
        }
    }

    private byte[] applyPatch(TwinTracker twinTracker, TwinUpdate twinUpdate) {
        if (twinTracker == null || !twinUpdate.isPatch()) {
            return twinUpdate.getObject();
        }
        // We can't apply patch when the version jumps more than one version.
        if (twinUpdate.getVersion() != twinTracker.getVersion() + 1) {
            return null;
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

    boolean isObjectUpdated(TwinTracker twinTracker, TwinUpdate twinResponse) {
        if (twinTracker == null || twinResponse.isPatch()) {
            return true;
        }
        return !Arrays.equals(twinTracker.getObj(), twinResponse.getObject());
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

    private class SessionKey {
        public final String sessionId;
        public final String key;

        public SessionKey(String key, String sessionId) {
            this.key = key;
            this.sessionId = sessionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SessionKey)) return false;
            SessionKey that = (SessionKey) o;
            return com.google.common.base.Objects.equal(sessionId, that.sessionId) && com.google.common.base.Objects.equal(key, that.key);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(sessionId, key);
        }
    }


}
