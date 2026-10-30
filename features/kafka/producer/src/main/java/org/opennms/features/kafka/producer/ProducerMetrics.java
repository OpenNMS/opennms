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
package org.opennms.features.kafka.producer;

import java.util.Locale;
import java.util.function.Supplier;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Per message type counts and timings of what the producer sends to Kafka, shown by
 * {@code opennms:metrics-display} and over JMX: {@code messages.<type>.sent}, {@code .failed},
 * {@code .dropped}, {@code .build} and {@code .size}, plus {@code messages.sendQueueSize}.
 */
public class ProducerMetrics {

    public static final String PREFIX = "messages";

    private final MetricRegistry registry;

    public ProducerMetrics(final MetricRegistry registry) {
        this.registry = registry;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    static String name(final KafkaProducerManager.MessageType type, final String metric) {
        return MetricRegistry.name(PREFIX, type.name().toLowerCase(Locale.ROOT), metric);
    }

    /** Time spent turning the OpenNMS object into a Kafka record. */
    public Timer build(final KafkaProducerManager.MessageType type) {
        return registry.timer(name(type, "build"));
    }

    /** Kafka acknowledged the record. */
    public void sent(final KafkaProducerManager.MessageType type) {
        registry.meter(name(type, "sent")).mark();
    }

    /** The send failed, either immediately or in Kafka's completion callback. */
    public void failed(final KafkaProducerManager.MessageType type) {
        registry.meter(name(type, "failed")).mark();
    }

    /** The record was discarded because the send queue was full. */
    public void dropped(final KafkaProducerManager.MessageType type) {
        registry.counter(name(type, "dropped")).inc();
    }

    /** Size of the record value in bytes; deletes (tombstones) count as 0. */
    public void size(final KafkaProducerManager.MessageType type, final byte[] value) {
        registry.histogram(name(type, "size")).update(value == null ? 0 : value.length);
    }

    public void registerSendQueueSize(final Supplier<Integer> size) {
        registry.gauge(MetricRegistry.name(PREFIX, "sendQueueSize"), () -> (Gauge<Integer>) size::get);
    }
}
