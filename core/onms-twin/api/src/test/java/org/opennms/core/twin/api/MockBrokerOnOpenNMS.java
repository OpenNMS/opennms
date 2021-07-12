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

package org.opennms.core.twin.api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Hashtable;
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
import org.opennms.core.twin.publisher.api.OnmsTwinPublisher;
import org.opennms.core.twin.publisher.api.TwinBrokerOnOpennms;

public class MockBrokerOnOpenNMS implements TwinBrokerOnOpennms {

    private OnmsTwinPublisher.RpcReceiver rpcReceiver;
    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();
    private KafkaProducer<String, byte[]> rpcResponseProducer;
    private KafkaConsumerRunner kafkaConsumerRunner;

    public MockBrokerOnOpenNMS(Hashtable<String, Object> config) {
        this.kafkaConfig.putAll(config);
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "OpenNMS");
    }

    public void init() {
        rpcResponseProducer = new KafkaProducer<String, byte[]>(kafkaConfig);
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
        kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer);
        Executors.newSingleThreadExecutor().execute(kafkaConsumerRunner);
    }

    @Override
    public void send(OnmsTwin onmsTwin) {
        // Publish it to broker.
        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(kafkaConfig);
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(MockBrokerOnMinion.sinkTopic, onmsTwin.getKey(), onmsTwin.getObjectValue());
        producer.send(producerRecord);

    }

    @Override
    public void register(OnmsTwinPublisher.RpcReceiver rpcReceiver) {
        this.rpcReceiver = rpcReceiver;
    }

    private class KafkaConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> kafkaConsumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public KafkaConsumerRunner(KafkaConsumer<String, byte[]> kafkaConsumer) {
            this.kafkaConsumer = kafkaConsumer;
        }

        @Override
        public void run() {
            try {
                kafkaConsumer.subscribe(Arrays.asList(MockBrokerOnMinion.rpcRequestTopic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        OnmsTwinRequest onmsTwinRequest = MarshallerForOnmsTwinRequest.getOnmsTwinFromBytes(record.key(), record.value());
                        CompletableFuture<OnmsTwin> future = rpcReceiver.rpcCallback(onmsTwinRequest);
                        future.whenComplete((response, ex) -> {
                            ProducerRecord<String, byte[]> producerRecord =
                                    new ProducerRecord<>(MockBrokerOnMinion.rpcResponseTopic, response.getKey(), response.getObjectValue());
                            rpcResponseProducer.send(producerRecord);
                        });
                    }
                }
            } catch (Exception e) {
                //Ignore
            }
        }

        public void close() {
            closed.set(true);
        }
    }

    public void destroy() {
        kafkaConsumerRunner.close();
    }

    static class MarshallerForOnmsTwin {

        public static OnmsTwin getOnmsTwinFromBytes(String key, byte[] value) {
            return new OnmsTwin() {
                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getLocation() {
                    return null;
                }

                @Override
                public int getVersion() {
                    return 0;
                }

                @Override
                public byte[] getObjectValue() {
                    return value;
                }
            };
        }
    }

    static class MarshallerForOnmsTwinRequest {

        public static OnmsTwinRequest getOnmsTwinFromBytes(String key, byte[] value) {
            return new OnmsTwinRequest() {
                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getLocation() {
                    return new String(value, StandardCharsets.UTF_8);
                }
            };
        }
    }
}
