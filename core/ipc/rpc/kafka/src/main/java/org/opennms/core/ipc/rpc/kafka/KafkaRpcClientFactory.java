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

import java.util.Arrays;
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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;
import org.opennms.core.logging.Logging;
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
    private static final long TIMEOUT_FOR_KAFKA_RPC = 30000;
    static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka";
    static final String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";
    private String location;
    private KafkaProducer<String, byte[]> producer;
    private final Properties kafkaConfig = new Properties();
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-consumer-rpc-client-%d").build();
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);
    private final Map<String, CompletableFuture<String>> rpcResponseMap = new ConcurrentHashMap<>();
    private final Map<String, KafkaConsumerRunner> consumerMap = new ConcurrentHashMap<>();

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
                // Wrap an empty future for response and add it to response map.
                final CompletableFuture<String> responseFuture = new CompletableFuture<>();
                rpcResponseMap.put(rpcId, responseFuture);
                RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.newBuilder()
                                                            .setRpcId(rpcId)
                                                            .setRpcContent(ByteString.copyFromUtf8(marshalledRequest))
                                                            .build();

                final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                        rpcMessage.toByteArray());
                producer.send(record);
                LOG.debug("RPC Request {} sent to minion at location {}", request.toString(), request.getLocation());
                final JmsQueueNameFactory topicNames = new JmsQueueNameFactory("rpc-response", module.getId());
                String responseTopicName = topicNames.getName();
                if (consumerMap.get(responseTopicName) == null) {
                    KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
                    KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer, responseTopicName);
                    consumerMap.put(responseTopicName, kafkaConsumerRunner);
                    executor.execute(kafkaConsumerRunner);
                }
                String response = null;
                long timeout = request.getTimeToLiveMs() != null ? request.getTimeToLiveMs().longValue()
                        : TIMEOUT_FOR_KAFKA_RPC;
                final CompletableFuture<T> rpcResponseFuture = new CompletableFuture<>();
                try {
                    response = responseFuture.get(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    LOG.error(" RPC Request {} interrupted", request.toString(), e);
                    rpcResponseFuture.completeExceptionally(e);
                } catch (ExecutionException e1) {
                    LOG.error(" RPC Request {} exception while executing ", request.toString(), e1);
                    rpcResponseFuture.completeExceptionally(e1);
                } catch (TimeoutException e2) {
                    LOG.error(" RPC Request {} timed out, no response from minion", request.toString(), e2);
                    rpcResponseFuture.completeExceptionally(e2);
                }
                T rpcResponse = null;
                if (response != null) {
                    rpcResponse = module.unmarshalResponse(response);
                    rpcResponseFuture.complete(rpcResponse);
                    LOG.debug("RPC Request {} received response {} ", request.toString(), rpcResponse.toString());
                }
                Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
                return rpcResponseFuture;
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
    }

    private class KafkaConsumerRunner implements Runnable {
        private final KafkaConsumer<String, byte[]> consumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final String topic;

        public KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer, String topic) {
            this.consumer = consumer;
            this.topic = topic;
        }

        @Override
        public void run() {
            try {
                consumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(100);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.parseFrom(record.value());
                        // Get future from rpc Id and complete future.
                        CompletableFuture<String> future = rpcResponseMap.get(rpcMessage.getRpcId());
                        future.complete(rpcMessage.getRpcContent().toStringUtf8());
                        // completing response is enough.
                        // Need to remove from map here as there is chance that waiting thread may have closed in a delayed response.
                        rpcResponseMap.remove(rpcMessage.getRpcId());
                    }
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) {
                    throw e;
                }
            } catch (InvalidProtocolBufferException e) {
                // No way to correlate this with request, will end up as Timeout exception
                LOG.error(" Error while parsing response", e);
     
            } finally {
                consumer.close();
            }
            
        }

        public void stop() {
            closed.set(true);
            consumer.wakeup();
        }

    }

    public void stop() {
       consumerMap.forEach((topic, consumer) -> {
           consumer.stop();
       });
    }

}
