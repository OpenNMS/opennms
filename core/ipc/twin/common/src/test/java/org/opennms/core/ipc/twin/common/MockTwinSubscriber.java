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

import static org.opennms.core.ipc.twin.common.TwinApiIT.rpcResponseTopic;
import static org.opennms.core.ipc.twin.common.TwinApiIT.sinkTopic;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.distributed.core.api.MinionIdentity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MockTwinSubscriber extends AbstractTwinSubscriber {

    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();
    private KafkaProducer<String, byte[]> producer;
    private KafkaSinkConsumerRunner kafkaSinkConsumerRunner;
    private KafkaConsumerRunner kafkaConsumerRunner;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Executor executor = Executors.newCachedThreadPool();

    public void init() {
        // Listens to response from OpenNMS.
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
        kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConsumer);
        Executors.newSingleThreadExecutor().execute(kafkaConsumerRunner);
        KafkaConsumer<String, byte[]> kafkaConsumer2 = new KafkaConsumer<>(kafkaConfig);
        kafkaSinkConsumerRunner = new KafkaSinkConsumerRunner(kafkaConsumer2);
        Executors.newSingleThreadExecutor().execute(kafkaSinkConsumerRunner);
    }

    protected MockTwinSubscriber(Hashtable<String, Object> kafkaConfig, MinionIdentity minionIdentity) {
        super(minionIdentity);
        this.kafkaConfig.putAll(kafkaConfig);
        this.kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "MINION");
        producer = new KafkaProducer<>(kafkaConfig);
    }

    @Override
    protected void sendRpcRequest(TwinRequestBean twinRequest) {
        try {
            byte[] value = objectMapper.writeValueAsBytes(twinRequest);
            ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(TwinApiIT.rpcRequestTopic, twinRequest.getKey(), value);
            producer.send(producerRecord);
        } catch (JsonProcessingException e) {
            //Ignore
        }
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

                        try {
                            TwinResponseBean twinResponse = objectMapper.readValue(record.value(), TwinResponseBean.class);
                            accept(twinResponse);
                        } catch (IOException e) {
                            // Ignore
                        }
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
                        CompletableFuture.runAsync(() -> {
                            try {
                                TwinResponseBean twinResponse = objectMapper.readValue(record.value(), TwinResponseBean.class);
                                accept(twinResponse);
                            } catch (IOException e) {
                                // Ignore
                            }
                        }, executor);
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
        if (kafkaConsumerRunner != null) {
            kafkaConsumerRunner.close();
        }
        if (kafkaSinkConsumerRunner != null) {
            kafkaSinkConsumerRunner.close();
        }
    }


}
