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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

import static org.opennms.core.ipc.twin.common.AbstractTwinPublisher.TAG_PATCH;
import static org.opennms.core.ipc.twin.common.AbstractTwinPublisher.TAG_SESSION_ID;
import static org.opennms.core.ipc.twin.common.AbstractTwinPublisher.TAG_VERSION;

public abstract class AbstractTwinSubscriber implements TwinSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinSubscriber.class);
    protected static final String TAG_TWIN_RPC_REQUEST = "TwinRpcRequest";

    private final Identity identity;

    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TracerRegistry tracerRegistry;

    private final Tracer tracer;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("abstract-twin-subscriber-%d")
                    .build());

    protected AbstractTwinSubscriber(final Identity identity, TracerRegistry tracerRegistry) {
        this.identity = Objects.requireNonNull(identity);
        this.tracerRegistry = tracerRegistry;
        this.tracerRegistry.init(SystemInfoUtils.getInstanceId() + "@" + identity.getLocation() + "-" + identity.getId());
        this.tracer = this.tracerRegistry.getTracer();
    }

    protected abstract void sendRpcRequest(TwinRequest twinRequest);


    @Override
    public <T> Closeable subscribe(final String key, final Class<T> clazz, final Consumer<T> consumer) {
        if (this.executorService.isShutdown()) {
            throw new IllegalStateException("Subscriber is already closed");
        }

        final var subscription = this.subscriptions.computeIfAbsent(key, Subscription::new);
        return subscription.consume(clazz, consumer);
    }

    protected void accept(final TwinUpdate twinUpdate) {
        // Ignore update if not broadcast but foreign location
        if (twinUpdate.getLocation() != null && !twinUpdate.getLocation().equals(this.identity.getLocation())) {
            return;
        }

        // Ignore empty response
        if (twinUpdate.getObject() == null || twinUpdate.getSessionId() == null) {
            return;
        }
        String tracingOperationKey = twinUpdate.getLocation() != null ?
                twinUpdate.getKey() + "@" + twinUpdate.getLocation() : twinUpdate.getKey();
        Tracer.SpanBuilder spanBuilder = TracingInfoCarrier.buildSpanFromTracingMetadata(getTracer(),
                tracingOperationKey, twinUpdate.getTracingInfo(), References.FOLLOWS_FROM);
        // Consume in thread instead of using broker's callback thread.
        this.executorService.execute(() -> {
            final var subscription = this.subscriptions.computeIfAbsent(twinUpdate.getKey(), Subscription::new);

            try (Scope scope = spanBuilder.startActive(true)){
                addTracingTags(scope.span(), twinUpdate);
                subscription.update(twinUpdate);
            } catch (final IOException e) {
                LOG.error("Processing update failed: {}", twinUpdate.getKey(), e);
                subscription.request();
            }
        });
    }

    private void addTracingTags(Span span, TwinUpdate twinUpdate) {
        span.setTag(TAG_VERSION, twinUpdate.getVersion());
        span.setTag(TAG_SESSION_ID, twinUpdate.getSessionId());
        span.setTag(TAG_PATCH, twinUpdate.isPatch());
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
            if (!twinResponseProto.getTwinObject().isEmpty()) {
                twinUpdate.setObject(twinResponseProto.getTwinObject().toByteArray());
            }
            twinUpdate.setPatch(twinResponseProto.getIsPatchObject());
            twinUpdate.setVersion(twinResponseProto.getVersion());
            twinResponseProto.getTracingInfoMap().forEach(twinUpdate::addTracingInfo);
            return twinUpdate;
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Failed to parse response from proto", e);
            throw new RuntimeException(e);
        }
    }

    protected TwinRequestProto mapTwinRequestToProto(TwinRequest twinRequest) {
        TwinRequestProto.Builder builder = TwinRequestProto.newBuilder();
        builder.setConsumerKey(twinRequest.getKey())
               .setLocation(getIdentity().getLocation())
               .setSystemId(getIdentity().getId());
        twinRequest.getTracingInfo().forEach(builder::putTracingInfo);
        return builder.build();
    }

    public void close() throws IOException {
        this.executorService.shutdown();
        this.subscriptions.clear();
    }

    public Tracer getTracer() {
        return tracer;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    private static class Value {
        public final String sessionId;
        public final int version;

        public final JsonNode value;

        private Value(final String sessionId,
                      final int version,
                      final JsonNode value) {
            this.sessionId = Objects.requireNonNull(sessionId);
            this.version = version;
            this.value = Objects.requireNonNull(value);
        }
    }

    private class Subscription {
        private final String key;

        private final Set<Consumer<JsonNode>> consumers = Sets.newConcurrentHashSet();

        private Value value = null;

        /**
         * A tasked scheduled to retry an outstanding request.
         *
         * This is a one-shot task re-executing the request. The task will be scheduled when a request is emitted and
         * will be canceled when an update is accepted. Setting this to {@code null} shows that there is no outstanding
         * request.
         */
        private ScheduledFuture<?> retry = null;

        private Subscription(final String key) {
            this.key = Objects.requireNonNull(key);
        }

        /**
         * Consume the subscription.
         *
         * Adds a consumer to the subscription. Incoming values will be forwarded to the passed consumer until the
         * returned value is closed. If there is a value already available for the subscription, the consumer will be
         * called with this value immediately.
         *
         * @param clazz The class of the value to consume
         * @param consumer The consumer accepting the values
         * @param <T> The class of the value to consume
         * @return a Closable, stopping the consumption when closed
         */
        public synchronized <T> Closeable consume(final Class<T> clazz, final Consumer<T> consumer) {
            final Consumer<JsonNode> jsonConsumer = (json) -> {
                try {
                    // Deserialize to the final class
                    final var value = AbstractTwinSubscriber.this.objectMapper.treeToValue(json, clazz);

                    // Forward to typed consumer
                    consumer.accept(value);

                } catch (final Exception e) {
                    LOG.error("Processing twin update failed: {} as {}", this.key, clazz, e);
                }
            };

            if (this.value == null) {
                // Initially request value

                // Send request only if there is no ongoing request
                if (this.retry == null) {
                    this.request();
                }
            } else {
                // If value already exists, forward to consumer without requesting
                jsonConsumer.accept(this.value.value);
            }

            // Add the consumer to the subscription
            this.consumers.add(jsonConsumer);

            // Return the closable removing the consumer
            return () -> this.consumers.remove(jsonConsumer);
        }

        private synchronized void accept(final Value value) {
            Objects.requireNonNull(value);

            // Call all consumers if value has changed
            if (!(this.value != null && Objects.equals(this.value.value, value.value))) {
                this.consumers.forEach(c -> c.accept(value.value));
            }

            // Remember value
            this.value = value;
        }

        private synchronized void request() {
            // Send a request
            String tracingOperationKey = AbstractTwinSubscriber.this.identity.getLocation() != null ?
                    this.key + "@" + AbstractTwinSubscriber.this.identity.getLocation() : this.key;
            Span span = tracer.buildSpan(tracingOperationKey).start();
            final var request = new TwinRequest(this.key, AbstractTwinSubscriber.this.identity.getLocation());
            updateTracingTags(span, request);
            AbstractTwinSubscriber.this.sendRpcRequest(request);
            span.finish();
            // Schedule a retry
            this.retry = AbstractTwinSubscriber.this.executorService.schedule(this::request, 5, TimeUnit.SECONDS);
        }

        private void updateTracingTags(Span span, TwinRequest twinRequest) {
            TracingInfoCarrier.updateTracingMetadata(getTracer(), span, twinRequest::addTracingInfo);
            span.setTag(TAG_TWIN_RPC_REQUEST, true);
            span.setTag(TracerConstants.TAG_LOCATION, twinRequest.getLocation());
            span.setTag(TracerConstants.TAG_SYSTEM_ID, getIdentity().getId());
        }

        public synchronized void update(final TwinUpdate update) throws IOException {
            // Cancel outstanding retry
            if (this.retry != null) {
                this.retry.cancel(false);
                this.retry = null;
            }

            if (this.value == null || !Objects.equals(this.value.sessionId, update.getSessionId())) {
                // Either there was no previous known value or the session has restarted

                if (!update.isPatch()) {
                    this.accept(new Value(update.getSessionId(),
                                          update.getVersion(),
                                          AbstractTwinSubscriber.this.objectMapper.readTree(update.getObject())));
                } else {
                    this.request();
                }

            } else {
                // Same session

                // Ignore update if version is not advancing
                if (update.getVersion() <= this.value.version) {
                    return;
                }

                if (!update.isPatch()) {
                    this.accept(new Value(update.getSessionId(),
                                          update.getVersion(),
                                          AbstractTwinSubscriber.this.objectMapper.readTree(update.getObject())));
                } else {
                    if (update.getVersion() == this.value.version + 1) {
                        // Version advanced - apply path
                        try {
                            final var patchObj = AbstractTwinSubscriber.this.objectMapper.readTree(update.getObject());
                            final var patch = JsonPatch.fromJson(patchObj);

                            final var value = patch.apply(this.value.value);

                            this.accept(new Value(update.getSessionId(), update.getVersion(), value));
                        } catch (JsonPatchException e) {
                            throw new IOException("Unable to apply patch", e);
                        }

                    } else {
                        // Version jumped
                        this.request();
                    }
                }
            }
        }
    }

}
