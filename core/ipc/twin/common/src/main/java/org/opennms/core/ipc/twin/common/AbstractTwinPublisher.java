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
package org.opennms.core.ipc.twin.common;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.opennms.core.logging.Logging;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;

public abstract class AbstractTwinPublisher implements TwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinPublisher.class);
    protected static final String TAG_TWIN_SINK = "TwinSink";
    protected static final String TAG_TWIN_RPC = "TwinRPC";
    protected static final String TAG_VERSION = "version";
    protected static final String TAG_SESSION_ID = "sessionId";
    protected static final String TAG_PATCH = "isPatch";
    private static final String SINK_UPDATE_SENT = "sinkUpdateSent";
    private static final String TWIN_RESPONSE_SENT = "twinResponseSent";
    private static final String TWIN_EMPTY_RESPONSE_SENT = "twinEmptyResponseSent";
    private final Map<SessionKey, TwinTracker> twinTrackerMap = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final LocalTwinSubscriber localTwinSubscriber;
    private final Tracer tracer;
    private final MetricRegistry metrics;

    public AbstractTwinPublisher(LocalTwinSubscriber localTwinSubscriber, TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        this.localTwinSubscriber = Objects.requireNonNull(localTwinSubscriber);
        Objects.requireNonNull(tracerRegistry);
        tracerRegistry.init(SystemInfoUtils.getInstanceId());
        this.tracer = tracerRegistry.getTracer();
        this.metrics = metricRegistry;
    }

    public AbstractTwinPublisher(LocalTwinSubscriber localTwinSubscriber) {
        this(localTwinSubscriber, localTwinSubscriber.getTracerRegistry(), localTwinSubscriber.getMetricRegistry());
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
            // JMX Metrics
            updateCounter(MetricRegistry.name(twinRequest.location, twinRequest.getKey(), TWIN_EMPTY_RESPONSE_SENT));
        } else {
            // Fill TwinUpdate fields from TwinTracker.
            twinUpdate = new TwinUpdate(twinRequest.getKey(), twinRequest.getLocation(), twinTracker.getObj());
            twinUpdate.setPatch(false);
            twinUpdate.setVersion(twinTracker.getVersion());
            twinUpdate.setSessionId(twinTracker.getSessionId());
            // JMX Metrics
            updateCounter(MetricRegistry.name(twinRequest.location, twinRequest.getKey(), TWIN_RESPONSE_SENT));
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

    private void updateCounter(String counterName) {
        final Counter counter = metrics.counter(counterName);
        counter.inc();
    }

    public static String generateTracingOperationKey(String location, String key) {
        return location != null ? key + "@" + location : key;
    }

    private synchronized TwinUpdate getTwinUpdateFromUpdatedObj(byte[] updatedObj, SessionKey sessionKey) {
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
                String tracingOperationKey = generateTracingOperationKey(sessionKey.location, sessionKey.key);
                Span span = tracer.buildSpan(tracingOperationKey).start();
                byte[] objInBytes = objectMapper.writeValueAsBytes(obj);
                TwinUpdate twinUpdate = getTwinUpdateFromUpdatedObj(objInBytes, sessionKey);
                if (twinUpdate != null) {
                    TracingInfoCarrier.updateTracingMetadata(AbstractTwinPublisher.this.tracer, span, twinUpdate::addTracingInfo);
                    // Send update to local subscriber and on sink path.
                    span.setTag(TAG_TWIN_SINK, true);
                    if (sessionKey.location != null) {
                        span.setTag(TAG_LOCATION, sessionKey.location);
                    }
                    span.setTag(TAG_VERSION, twinUpdate.getVersion());
                    span.setTag(TAG_SESSION_ID, twinUpdate.getSessionId());
                    handleSinkUpdate(twinUpdate);
                    String sinkUpdateMetricName = sessionKey.location != null ?
                            MetricRegistry.name(sessionKey.location, sessionKey.key, SINK_UPDATE_SENT) :
                            MetricRegistry.name(sessionKey.key, SINK_UPDATE_SENT);
                    localTwinSubscriber.accept(twinUpdate);
                    // JMX Metrics
                    updateCounter(sinkUpdateMetricName);
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
