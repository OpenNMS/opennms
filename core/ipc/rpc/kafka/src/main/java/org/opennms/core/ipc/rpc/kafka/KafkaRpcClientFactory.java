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
import java.util.concurrent.ExecutionException;
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
    private static final String TIMEOUT_FOR_KAFKA_RPC = 
            System.getProperty("org.opennms.core.ipc.rpc.kafka.ttl","30000");
    private static final String TOPICS_METADATA_MAX_AGE = 
            System.getProperty("org.opennms.core.ipc.rpc.kafka.metadata.max.age.ms", "30000");
    private String location;
    private KafkaProducer<String, byte[]> producer;
    private final Properties kafkaConfig = new Properties();
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-consumer-rpc-client-%d").build();
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);
    private final Map<String, CompletableFuture<String>> rpcMessageMap = new ConcurrentHashMap<>();
    private KafkaConsumerRunner kafkaConsumerRunner;

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
                // Wrap an empty future for response message and add it to map.
                final CompletableFuture<String> messageFuture = new CompletableFuture<>();
                rpcMessageMap.put(rpcId, messageFuture);
                // Create consumer for response handling before sending any request

                if (request.getSystemId() != null) {
                    // For directed RPCs, so send request to all partitions (consumers), 
                    // this forces partitions >= max(number of minions at location)
                    RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.newBuilder()
                            .setRpcId(rpcId)
                            .setSystemId(request.getSystemId())
                            .setRpcContent(ByteString.copyFromUtf8(marshalledRequest))
                            .build();
                    List<PartitionInfo> partitionInfo = producer.partitionsFor(requestTopic);
                    partitionInfo.stream().forEach(partition -> {
                        //Use systemId as key
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
                LOG.debug("RPC Request {} sent to minion at location {}", request.toString(), request.getLocation());
                long metadataRefreshRate = Long.parseLong(TOPICS_METADATA_MAX_AGE);
                long ttl = request.getTimeToLiveMs() != null ? request.getTimeToLiveMs().longValue() : 0;
                // timeout for RPC > metadataRefreshRate, add default timeout to metadata refresh rate.
                long timeout = metadataRefreshRate + Long.parseLong(TIMEOUT_FOR_KAFKA_RPC);
                // ttl should be > default timeout otherwise use default.
                timeout = ttl < timeout ? timeout : ttl ;
                final CompletableFuture<T> future = new CompletableFuture<>();
                ResponseAndTimeOutHandler<S, T> timeoutHandler = new ResponseAndTimeOutHandler<S, T>(future,
                        messageFuture, rpcId, timeout, module);
                executor.execute(timeoutHandler);
                return future;
            }
        };

    }
    
    /** Handle Response and timeout in a separate thread. This is short lived thread for every request. **/
    private class ResponseAndTimeOutHandler<S extends RpcRequest, T extends RpcResponse> implements Runnable {

        private final CompletableFuture<T> responseFuture;
        private final CompletableFuture<String> messageFuture;
        private final String rpcId;
        private final long timeout;
        private final RpcModule<S, T> rpcModule;

        public ResponseAndTimeOutHandler(CompletableFuture<T> responseFuture, CompletableFuture<String> messageFuture,
                String requestId, long timeout, RpcModule<S, T> rpcModule) {
            this.responseFuture = responseFuture;
            this.messageFuture = messageFuture;
            this.rpcId = requestId;
            this.timeout = timeout;
            this.rpcModule = rpcModule;
        }

        @Override
        public void run() {
            String message = null;
            try {
                message = messageFuture.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                responseFuture.completeExceptionally(e);
            } catch (ExecutionException e) {
                responseFuture.completeExceptionally(e);
            } catch (TimeoutException e) {
                responseFuture.completeExceptionally(new RequestTimedOutException(e));
            } finally {
                rpcMessageMap.remove(rpcId);
            }
            T response = null;
            if (message != null) {
                response = rpcModule.unmarshalResponse(message);
                if (response.getErrorMessage() != null) {
                    responseFuture.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                } else {
                    responseFuture.complete(response);
                }
            }
        }

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

        // Find all of the system properties that start with 'org.opennms.core.ipc.rpc.kafka.'
        // and add them to the config. See https://kafka.apache.org/10/documentation.html#newconsumerconfigs
        // for the list of supported properties
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
                // subscribe to topics with "rpc-response" as part of topic name.
                Pattern pattern = Pattern.compile(".*rpc-response.*");
                consumer.subscribe(pattern);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.parseFrom(record.value());
                        // Get future from rpc Id and complete future.
                        CompletableFuture<String> future = rpcMessageMap.get(rpcMessage.getRpcId());
                        if (future != null) {
                            future.complete(rpcMessage.getRpcContent().toStringUtf8());
                            LOG.debug("Received response {}", rpcMessage.getRpcContent().toStringUtf8());
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
