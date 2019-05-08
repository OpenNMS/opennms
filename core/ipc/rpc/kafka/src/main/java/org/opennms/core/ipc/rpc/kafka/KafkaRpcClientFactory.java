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

import static org.opennms.core.ipc.rpc.kafka.KafkaRpcConstants.DEFAULT_TTL;
import static org.opennms.core.ipc.rpc.kafka.KafkaRpcConstants.MAX_BUFFER_SIZE;
import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;
import static org.opennms.core.tracing.api.TracerConstants.TAG_TIMEOUT;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.OnmsKafkaConfigProvider;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

/**
 * This Client Factory runs on OpenNMS. Whenever a Client receives an RPC request, it generates a unique rpcId and
 * initiates a response handler unique to the rpcId. It also starts a consumer thread for the specific RPC module if it
 * doesn't exist yet.
 * <p>
 * The client also expands the buffer into chunks if it is larger than the configured buffer size. The client then sends
 * the request to Kafka. If it is directed RPC (to a specific minion) it sends the request to all partitions there by to
 * all consumers (minions).
 * <p>
 * Consumer thread (one for each module) will receive the response and send it to a response handler which will return
 * the response.
 * <p>
 * Timeout tracker thread will fetch the response handler from it's delay queue and send a timeout response if it is not
 * finished already.
 */
public class KafkaRpcClientFactory implements RpcClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaRpcClientFactory.class);
    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();
    private String location;
    private KafkaProducer<String, byte[]> producer;
    private final Properties kafkaConfig = new Properties();
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-client-kafka-consumer-%d")
            .build();
    private final ThreadFactory timerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-client-timeout-tracker-%d")
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
    private final ExecutorService timerExecutor = Executors.newSingleThreadExecutor(timerThreadFactory);
    private final Map<String, ResponseCallback> rpcResponseMap = new ConcurrentHashMap<>();
    private KafkaConsumerRunner kafkaConsumerRunner;
    private DelayQueue<ResponseCallback> delayQueue = new DelayQueue<>();
    // Used to cache responses when large message are involved.
    private Map<String, ByteString> messageCache = new ConcurrentHashMap<>();
    private MetricRegistry metrics = new MetricRegistry();
    private JmxReporter metricsReporter = null;


    @Autowired
    private TracerRegistry tracerRegistry;
    private Tracer tracer;

    @Override
    public <S extends RpcRequest, T extends RpcResponse> RpcClient<S, T> getClient(RpcModule<S, T> module) {
        return new RpcClient<S, T>() {

            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(location)) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }
                Span span = tracer.buildSpan(module.getId()).start();
                final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(
                        KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME, module.getId(), request.getLocation());
                String requestTopic = topicNameFactory.getName();
                String marshalRequest = module.marshalRequest(request);
                request.getTracingInfo().forEach(span::setTag);
                // Generate RPC Id for every request to track request/response.
                String rpcId = UUID.randomUUID().toString();
                span.setTag(TAG_LOCATION, request.getLocation());
                if(request.getSystemId() != null) {
                    span.setTag(TAG_SYSTEM_ID, request.getSystemId());
                }
                // Calculate timeout based on ttl and default timeout.
                Long ttl = request.getTimeToLiveMs();
                ttl = (ttl != null && ttl > 0) ? ttl : DEFAULT_TTL;
                long expirationTime = System.currentTimeMillis() + ttl;
                // Create a future and add it to response handler which will complete the future when it receives callback.
                final CompletableFuture<T> future = new CompletableFuture<>();
                final Map<String, String> loggingContext = Logging.getCopyOfContextMap();
                ResponseHandler<S, T> responseHandler = new ResponseHandler<S, T>(future, module, rpcId,
                        expirationTime, loggingContext, request.getLocation(), span);
                delayQueue.offer(responseHandler);
                rpcResponseMap.put(rpcId, responseHandler);
                kafkaConsumerRunner.startConsumingForModule(module.getId());
                byte[] messageInBytes = marshalRequest.getBytes();
                int totalChunks = IntMath.divide(messageInBytes.length, MAX_BUFFER_SIZE, RoundingMode.UP);
                RpcMessageProtos.RpcMessage.Builder builder = RpcMessageProtos.RpcMessage.newBuilder()
                        .setRpcId(rpcId)
                        .setSystemId(request.getSystemId() == null ? "" : request.getSystemId())
                        .setExpirationTime(expirationTime);
                // Divide the message in chunks and send each chunk as a different message with the same key.
                for (int chunk = 0; chunk < totalChunks; chunk++) {
                    // Calculate remaining bufferSize for each chunk.
                    int bufferSize = KafkaRpcConstants.getBufferSize(messageInBytes.length, MAX_BUFFER_SIZE, chunk);
                    ByteString byteString = ByteString.copyFrom(messageInBytes, chunk * MAX_BUFFER_SIZE, bufferSize);
                    int chunkNum = chunk;
                    // Add tracing info to message builder.
                    addTracingInfoToRpcMessage(span, builder);
                    //Add custom tags to Rpc Message
                    request.getTracingInfo().forEach((key, value) -> {
                        RpcMessageProtos.TracingInfo tracingInfo = RpcMessageProtos.TracingInfo.newBuilder()
                                .setKey(key)
                                .setValue(value).build();
                        builder.addTracingInfo(tracingInfo);
                    });
                    // Build message.
                    RpcMessageProtos.RpcMessage rpcMessage =  builder.setRpcContent(byteString)
                            .setCurrentChunkNumber(chunk)
                            .setTotalChunks(totalChunks)
                            .build();
                    // Initialize kafka producer callback.
                    Callback sendCallback = (recordMetadata, e) -> {
                        if (e != null) {
                            RATE_LIMITED_LOG.error(" RPC request {} with id {} couldn't be sent to Kafka", request, rpcId, e);
                            future.completeExceptionally(e);
                        } else {
                            if(LOG.isTraceEnabled()) {
                                LOG.trace("RPC Request {} with id {} chunk {} sent to minion at location {}", request, rpcId, chunkNum, request.getLocation());
                            }
                        }
                    };
                    if (request.getSystemId() != null) {
                        // For directed RPCs, send request to all partitions (consumers),
                        // as it is reasonable to assume that partitions >= consumers(number of minions at location).
                        List<PartitionInfo> partitionInfo = producer.partitionsFor(requestTopic);
                        partitionInfo.forEach(partition -> {
                            // Use rpc Id as key.
                            final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                                    partition.partition(), rpcId, rpcMessage.toByteArray());
                            producer.send(record, sendCallback);
                        });
                    } else {
                        // Use rpc Id as key.
                        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                                rpcId, rpcMessage.toByteArray());
                        producer.send(record, sendCallback);
                    }
                }
                final Meter requestSentMeter = metrics.meter(MetricRegistry.name(request.getLocation(), module.getId(), RPC_COUNT));
                requestSentMeter.mark();
                final Histogram rpcRequestSize = metrics.histogram(MetricRegistry.name(request.getLocation(), module.getId(), RPC_REQUEST_SIZE));
                rpcRequestSize.update(messageInBytes.length);
                return future;
            }

            private void addTracingInfoToRpcMessage(Span span, RpcMessageProtos.RpcMessage.Builder builder) {
                TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
                tracer.inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
                if(tracingInfoCarrier.getTracingInfoMap().size() > 0) {
                    tracingInfoCarrier.getTracingInfoMap().forEach( (key, value) -> {
                        RpcMessageProtos.TracingInfo tracingInfo = RpcMessageProtos.TracingInfo.newBuilder()
                                .setKey(key)
                                .setValue(value).build();
                        builder.addTracingInfo(tracingInfo);
                    });
                }
            }
        };

    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }



    public void start() {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(RpcClientFactory.LOG_PREFIX)) {
            // Set the defaults
            kafkaConfig.clear();
            kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, SystemInfoUtils.getInstanceId());
            kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
            kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());

            // Find all of the system properties that start with 'org.opennms.core.ipc.rpc.kafka.' and add them to the config.
            KafkaConfigProvider kafkaConfigProvider = new OnmsKafkaConfigProvider(KafkaRpcConstants.KAFKA_CONFIG_SYS_PROP_PREFIX);
            kafkaConfig.putAll(kafkaConfigProvider.getProperties());
            producer = new KafkaProducer<>(kafkaConfig);
            LOG.info("initializing the Kafka producer with: {}", kafkaConfig);
            // Start consumer which handles all the responses.
            KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
            kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer);
            executor.execute(kafkaConsumerRunner);
            // Initialize metrics reporter.
            metricsReporter = JmxReporter.forRegistry(metrics).
                    inDomain(JMX_DOMAIN_RPC).build();
            metricsReporter.start();
            // Initialize tracer from tracer registry.
            tracerRegistry.init(SystemInfoUtils.getInstanceId());
            tracer = tracerRegistry.getTracer();
            LOG.info("started  kafka consumer with : {}", kafkaConfig);
            // Start a new thread which handles timeouts from delayQueue and calls response callback.
            timerExecutor.execute(() -> {
                while (true) {
                    try {
                        ResponseCallback responseCb = delayQueue.take();
                        if (!responseCb.isProcessed()) {
                            LOG.warn("RPC request with id {} timedout ", responseCb.getRpcId());
                            responseCb.sendResponse(null);
                        }
                    } catch (InterruptedException e) {
                        LOG.info("interrupted while waiting for an element from delayQueue", e);
                        break;
                    } catch (Exception e) {
                        LOG.warn("error while sending response from timeout handler", e);
                    }
                }
            });
            LOG.info("started timeout tracker");
        }
    }

    /**
     * Handle Response from kafka consumer and timeout thread.
     **/
    private class ResponseHandler<S extends RpcRequest, T extends RpcResponse> implements ResponseCallback {

        private final CompletableFuture<T> responseFuture;
        private final RpcModule<S, T> rpcModule;
        private final long expirationTime;
        private final String rpcId;
        private Map<String, String> loggingContext;
        private boolean isProcessed = false;
        private final String location;
        private Span span;
        private final Long requestCreationTime;
        private final Histogram rpcDuration;
        private final Meter failedMeter;
        private final Histogram responseSize;

        private ResponseHandler(CompletableFuture<T> responseFuture, RpcModule<S, T> rpcModule, String rpcId,
                               long timeout, Map<String, String> loggingContext, String location, Span span) {
            this.responseFuture = responseFuture;
            this.rpcModule = rpcModule;
            this.expirationTime = timeout;
            this.rpcId = rpcId;
            this.loggingContext = loggingContext;
            this.span = span;
            this.location = location;
            requestCreationTime = System.currentTimeMillis();
            rpcDuration = metrics.histogram(MetricRegistry.name(location, rpcModule.getId(), RPC_DURATION));
            failedMeter = metrics.meter(MetricRegistry.name(location, rpcModule.getId(), RPC_FAILED));
            responseSize = metrics.histogram(MetricRegistry.name(location, rpcModule.getId(), RPC_RESPONSE_SIZE));
        }

        @Override
        public void sendResponse(String message) {
            // restore Logging context on callback.
            try (MDCCloseable mdc = Logging.withContextMapCloseable(loggingContext)) {
                // When message is not null, it's called from kafka consumer otherwise it is from timeout tracker.
                if (message != null) {
                   T response = rpcModule.unmarshalResponse(message);
                    if (response.getErrorMessage() != null) {
                        responseFuture.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                        span.log(response.getErrorMessage());
                        failedMeter.mark();
                    } else {
                        responseFuture.complete(response);
                    }
                    isProcessed = true;
                    responseSize.update(message.getBytes().length);
                } else {
                    responseFuture.completeExceptionally(new RequestTimedOutException(new TimeoutException()));
                    span.setTag(TAG_TIMEOUT, "true");
                    failedMeter.mark();
                }
                rpcDuration.update(System.currentTimeMillis() - requestCreationTime);
                span.finish();
                rpcResponseMap.remove(rpcId);
                messageCache.remove(rpcId);
            } catch (Exception e) {
                LOG.warn("error while handling response for RPC request id {}", rpcId, e);
            }
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

        @Override
        public boolean isProcessed() {
            return isProcessed;
        }

        @Override
        public String getRpcId() {
            return rpcId;
        }

    }

    /**
     * Kafka Consumer thread that receives response from minion and send it to response handler.
     */
    private class KafkaConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> consumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final Set<String> moduleIdsForTopics = new HashSet<>();
        private final Set<String> topics = new HashSet<>();
        private final AtomicBoolean topicAdded = new AtomicBoolean(false);
        private final CountDownLatch firstTopicAdded = new CountDownLatch(1);

        private KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
            if (topics.isEmpty()) {
                // kafka consumer needs to subscribe to at least one topic for the consumer.poll to work.
                while (!topicAdded.get()) {
                    try {
                        firstTopicAdded.await(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        LOG.info("Interrupted before first topic was added. Terminating Kafka RPC consumer thread.");
                        return;
                    }
                }
                LOG.info("First topic is added, consumer will be started.");
            }
            while (!closed.get()) {
                if (topicAdded.get()) {
                    synchronized (topics) {
                        // Topic subscriptions are not incremental. This list will replace the current assignment (if there is one).
                        LOG.info("Subscribing Kafka RPC consumer to topics named: {}", topics);
                        consumer.subscribe(topics);
                        topicAdded.set(false);
                    }
                }
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        // Get Response callback from key and send rpc content to callback.
                        ResponseCallback responseCb = rpcResponseMap.get(record.key());
                        if (responseCb != null) {
                            RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage
                                    .parseFrom(record.value());
                            ByteString rpcContent = rpcMessage.getRpcContent();
                            // For larger messages which get split into multiple chunks, cache them until all of them arrive.
                            if (rpcMessage.getTotalChunks() > 1) {
                                String rpcId = rpcMessage.getRpcId();
                                ByteString byteString = messageCache.get(rpcId);
                                if (byteString != null) {
                                    messageCache.put(rpcId, byteString.concat(rpcMessage.getRpcContent()));
                                } else {
                                    messageCache.put(rpcId, rpcMessage.getRpcContent());
                                }
                                if (rpcMessage.getTotalChunks() != rpcMessage.getCurrentChunkNumber() + 1) {
                                    continue;
                                }
                                rpcContent = messageCache.get(rpcId);
                            }
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Received RPC response for id {}",  rpcMessage.getRpcId());
                            }
                            responseCb.sendResponse(rpcContent.toStringUtf8());
                        } else {
                            LOG.warn("Received a response for request with ID:{}, but no outstanding request was found with this id, The request may have timed out.",
                                    record.key());
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOG.error("error while parsing response", e);
                } catch (WakeupException e) {
                    LOG.info(" consumer got wakeup exception, closed = {} ", closed.get(), e);
                }
            }
            // Close consumer when while loop is closed.
            consumer.close();
        }

        public void stop() {
            closed.set(true);
            consumer.wakeup();
        }

        private void startConsumingForModule(String moduleId) {
            if (moduleIdsForTopics.contains(moduleId)) {
                return;
            }
            moduleIdsForTopics.add(moduleId);
            final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME, moduleId);
            synchronized (topics) {
                if (topics.add(topicNameFactory.getName())) {
                    topicAdded.set(true);
                    firstTopicAdded.countDown();
                    consumer.wakeup();
                }
            }
        }

    }


    public void stop() {
        LOG.info("stop kafka consumer runner");
        if (metricsReporter != null) {
            metricsReporter.close();
        }
        kafkaConsumerRunner.stop();
        executor.shutdown();
        timerExecutor.shutdown();
    }



}
