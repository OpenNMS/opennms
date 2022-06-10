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

package org.opennms.core.ipc.twin.kafka.common;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerRunner implements Runnable, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerRunner.class);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final KafkaConsumer<String, byte[]> consumer;

    private final Consumer<ConsumerRecord<String, byte[]>> handler;

    private final Thread thread;

    public KafkaConsumerRunner(final KafkaConsumer<String, byte[]> consumer,
                               final Consumer<ConsumerRecord<String, byte[]>> handler,
                               final String name) {
        this.consumer = Objects.requireNonNull(consumer);
        this.handler = Objects.requireNonNull(handler);

        this.consumer.commitSync();

        this.thread = new Thread(this, "kafka-consumer:" + name);
        this.thread.start();
    }

    public void close() {
        this.closed.set(true);
        this.consumer.wakeup();

        try {
            this.thread.join();
        } catch (InterruptedException e) {
            LOG.error("Consumer thread interrupted", e);
        }
    }

    @Override
    public void run() {
        Logging.putPrefix("twin");

        try {
            while (!this.closed.get()) {
                for (final var record : this.consumer.poll(Duration.ofMillis(100))) {
                    this.handler.accept(record);
                }
            }
        } catch (final WakeupException e) {
            // Ignored
        } finally {
            this.consumer.unsubscribe();
            this.consumer.close();
        }
    }
}
