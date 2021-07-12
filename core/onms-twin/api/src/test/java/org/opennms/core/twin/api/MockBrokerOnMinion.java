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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
import org.opennms.core.twin.subscriber.api.OnmsTwinSubscriber;
import org.opennms.core.twin.subscriber.api.TwinBrokerOnMinion;

public class MockBrokerOnMinion implements TwinBrokerOnMinion {

    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();
    private KafkaProducer<String, byte[]> producer;
    private Map<String, CompletableFuture<OnmsTwin>> futureMap = new ConcurrentHashMap<>();
    private OnmsTwinSubscriber.Subscriber subscriber;
    private KafkaSinkConsumerRunner kafkaSinkConsumerRunner;
    private KafkaConsumerRunner kafkaConsumerRunner;
    static String rpcRequestTopic = "OpenNMS-MINION-RPC-Request-onms-twin";
    static String rpcResponseTopic = "OpenNMS-MINION-RPC-Response-onms-twin";
    static String sinkTopic = "OpenNMS-MINION-Sink-onms-twin";

    public void init() {
        // Listens to response from OpenNMS.
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
        kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer);
        Executors.newSingleThreadExecutor().execute(kafkaConsumerRunner);
        KafkaConsumer<String, byte[]> kafkaConsumer2 = new KafkaConsumer<>(kafkaConfig);
        kafkaSinkConsumerRunner = new KafkaSinkConsumerRunner(kafkaConsumer2);
        Executors.newSingleThreadExecutor().execute(kafkaSinkConsumerRunner);
    }

    public MockBrokerOnMinion(Hashtable<String, Object> config) {
        kafkaConfig.putAll(config);
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "MINION");
        producer = new KafkaProducer<>(kafkaConfig);
    }

    @Override
    public CompletableFuture<OnmsTwin> sendRpcRequest(OnmsTwinRequest request) {
        CompletableFuture<OnmsTwin> future = new CompletableFuture<>();
        futureMap.put(request.getKey(), future);
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(rpcRequestTopic, request.getKey(), request.getLocation().getBytes(StandardCharsets.UTF_8));
        producer.send(producerRecord);
        return future;
    }

    @Override
    public void registerSinkUpdate(OnmsTwinSubscriber.Subscriber callback) {
        subscriber = callback;
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
                kafkaConsumer.subscribe(Arrays.asList(rpcResponseTopic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        OnmsTwin onmsTwin = MockBrokerOnOpenNMS.MarshallerForOnmsTwin.getOnmsTwinFromBytes(record.key(), record.value());
                        CompletableFuture<OnmsTwin> future = futureMap.get(record.key());
                        future.complete(onmsTwin);
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

    private class KafkaSinkConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> kafkaConsumer;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public KafkaSinkConsumerRunner(KafkaConsumer<String, byte[]> kafkaConsumer) {
            this.kafkaConsumer = kafkaConsumer;
        }

        @Override
        public void run() {
            try {

                kafkaConsumer.subscribe(Arrays.asList(sinkTopic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        OnmsTwin onmsTwin = MockBrokerOnOpenNMS.MarshallerForOnmsTwin.getOnmsTwinFromBytes(record.key(), record.value());
                        subscriber.subscribe(onmsTwin);
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
        kafkaSinkConsumerRunner.close();
    }

}
