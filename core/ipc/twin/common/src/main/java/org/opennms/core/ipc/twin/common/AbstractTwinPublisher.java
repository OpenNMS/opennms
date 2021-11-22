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
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinStrategy;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;

public abstract class AbstractTwinPublisher implements TwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinPublisher.class);
    protected static final String TAG_TWIN_SINK = "TwinSink";
    protected static final String TAG_TWIN_RPC = "TwinRPC";
    protected static final String TAG_VERSION = "version";
    protected static final String TAG_SESSION_ID = "sessionId";
    protected static final String TAG_PATCH = "isPatch";
    private final Map<SessionKey, TwinTracker> twinTrackerMap = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final LocalTwinSubscriber localTwinSubscriber;
    private final TracerRegistry tracerRegistry;
    private final Tracer tracer;

    public AbstractTwinPublisher(LocalTwinSubscriber localTwinSubscriber, TracerRegistry tracerRegistry) {
        this.localTwinSubscriber = Objects.requireNonNull(localTwinSubscriber);
        this.tracerRegistry = Objects.requireNonNull(tracerRegistry);
        this.tracerRegistry.init(SystemInfoUtils.getInstanceId());
        this.tracer = this.tracerRegistry.getTracer();
    }

    public AbstractTwinPublisher(LocalTwinSubscriber localTwinSubscriber) {
        this.localTwinSubscriber = Objects.requireNonNull(localTwinSubscriber);
        this.tracerRegistry = localTwinSubscriber.getTracerRegistry();
        this.tracerRegistry.init(SystemInfoUtils.getInstanceId());
        this.tracer = this.tracerRegistry.getTracer();
    }

    /**
     * @param sinkUpdate Handle sink Update from @{@link AbstractTwinPublisher}.
     */
    protected abstract void handleSinkUpdate(TwinUpdate sinkUpdate);

    @Override
    public <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            SessionKey sessionKey = new SessionKey(key, location);
            LOG.info("Registered a session with key {}", sessionKey);
            return new SessionImpl<>(sessionKey);
        }
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
        twinUpdate.getTracingInfo().forEach(builder::putTracingInfo);
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
            twinRequestProto.getTracingInfoMap().forEach(twinRequest::addTracingInfo);
        } catch (InvalidProtocolBufferException e) {
            LOG.warn("Failed to parse protobuf for the request", e);
            throw new RuntimeException(e);
        }
        return twinRequest;
    }

    protected void addTracingInfo(Span span, TwinUpdate twinUpdate) {
        TracingInfoCarrier.updateTracingMetadata(getTracer(), span, twinUpdate::addTracingInfo);
        span.setTag(TAG_TWIN_RPC, true);
        span.setTag(TAG_VERSION, twinUpdate.getVersion());
        span.setTag(TAG_SESSION_ID, twinUpdate.getSessionId());
        span.setTag(TAG_PATCH, twinUpdate.isPatch());
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

    public Tracer getTracer() {
        return tracer;
    }

    private class SessionImpl<T> implements Session<T> {

        private final SessionKey sessionKey;

        public SessionImpl(SessionKey sessionKey) {
            this.sessionKey = sessionKey;
        }

        @Override
        public void publish(T obj) throws IOException {
            try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
                LOG.info("Published an object update for the session with key {}", sessionKey.toString());
                String tracingOperationKey = sessionKey.location != null ? sessionKey.key + "@" + sessionKey.location : sessionKey.key;
                Span span = tracer.buildSpan(tracingOperationKey).start();
                byte[] objInBytes = objectMapper.writeValueAsBytes(obj);
                TwinUpdate twinUpdate = getResponseFromUpdatedObj(objInBytes, sessionKey);
                if (twinUpdate != null) {
                    TracingInfoCarrier.updateTracingMetadata(AbstractTwinPublisher.this.tracer, span, twinUpdate::addTracingInfo);
                    // Send update to local subscriber and on sink path.
                    span.setTag(TAG_TWIN_SINK, true);
                    if (sessionKey.location != null) {
                        span.setTag(TAG_LOCATION, sessionKey.location);
                    }
                    span.setTag(TAG_VERSION, twinUpdate.getVersion());
                    span.setTag(TAG_SESSION_ID, twinUpdate.getSessionId());
                    localTwinSubscriber.accept(twinUpdate);
                    handleSinkUpdate(twinUpdate);
                }
                span.finish();
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
