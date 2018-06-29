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
import org.opennms.core.rpc.echo.EchoRpcModule;
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
    private final Map<String, CompletableFuture<String>> rpcMessageMap = new ConcurrentHashMap<>();
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
                // Wrap an empty future for response message and add it to map.
                final CompletableFuture<String> messageFuture = new CompletableFuture<>();
                rpcMessageMap.put(rpcId, messageFuture);
                // Create consumer for response handling before sending any request
                createKafkaConsumerForModule(module.getId());
                RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage.newBuilder()
                                                            .setRpcId(rpcId)
                                                            .setRpcContent(ByteString.copyFromUtf8(marshalledRequest))
                                                            .build();
                if (module.getId().equals(EchoRpcModule.RPC_MODULE_ID)) {
                    // Echo module needs directed RPCs, so send request to all partitions (consumers), 
                    // this forces partitions >= max(number of minions at location)
                    List<PartitionInfo> partitionInfo = producer.partitionsFor(requestTopic);
                    partitionInfo.stream().forEach(partition -> {
                        //Use systemId as key
                        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                                partition.partition(), request.getSystemId(), rpcMessage.toByteArray());
                        producer.send(record);
                    });
                } else {
                    final ProducerRecord<String, byte[]> record = new ProducerRecord<>(requestTopic,
                            rpcMessage.toByteArray());
                    producer.send(record);
                }
                LOG.debug("RPC Request {} sent to minion at location {}", request.toString(), request.getLocation());
                String message = null;
                long timeout = request.getTimeToLiveMs() != null ? request.getTimeToLiveMs().longValue()
                        : TIMEOUT_FOR_KAFKA_RPC;
                final CompletableFuture<T> future = new CompletableFuture<>();
                try {
                    message = messageFuture.get(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                } catch (ExecutionException e1) {
                    future.completeExceptionally(e1);
                } catch (TimeoutException e) {
                    future.completeExceptionally(new RequestTimedOutException(e));
                } finally {
                    rpcMessageMap.remove(rpcMessage.getRpcId());
                }
                T response = null;
                if (message != null) {
                    response = module.unmarshalResponse(message);
                    if (response.getErrorMessage() != null) {
                        future.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                    } else {
                        future.complete(response);
                    }
                }
                return future;
            }
        };

    }

    private synchronized void createKafkaConsumerForModule(String moduleId) {
        final JmsQueueNameFactory topicNames = new JmsQueueNameFactory("rpc-response", moduleId);
        String responseTopicName = topicNames.getName();
        if (consumerMap.get(responseTopicName) == null) {
            KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
            KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer, responseTopicName);
            consumerMap.put(responseTopicName, kafkaConsumerRunner);
            executor.execute(kafkaConsumerRunner);
            LOG.info("started consumer for {}", responseTopicName);
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
        private final String topic;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer, String topic) {
            this.consumer = consumer;
            this.topic = topic;
        }

        @Override
        public void run() {
            try {
                consumer.subscribe(Arrays.asList(topic));
                LOG.info("subscribed to topic {}", topic);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(100);
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
                LOG.info("closing consumer for topic {}", topic);
                consumer.close();
            }
            
        }

        public void stop() {
            LOG.info("stopping consumer for topic {} ", topic);
            closed.set(true);
            consumer.wakeup();
        }

    }

    public void stop() {
       consumerMap.forEach((topic, consumer) -> {
           consumer.stop();
       });
       consumerMap.clear();
    }

}
