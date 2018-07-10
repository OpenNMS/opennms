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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;
import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class KafkaRpcClientFactory implements RpcClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaRpcClientFactory.class);
    public static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka";
    public static final String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";
    private static final String TIMEOUT_FOR_KAFKA_RPC = System
            .getProperty(String.format("%sttl", KAFKA_CONFIG_SYS_PROP_PREFIX), "30000");
    private static final String TOPICS_METADATA_MAX_AGE = System
            .getProperty(String.format("%smetadata.max.age.ms", KAFKA_CONFIG_SYS_PROP_PREFIX), "30000");
    private String location;
    private KafkaProducer<String, byte[]> producer;
    private final Properties kafkaConfig = new Properties();
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-consumer-rpc-client-%d")
                                                    .build();
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);
    private final Map<String, ResponseCallback> rpcResponseMap = new ConcurrentHashMap<>();
    private KafkaConsumerRunner kafkaConsumerRunner;
    private DelayQueue<ResponseCallback> delayQueue = new DelayQueue<>();

    @Override
    public <S extends RpcRequest, T extends RpcResponse> RpcClient<S, T> getClient(RpcModule<S, T> module) {
        return new RpcClient<S, T>() {

            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(location)) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }
                final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory("rpc-request", module.getId(),
                        request.getLocation());
                String requestTopic = topicNameFactory.getName();
                String marshalledRequest = module.marshalRequest(request);
                // Generate RPC Id for every request to track request/response.
                String rpcId = UUID.randomUUID().toString();
                // Calculate timeout based on ttl and default timeouts.
                long metadataRefreshRate = Long.parseLong(TOPICS_METADATA_MAX_AGE);
                long ttl = request.getTimeToLiveMs() != null ? request.getTimeToLiveMs().longValue() : 0;
                // timeout for RPC > metadataRefreshRate, add default timeout to metadata refresh rate.
                long timeout = metadataRefreshRate + Long.parseLong(TIMEOUT_FOR_KAFKA_RPC);
                // ttl should be > default timeout otherwise use default.
                timeout = Math.max(ttl, timeout);
                // Create a future and add it to response handler which will complete the future when it receives callback.
                final CompletableFuture<T> future = new CompletableFuture<>();
                ResponseHandler<S, T> responseHandler = new ResponseHandler<S, T>(future, module, rpcId,
                        System.currentTimeMillis() + timeout);
                delayQueue.offer(responseHandler);
                rpcResponseMap.put(rpcId, responseHandler);
                if (request.getSystemId() != null) {
                    // For directed RPCs, send request to all partitions (consumers),
                    // this forces partitions >= max(number of minions at location)
                    RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.newBuilder()
                                                                 .setRpcId(rpcId)
                                                                 .setSystemId(request.getSystemId())
                                                                 .setRpcContent(ByteString.copyFromUtf8(marshalledRequest))
                                                                 .build();
                    List<PartitionInfo> partitionInfo = producer.partitionsFor(requestTopic);
                    partitionInfo.stream().forEach(partition -> {
                        // Use systemId as key.
                        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                                partition.partition(), request.getSystemId(), rpcMessage.toByteArray());
                        producer.send(record);
                    });
                } else {
                    RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.newBuilder()
                                                                .setRpcId(rpcId)
                                                                .setRpcContent(ByteString.copyFromUtf8(marshalledRequest))
                                                                .build();
                    final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                            rpcMessage.toByteArray());
                    producer.send(record);
                }
                LOG.debug("RPC Request {} sent to minion at location {}", request, request.getLocation());
                return future;
            }
        };

    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void start() {
        // Set the defaults
        kafkaConfig.clear();
        kafkaConfig.put("group.id", SystemInfoUtils.getInstanceId());
        kafkaConfig.put("enable.auto.commit", "true");
        kafkaConfig.put("key.deserializer", StringDeserializer.class.getCanonicalName());
        kafkaConfig.put("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put("auto.commit.interval.ms", "1000");
        kafkaConfig.put("key.serializer", StringSerializer.class.getCanonicalName());
        kafkaConfig.put("value.serializer", ByteArraySerializer.class.getCanonicalName());
        // This is needed to refresh topics metadata so that topics are added dynamically to consumer.
        kafkaConfig.put("metadata.max.age.ms", TOPICS_METADATA_MAX_AGE);
        kafkaConfig.put("auto.offset.reset", "earliest");

        // Find all of the system properties that start with 'org.opennms.core.ipc.rpc.kafka.' and add them to the config. 
        // See https://kafka.apache.org/10/documentation.html#newconsumerconfigs for the list of supported properties
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null || !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String) keyAsObject;

            if (key.length() > KAFKA_CONFIG_SYS_PROP_PREFIX.length() && key.startsWith(KAFKA_CONFIG_SYS_PROP_PREFIX)) {
                final String kafkaConfigKey = key.substring(KAFKA_CONFIG_SYS_PROP_PREFIX.length());
                kafkaConfig.put(kafkaConfigKey, entry.getValue());
            }
        }
        producer = new KafkaProducer<>(kafkaConfig);
        // Start consumer which handles all the responses.
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
        kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer);
        executor.execute(kafkaConsumerRunner);
        //Start a new thread which handles timeouts from delayQueue and calls response callback.
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-client-timeout-tracker-%d")
                .build();
        Executors.newSingleThreadExecutor(threadFactory).execute(() -> {
            while (true) {
                try {
                    ResponseCallback responseCb = delayQueue.take();
                    responseCb.sendResponse(null);
                } catch (InterruptedException e) {
                    LOG.info("exited while waiting for an element from delayQueue {}", e);
                } catch (Exception e) {
                    LOG.warn("error while sending response from timeout handler {}", e);
                }
            }
        });
    }

    /** Handle Response from kafka consumer and timeout thread **/
    private class ResponseHandler<S extends RpcRequest, T extends RpcResponse> implements ResponseCallback {

        private final CompletableFuture<T> responseFuture;
        private final RpcModule<S, T> rpcModule;
        private final long expirationTime;
        private final String rpcId;
 
        public ResponseHandler(CompletableFuture<T> responseFuture, RpcModule<S, T> rpcModule, String rpcId,
                long timeout) {
            this.responseFuture = responseFuture;
            this.rpcModule = rpcModule;
            this.expirationTime = timeout;
            this.rpcId = rpcId;
        }

        @Override
        public void sendResponse(String message) {
            T response = null;
            // When message is not null, it's called from kafka consumer otherwise it is from timeout tracker.
            if (message != null) {
                response = rpcModule.unmarshalResponse(message);
                if (response.getErrorMessage() != null) {
                    responseFuture.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                } else {
                    responseFuture.complete(response);
                }
            } else {
                responseFuture.completeExceptionally(new RequestTimedOutException(new TimeoutException()));
            }
            rpcResponseMap.remove(rpcId);
        }

        @Override
        public int compareTo(Delayed other) {
            long myDelay = getDelay(TimeUnit.MILLISECONDS);
            long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
            if (myDelay < otherDelay)
                return -1;
            if (myDelay == otherDelay)
                return 0;
            return 1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long now = System.currentTimeMillis();
            return unit.convert(expirationTime - now, TimeUnit.MILLISECONDS);
        }

    }

    private class KafkaConsumerRunner implements Runnable {
        private final KafkaConsumer<String, byte[]> consumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
                // subscribe to topics with "{OpenNMSInstanceName}.rpc-response." as prefix.
                String topicName = new JmsQueueNameFactory("rpc-response","").getName();
                String regex = String.format("%s.*", topicName);
                Pattern pattern = Pattern.compile(regex);
                consumer.subscribe(pattern);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.parseFrom(record.value());
                        // Get Response callback from rpcId and send rpc content to callback.
                        ResponseCallback responseCb = rpcResponseMap.get(rpcMessage.getRpcId());
                        LOG.debug("Received response {}", rpcMessage.getRpcContent().toStringUtf8());
                        if (responseCb != null) {
                            responseCb.sendResponse(rpcMessage.getRpcContent().toStringUtf8());
                        } else {
                            LOG.warn("received Response for the request which was timedout",
                                    rpcMessage.getRpcContent().toStringUtf8());
                        }
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error("Error while parsing response", e);
            } finally {
                LOG.info("closing kafka consumer");
                consumer.close();
            }

        }

        public void stop() {
            closed.set(true);
            consumer.wakeup();
        }

    }

    public void stop() {
        LOG.info("stop kafka consumer runner");
        kafkaConsumerRunner.stop();
    }

}
