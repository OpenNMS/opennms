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

package org.opennms.features.kafka.producer;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class KafkaConsumerTest {

    @Test
    public void testKafkaConsumerOnEvents() {

        Executor executor = Executors.newSingleThreadExecutor();
        KafkaConsumerRunner consumerRunner = new KafkaConsumerRunner();
        executor.execute(consumerRunner);
        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .untilAtomic(consumerRunner.getNumberOfEvents(), greaterThan(3));

    }

    private class KafkaConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private AtomicInteger numberOfEvents = new AtomicInteger(0);

        public AtomicInteger getNumberOfEvents() {
            return numberOfEvents;
        }

        @Override
        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", "kafka:9092");
            props.put("group.id", "opennms");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");

            KafkaConsumer consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("events"));
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record.key());
                    numberOfEvents.getAndIncrement();
                }
            }
        }

    }

}