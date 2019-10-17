/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.IntMath;
import com.google.protobuf.ByteString;

/**
 * Mocking Kafka Server Manager to push duplicate messages to kafka.
 */
public class MockKafkaServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(MockKafkaServerManager.class);

    private final KafkaConfigProvider kafkaConfigProvider;

    private final MinionIdentity minionIdentity;

    private KafkaProducer<String, byte[]> kafkaProducer;

    private KafkaConsumerRunner kafkaConsumerRunner;

    private Integer maxBufferSize = RpcKafkaLargeBufferIT.MAX_BUFFER_SIZE_CONFIGURED;

    private final Properties kafkaConfig = new Properties();

    public MockKafkaServerManager(KafkaConfigProvider kafkaConfigProvider, MinionIdentity minionIdentity) {
        this.kafkaConfigProvider = kafkaConfigProvider;
        this.minionIdentity = minionIdentity;
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
        kafkaProducer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<String, byte[]>(kafkaConfig), KafkaProducer.class.getClassLoader());

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            startConsumerForModule(rpcModule);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            stopConsumerForModule(rpcModule);
        }
    }

    private void stopConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
        LOG.info("stopped kafka consumer for module : {}", rpcModule.getId());
        kafkaConsumerRunner.shutdown();
    }

    private void startConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME, rpcModule.getId(),
                minionIdentity.getLocation());
        KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(kafkaConfig), KafkaConsumer.class.getClassLoader());
        kafkaConsumerRunner = new KafkaConsumerRunner(consumer, topicNameFactory.getName(), rpcModule);
        Executors.newSingleThreadExecutor().execute(kafkaConsumerRunner);
        LOG.info("started kafka consumer for module : {}", rpcModule.getId());
    }

    private class KafkaConsumerRunner implements Runnable {
        private final KafkaConsumer<String, byte[]> consumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private String topic;
        private RpcModule<RpcRequest, RpcResponse> module;

        public KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer, String topic, RpcModule<RpcRequest, RpcResponse> module) {
            this.consumer = consumer;
            this.topic = topic;
            this.module = module;
        }


        @Override
        public void run() {
            try {
                consumer.subscribe(Arrays.asList(topic));
                LOG.info("subscribed to topic {}", topic);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(java.time.Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        RpcMessageProtos.RpcMessage rpcMessage = RpcMessageProtos.RpcMessage
                                .parseFrom(record.value());
                        String rpcId = rpcMessage.getRpcId();
                        CompletableFuture<RpcResponse> future = module.execute(null);
                        future.whenComplete((res, ex) -> {
                            if (res != null) {
                                final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME,
                                        module.getId());
                                final String responseAsString = module.marshalResponse(res);
                                final byte[] messageInBytes = responseAsString.getBytes();
                                int totalChunks = IntMath.divide(messageInBytes.length, maxBufferSize, RoundingMode.UP);
                                // Divide the message in chunks and send each chunk as a different message with the same key.
                                RpcMessageProtos.RpcMessage.Builder builder = RpcMessageProtos.RpcMessage.newBuilder()
                                        .setRpcId(rpcId);
                                builder.setTotalChunks(totalChunks);
                                boolean repeatFirstChunk = true;
                                for (int chunk = 0; chunk < totalChunks;) {
                                    // Calculate remaining bufferSize for each chunk.
                                    int bufferSize = KafkaRpcConstants.getBufferSize(messageInBytes.length, maxBufferSize, chunk);
                                    ByteString byteString = ByteString.copyFrom(messageInBytes, chunk * maxBufferSize, bufferSize);
                                    RpcMessageProtos.RpcMessage rpcResponse = builder.setCurrentChunkNumber(chunk)
                                            .setRpcContent(byteString)
                                            .build();
                                    final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
                                            topicNameFactory.getName(), rpcId, rpcResponse.toByteArray());
                                    kafkaProducer.send(producerRecord);
                                    if(repeatFirstChunk) {
                                        repeatFirstChunk = false;
                                    } else {
                                        chunk++;
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (Throwable e) {
                //Ignore.
            }
        }
        void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
    }
}
