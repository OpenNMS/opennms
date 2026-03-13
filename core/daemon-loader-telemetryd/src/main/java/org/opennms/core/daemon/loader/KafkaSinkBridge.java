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
package org.opennms.core.daemon.loader;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.ipc.sink.model.SinkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Bridges Kafka Sink topic consumption to the {@link TelemetryMessageConsumerManager}.
 *
 * <p>In the Delta-V architecture, Minion forwards telemetry data to per-queue Kafka Sink
 * topics (e.g., {@code OpenNMS.Sink.Telemetry-Netflow-5}, {@code OpenNMS.Sink.Telemetry-IPFIX}).
 * Each bridge instance consumes from one topic and dispatches to the consumer manager,
 * which delivers to the appropriate telemetry adapter.</p>
 *
 * <p>Configuration via system properties:</p>
 * <ul>
 *   <li>{@code org.opennms.core.ipc.sink.kafka.bootstrap.servers} — Kafka bootstrap servers (default: kafka:9092)</li>
 *   <li>{@code org.opennms.core.ipc.sink.kafka.group.id} — Consumer group ID (default: opennms-telemetryd-sink)</li>
 * </ul>
 */
public class KafkaSinkBridge implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSinkBridge.class);
    private static final Duration POLL_DURATION = Duration.ofMillis(100);

    private static final String PROP_BOOTSTRAP_SERVERS = "org.opennms.core.ipc.sink.kafka.bootstrap.servers";
    private static final String PROP_GROUP_ID = "org.opennms.core.ipc.sink.kafka.group.id";
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String DEFAULT_GROUP_ID = "opennms-telemetryd-sink";

    private final AbstractMessageConsumerManager consumerManager;

    private volatile SinkModule<?, Message> module;
    private volatile Thread consumerThread;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public KafkaSinkBridge(AbstractMessageConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    /**
     * Called by {@link TelemetryMessageConsumerManager#startConsumingForModule} when
     * a telemetry consumer registers. Provides the exact module reference needed for
     * dispatch (module identity/equals matching in the consumer map).
     */
    public void setModule(SinkModule<?, Message> module) {
        this.module = module;
    }

    @Override
    public void afterPropertiesSet() {
        String threadName = "kafka-sink-bridge-" + (module != null ? module.getId() : "unknown");
        consumerThread = new Thread(this::pollLoop, threadName);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private void pollLoop() {
        // Wait for the module to be set (consumer registers asynchronously)
        while (module == null && !closed.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (closed.get()) return;

        final String bootstrapServers = System.getProperty(PROP_BOOTSTRAP_SERVERS, DEFAULT_BOOTSTRAP_SERVERS);
        final String groupId = System.getProperty(PROP_GROUP_ID, DEFAULT_GROUP_ID);
        final String topic = "OpenNMS.Sink." + module.getId();
        LOG.info("KafkaSinkBridge starting: topic={}, bootstrapServers={}, groupId={}", topic, bootstrapServers, groupId);

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", ByteArrayDeserializer.class.getName());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            while (!closed.get()) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(POLL_DURATION);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        try {
                            SinkMessage sinkMessage = SinkMessage.parseFrom(record.value());
                            byte[] content = sinkMessage.getContent().toByteArray();
                            Message message = module.unmarshal(content);
                            consumerManager.dispatch(module, message);
                            LOG.debug("Dispatched Sink message from Kafka: module={}, offset={}",
                                    module.getId(), record.offset());
                        } catch (Exception e) {
                            LOG.warn("Error processing Sink message from Kafka (module={}, offset={}): {}",
                                    module.getId(), record.offset(), e.getMessage(), e);
                        }
                    }
                } catch (Throwable t) {
                    if (closed.get()) break;
                    LOG.error("Error in KafkaSinkBridge poll loop (module={}): {}", module.getId(), t.getMessage(), t);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            if (!closed.get()) {
                LOG.error("Fatal error in KafkaSinkBridge (module={}): {}", module.getId(), t.getMessage(), t);
            }
        }
        LOG.info("KafkaSinkBridge stopped for module: {}", module != null ? module.getId() : "unknown");
    }

    @Override
    public void destroy() {
        closed.set(true);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }
}
