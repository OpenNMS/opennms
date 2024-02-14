/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
