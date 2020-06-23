/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.rpc.kafka;

import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_RPC_FAILED;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaRpcConstants;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

/**
 * This Manager runs on Minion, A consumer thread will be started on each RPC module which handles the request and
 * executes it on rpc module and sends the response to Kafka. When the request is directed at specific minion
 * (request with system-id), minion executes the request only if system-id matches with minionId.
 */
public class KafkaRpcServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaRpcServerManager.class);
    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();
    private final Map<String, RpcModule<RpcRequest, RpcResponse>> registerdModules = new ConcurrentHashMap<>();
    private final Properties kafkaConfig = new Properties();
    private final KafkaConfigProvider kafkaConfigProvider;
    private KafkaProducer<String, byte[]> producer;
    private MinionIdentity minionIdentity;
    private Integer maxBufferSize = KafkaRpcConstants.MAX_BUFFER_SIZE_CONFIGURED;
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                                                       .setNameFormat("rpc-server-kafka-consumer-%d")
                                                       .build();
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);
    private Map<RpcModule<RpcRequest, RpcResponse>, KafkaConsumerRunner> rpcModuleConsumers = new ConcurrentHashMap<>();
    // cache to hold rpcId and ByteString when there are multiple chunks for the message.
    private Map<String, ByteString> messageCache = new ConcurrentHashMap<>();
    // Delay queue which caches rpcId and removes when rpcId reaches expiration time.
    private DelayQueue<RpcId> rpcIdQueue = new DelayQueue<>();
    private ExecutorService delayQueueExecutor = Executors.newSingleThreadExecutor();
    private Map<String, Integer> currentChunkCache = new ConcurrentHashMap<>();
    private final TracerRegistry tracerRegistry;

    public KafkaRpcServerManager(KafkaConfigProvider configProvider, MinionIdentity minionIdentity, TracerRegistry tracerRegistry) {
        this.kafkaConfigProvider = configProvider;
        this.minionIdentity = minionIdentity;
        this.tracerRegistry = tracerRegistry;
    }

    public void init() throws IOException {
        // group.id is mapped to minion location, so one of the minion executes the request.
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, minionIdentity.getLocation());
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        // Retrieve all of the properties from org.opennms.core.ipc.rpc.kafka.cfg
        kafkaConfig.putAll(kafkaConfigProvider.getProperties());
        LOG.info("initializing the Kafka producer with: {}", kafkaConfig);
        producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<String, byte[]>(kafkaConfig), KafkaProducer.class.getClassLoader());
        // Configurable cache config.
        maxBufferSize = KafkaRpcConstants.getMaxBufferSize(kafkaConfig);
        // Thread to expire RpcId from rpcIdQueue.
        delayQueueExecutor.execute(() -> {
            while(true) {
                try {
                    RpcId rpcId = rpcIdQueue.take();
                    messageCache.remove(rpcId.getRpcId());
                    currentChunkCache.remove(rpcId.getRpcId());
                } catch (InterruptedException e) {
                    LOG.error("Delay Queue has been interrupted ", e);
                    break;
                }
            }
        });
        tracerRegistry.init(minionIdentity.getLocation() + "@" + minionIdentity.getId());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            if (registerdModules.containsKey(rpcModule.getId())) {
                LOG.warn(" {} module is already registered", rpcModule.getId());
            } else {
                registerdModules.put(rpcModule.getId(), rpcModule);
                startConsumerForModule(rpcModule);
            }
        }
    }

    protected void startConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME, rpcModule.getId(),
                minionIdentity.getLocation());
        KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaConsumer.class.getClassLoader());
        KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(rpcModule, consumer, topicNameFactory.getName());
        executor.execute(kafkaConsumerRunner);
        LOG.info("started kafka consumer for module : {}", rpcModule.getId());
        rpcModuleConsumers.put(rpcModule, kafkaConsumerRunner);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            registerdModules.remove(rpcModule.getId());
            stopConsumerForModule(rpcModule);
        }
    }

    protected void stopConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
        KafkaConsumerRunner kafkaConsumerRunner  = rpcModuleConsumers.remove(rpcModule);
        LOG.info("stopped kafka consumer for module : {}", rpcModule.getId());
        kafkaConsumerRunner.shutdown();
    }

    public void destroy() {
        if (producer != null) {
            producer.close();
        }
         messageCache.clear();
         executor.shutdown();
         delayQueueExecutor.shutdown();
    }


    class KafkaConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> consumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private String topic;
        private RpcModule<RpcRequest, RpcResponse> module;


        public KafkaConsumerRunner(RpcModule<RpcRequest, RpcResponse> rpcModule, KafkaConsumer<String, byte[]> consumer, String topic) {
            this.consumer = consumer;
            this.topic = topic;
            this.module = rpcModule;
        }

        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }

        @Override
        public void run() {
            try {
                consumer.subscribe(Arrays.asList(topic));
                LOG.info("subscribed to topic {}", topic);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(java.time.Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {  
                        try {
                            RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage
                                                                          .parseFrom(record.value());
                            String rpcId = rpcMessage.getRpcId();
                            long expirationTime = rpcMessage.getExpirationTime();
                            if (expirationTime < System.currentTimeMillis()) {
                                LOG.warn("ttl already expired for the request id = {}, won't process.", rpcMessage.getRpcId());
                                continue;
                            }
                            boolean hasSystemId = !Strings.isNullOrEmpty(rpcMessage.getSystemId());
                            String minionId = minionIdentity.getId();

                            if (hasSystemId && !(minionId.equals(rpcMessage.getSystemId()))) {
                                // directed RPC and not directed at this minion
                                continue;
                            }
                            if (hasSystemId) {
                                // directed RPC, there may be more than one request with same request Id, cache and allow only one.
                                boolean messageProcessed = handleDirectedRPC(rpcMessage);
                                if(messageProcessed) {
                                    continue;
                                }
                            }
                            ByteString rpcContent = rpcMessage.getRpcContent();
                            // For larger messages which get split into multiple chunks, cache them until all of them arrive.
                            if (rpcMessage.getTotalChunks() > 1) {
                                // Handle multiple chunks
                                boolean allChunksReceived = handleChunks(rpcMessage);
                                if(!allChunksReceived) {
                                    continue;
                                }
                                rpcContent = messageCache.get(rpcId);
                                //Remove rpcId from cache.
                                messageCache.remove(rpcId);
                                currentChunkCache.remove(rpcId);
                            }
                            //Build child span from rpcMessage and start minion span.
                            Tracer.SpanBuilder spanBuilder = buildSpanFromRpcMessage(rpcMessage);
                            Span minionSpan = spanBuilder.start();

                            RpcRequest request = module.unmarshalRequest(rpcContent.toStringUtf8());
                            setTagsOnMinion(rpcMessage, request, minionSpan);

                            CompletableFuture<RpcResponse> future = module.execute(request);
                            future.whenComplete((res, ex) -> {
                                final RpcResponse response;
                                if (ex != null) {
                                    // An exception occurred, store the exception in a new response
                                    LOG.warn("An error occured while executing a call in {}.", module.getId(), ex);
                                    response = module.createResponseWithException(ex);
                                    minionSpan.log(ex.getMessage());
                                    minionSpan.setTag(TAG_RPC_FAILED, "true");
                                } else {
                                    // No exception occurred, use the given response
                                    response = res;
                                }
                                // Finish minion Span
                                minionSpan.finish();
                                sendResponse(rpcId, response);
                            });
                        } catch (InvalidProtocolBufferException e) {
                             LOG.error("error while parsing the request", e);
                        }
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
            }
        }

        private void sendResponse(String rpcId, RpcResponse response) {
            try {
                final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME,
                        module.getId());
                final String responseAsString = module.marshalResponse(response);
                final byte[] messageInBytes = responseAsString.getBytes();
                int totalChunks = IntMath.divide(messageInBytes.length, maxBufferSize, RoundingMode.UP);

                // Divide the message in chunks and send each chunk as a different message with the same key.
                RpcMessageProtos.RpcMessage.Builder builder = RpcMessageProtos.RpcMessage.newBuilder()
                        .setRpcId(rpcId);
                builder.setTotalChunks(totalChunks);

                for (int chunk = 0; chunk < totalChunks; chunk++) {
                    // Calculate remaining bufferSize for each chunk.
                    int bufferSize = KafkaRpcConstants.getBufferSize(messageInBytes.length, maxBufferSize, chunk);
                    ByteString byteString = ByteString.copyFrom(messageInBytes, chunk * maxBufferSize, bufferSize);
                    RpcMessageProtos.RpcMessage rpcMessage = builder.setCurrentChunkNumber(chunk)
                            .setRpcContent(byteString)
                            .build();
                    sendMessageToKafka(rpcMessage, topicNameFactory.getName(), responseAsString);
                }
            } catch (Throwable t) {
                LOG.error("Marshalling response in RPC module {} failed.", module, t);
            }
        }

        void sendMessageToKafka(RpcMessageProtos.RpcMessage rpcMessage, String topic, String responseAsString) {
            String rpcId = rpcMessage.getRpcId();
            int chunkNum = rpcMessage.getCurrentChunkNumber();
            final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
                    topic, rpcMessage.getRpcId(), rpcMessage.toByteArray());

            producer.send(producerRecord, (recordMetadata, e) -> {
                if (e != null) {
                    RATE_LIMITED_LOG.error(" RPC response {} with id {} couldn't be sent to Kafka", rpcMessage, rpcId, e);
                } else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("request with id {} executed, sending response {}, chunk number {} ", rpcId, responseAsString, chunkNum);
                    }
                }
            });
        }

        private boolean handleDirectedRPC(RpcMessageProtos.RpcMessage rpcMessage) {
            String messageId = rpcMessage.getRpcId();
            // If this message has more than one chunk, chunk number should be added to messageId to make it unique.
            if (rpcMessage.getTotalChunks() > 1) {
                messageId = messageId + rpcMessage.getCurrentChunkNumber();
            }
            // If rpcId is already present in queue, no need to process it again.
            if (rpcIdQueue.contains(new RpcId(messageId, rpcMessage.getExpirationTime())) ||
                    rpcMessage.getExpirationTime() < System.currentTimeMillis()) {
                return true;
            } else {
                rpcIdQueue.offer(new RpcId(messageId, rpcMessage.getExpirationTime()));
            }
            return false;
        }

        private boolean handleChunks(RpcMessageProtos.RpcMessage rpcMessage) {
            // Avoid duplicate chunks. discard if chunk is repeated.
            String rpcId = rpcMessage.getRpcId();
            currentChunkCache.putIfAbsent(rpcId, 0);
            Integer chunkNumber = currentChunkCache.get(rpcId);
            if(chunkNumber != rpcMessage.getCurrentChunkNumber()) {
                LOG.debug("Expected chunk = {} but got chunk = {}, ignoring.", chunkNumber, rpcMessage.getCurrentChunkNumber());
                return false;
            }
            ByteString byteString = messageCache.get(rpcId);
            if (byteString != null) {
                messageCache.put(rpcId, byteString.concat(rpcMessage.getRpcContent()));
            } else {
                messageCache.put(rpcId, rpcMessage.getRpcContent());
            }
            currentChunkCache.put(rpcId, ++chunkNumber);

            return rpcMessage.getTotalChunks() == chunkNumber;
        }

        private Tracer.SpanBuilder buildSpanFromRpcMessage(RpcMessageProtos.RpcMessage rpcMessage) {
            // Initializer tracer and extract parent tracer context from TracingInfo
            final Tracer tracer = tracerRegistry.getTracer();
            Tracer.SpanBuilder spanBuilder;
            Map<String, String> tracingInfoMap = new HashMap<>();
            rpcMessage.getTracingInfoList().forEach(tracingInfo -> {
                tracingInfoMap.put(tracingInfo.getKey(), tracingInfo.getValue());
            });
            SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfoMap));
            if (context != null) {
                spanBuilder = tracer.buildSpan(module.getId()).asChildOf(context);
            } else {
                spanBuilder = tracer.buildSpan(module.getId());
            }
            return spanBuilder;
        }

        private void setTagsOnMinion(RpcMessageProtos.RpcMessage rpcMessage, RpcRequest request, Span minionSpan) {
            // Retrieve custom tags from rpcMessage and add them as tags.
            rpcMessage.getTracingInfoList().forEach(tracingInfo -> {
                minionSpan.setTag(tracingInfo.getKey(), tracingInfo.getValue());
            });
            // Set tags for minion span
            minionSpan.setTag(TAG_LOCATION, request.getLocation());
            if(request.getSystemId() != null) {
                minionSpan.setTag(TAG_SYSTEM_ID, request.getSystemId());
            }
        }


    }



    /**
     * RpcId is used to remove rpcId from DelayQueue after it reaches expirationTime.
     */
    private class RpcId implements Delayed {

        private final long expirationTime;

        private final String rpcId;

        public RpcId(String rpcId, long expirationTime) {
            this.rpcId = rpcId;
            this.expirationTime = expirationTime;
        }

        @Override
        public int compareTo(Delayed other) {
            long myDelay = getDelay(TimeUnit.MILLISECONDS);
            long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(myDelay, otherDelay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long now = System.currentTimeMillis();
            return unit.convert(expirationTime - now, TimeUnit.MILLISECONDS);
        }

        public String getRpcId() {
            return rpcId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RpcId that = (RpcId) o;
            return expirationTime == that.expirationTime &&
                    Objects.equals(rpcId, that.rpcId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expirationTime, rpcId);
        }
    }

    DelayQueue<RpcId> getRpcIdQueue() {
        return rpcIdQueue;
    }

    Map<RpcModule<RpcRequest, RpcResponse>, KafkaConsumerRunner> getRpcModuleConsumers() {
        return rpcModuleConsumers;
    }

    ExecutorService getExecutor() {
        return executor;
    }

    Properties getKafkaConfig() {
        return kafkaConfig;
    }

    Map<String, ByteString> getMessageCache() {
        return messageCache;
    }
}