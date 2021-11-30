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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTwinPublisher implements TwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinPublisher.class);
    private final Map<SessionKey, TwinTracker> twinTrackerMap = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final LocalTwinSubscriber localTwinSubscriber;

    public AbstractTwinPublisher(LocalTwinSubscriber localTwinSubscriber) {
        this.localTwinSubscriber = Objects.requireNonNull(localTwinSubscriber);
    }

    /**
     * @param sinkUpdate Handle sink Update from @{@link AbstractTwinPublisher}.
     */
    protected abstract void handleSinkUpdate(TwinUpdate sinkUpdate);

    @Override
    public <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException {
        SessionKey sessionKey = new SessionKey(key, location);
        LOG.info("Registered a session with key {}", sessionKey);
        return new SessionImpl<>(sessionKey);
    }

    protected TwinUpdate getTwin(TwinRequest twinRequest) {
        TwinTracker twinTracker = getTwinTracker(twinRequest.getKey(), twinRequest.getLocation());
        TwinUpdate twinUpdate;
        if (twinTracker == null) {
            // No twin object exists for this key yet, return with null object.
            twinUpdate = new TwinUpdate(twinRequest.getKey(), twinRequest.getLocation(), null);
        } else {
            // Fill TwinUpdate fields from TwinTracker.
            twinUpdate = new TwinUpdate(twinRequest.getKey(), twinRequest.getLocation(), twinTracker.getObj());
            twinUpdate.setPatch(false);
            twinUpdate.setVersion(twinTracker.getVersion());
            twinUpdate.setSessionId(twinTracker.getSessionId());
        }
        return twinUpdate;
    }

    private synchronized TwinTracker getTwinTracker(String key, String location) {
        // Check if we have a session key specific to location else check session key without location.
        TwinTracker twinTracker = twinTrackerMap.get(new SessionKey(key, location));
        if(twinTracker == null) {
            twinTracker = twinTrackerMap.get(new SessionKey(key, null));
        }
        return twinTracker;
    }

    protected TwinResponseProto mapTwinResponse(TwinUpdate twinUpdate) {
        TwinResponseProto.Builder builder = TwinResponseProto.newBuilder();
        if (!Strings.isNullOrEmpty(twinUpdate.getLocation())) {
            builder.setLocation(twinUpdate.getLocation());
        }
        if(!Strings.isNullOrEmpty(twinUpdate.getSessionId())) {
            builder.setSessionId(twinUpdate.getSessionId());
        }
        builder.setConsumerKey(twinUpdate.getKey());
        if (twinUpdate.getObject() != null) {
            builder.setTwinObject(ByteString.copyFrom(twinUpdate.getObject()));
        }
        builder.setIsPatchObject(twinUpdate.isPatch());
        builder.setVersion(twinUpdate.getVersion());
        return builder.build();
    }

    protected TwinRequest mapTwinRequestProto(byte[] twinRequestBytes) {
        TwinRequest twinRequest = new TwinRequest();
        try {
            TwinRequestProto twinRequestProto = TwinRequestProto.parseFrom(twinRequestBytes);
            twinRequest.setKey(twinRequestProto.getConsumerKey());
            if (!Strings.isNullOrEmpty(twinRequestProto.getLocation())) {
                twinRequest.setLocation(twinRequestProto.getLocation());
            }
        } catch (InvalidProtocolBufferException e) {
            LOG.warn("Failed to parse protobuf for the request", e);
            throw new RuntimeException(e);
        }
        return twinRequest;
    }

    private synchronized TwinUpdate getResponseFromUpdatedObj(byte[] updatedObj, SessionKey sessionKey) {
        TwinTracker twinTracker = getTwinTracker(sessionKey.key, sessionKey.location);
        if (twinTracker == null || !Arrays.equals(twinTracker.getObj(), updatedObj)) {
            TwinUpdate twinUpdate = new TwinUpdate(sessionKey.key, sessionKey.location, updatedObj);
            if (twinTracker == null) {
                twinTracker = new TwinTracker(updatedObj);
            } else {
                // Generate patch and update response with patch.
                byte[] patchValue = getPatchValue(twinTracker.getObj(), updatedObj, sessionKey);
                if (patchValue != null) {
                    twinUpdate.setObject(patchValue);
                    twinUpdate.setPatch(true);
                }
                // Update Twin tracker with updated obj.
                twinTracker.update(updatedObj);
            }
            twinTrackerMap.put(sessionKey, twinTracker);
            twinUpdate.setVersion(twinTracker.getVersion());
            twinUpdate.setSessionId(twinTracker.getSessionId());
            return twinUpdate;
        }
        return null;
    }

    private byte[] getPatchValue(byte[] originalObj, byte[] updatedObj, SessionKey sessionKey) {
        try {
            JsonNode sourceNode = objectMapper.readTree(originalObj);
            JsonNode targetNode = objectMapper.readTree(updatedObj);
            JsonNode diffNode = JsonDiff.asJson(sourceNode, targetNode);
            return diffNode.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Unable to generate patch for SessionKey {}", sessionKey, e);
        }
        return null;
    }

    private synchronized void removeSessionKey(SessionKey sessionKey) {
        twinTrackerMap.remove(sessionKey);
    }

    public synchronized void forEachSession(BiConsumer<SessionKey, TwinTracker> consumer) {
        twinTrackerMap.forEach(consumer);
    }

    private class SessionImpl<T> implements Session<T> {

        private final SessionKey sessionKey;

        public SessionImpl(SessionKey sessionKey) {
            this.sessionKey = sessionKey;
        }

        @Override
        public void publish(T obj) throws IOException {
            LOG.info("Published an object update for the session with key {}", sessionKey.toString());
            byte[] objInBytes = objectMapper.writeValueAsBytes(obj);
            TwinUpdate twinUpdate = getResponseFromUpdatedObj(objInBytes, sessionKey);
            if(twinUpdate != null) {
                // Send update to local subscriber and on sink path.
                localTwinSubscriber.accept(twinUpdate);
                handleSinkUpdate(twinUpdate);
            }
        }


        @Override
        public void close() throws IOException {
            removeSessionKey(sessionKey);
            LOG.info("Closed session with key {} ", sessionKey);
        }
    }

    public static class SessionKey {

        public final String key;
        public final String location;

        private SessionKey(String key, String location) {
            this.key = key;
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionKey that = (SessionKey) o;
            return Objects.equals(key, that.key) && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, location);
        }

        @Override
        public String toString() {
            return "SessionKey{" +
                    "key='" + key + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }

}
