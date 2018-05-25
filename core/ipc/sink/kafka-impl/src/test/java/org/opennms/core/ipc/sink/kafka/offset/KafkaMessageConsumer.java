/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.offset;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private final String groupName = "OpenNMS";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String bootStrapServer;

    private KafkaMessageConsumerRunner consumerRunner;

    public KafkaMessageConsumer(String bootStrapServer) {
        this.bootStrapServer = bootStrapServer;
    }

    private class KafkaMessageConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<String, String> consumer;

        @Override
        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", getBootStrapServer());
            props.put("group.id", getGroupName());
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("USER_TOPIC"));
            LOGGER.info("subscribed to USER_TOPIC");
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    LOGGER.info(record.value());
                }
            }
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            if (consumer != null) {
                consumer.wakeup();
            }
        }
    }

    public String getBootStrapServer() {
        return bootStrapServer;
    }

    public void startConsumer() {
        consumerRunner = new KafkaMessageConsumerRunner();
        executor.execute(consumerRunner);
    }

    public void stopConsumer() {
        consumerRunner.shutdown();
    }

    public String getGroupName() {
        return groupName;
    }

}
