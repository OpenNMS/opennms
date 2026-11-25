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
import com.codahale.metrics.Histogram;
import com.codahale.metrics.LockFreeExponentiallyDecayingReservoir;
import com.codahale.metrics.Meter;
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
    // Racy lazy init is safe: the registry returns the same metrics for a name and TypeMetrics is immutable.
    private final TypeMetrics[] byType = new TypeMetrics[KafkaProducerManager.MessageType.values().length];

    public ProducerMetrics(final MetricRegistry registry) {
        this.registry = registry;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    static String name(final KafkaProducerManager.MessageType type, final String metric) {
        return MetricRegistry.name(PREFIX, type.name().toLowerCase(Locale.ROOT), metric);
    }

    private TypeMetrics of(final KafkaProducerManager.MessageType type) {
        TypeMetrics metrics = byType[type.ordinal()];
        if (metrics == null) {
            metrics = new TypeMetrics(registry, type);
            byType[type.ordinal()] = metrics;
        }
        return metrics;
    }

    /** Time spent turning the OpenNMS object into a Kafka record. */
    public Timer build(final KafkaProducerManager.MessageType type) {
        return of(type).build;
    }

    /** Kafka acknowledged the record. */
    public void sent(final KafkaProducerManager.MessageType type) {
        of(type).sent.mark();
    }

    /** The send failed, either immediately or in Kafka's completion callback. */
    public void failed(final KafkaProducerManager.MessageType type) {
        of(type).failed.mark();
    }

    /** The record was discarded because the send queue was full; registered on first drop so unqueued types never show it. */
    public void dropped(final KafkaProducerManager.MessageType type) {
        registry.counter(name(type, "dropped")).inc();
    }

    /** Size of the record value in bytes; deletes (tombstones) count as 0. */
    public void size(final KafkaProducerManager.MessageType type, final byte[] value) {
        of(type).size.update(value == null ? 0 : value.length);
    }

    public void registerSendQueueSize(final Supplier<Integer> size) {
        registry.gauge(MetricRegistry.name(PREFIX, "sendQueueSize"), () -> (Gauge<Integer>) size::get);
    }

    private static final class TypeMetrics {
        private final Timer build;
        private final Meter sent;
        private final Meter failed;
        private final Histogram size;

        private TypeMetrics(final MetricRegistry registry, final KafkaProducerManager.MessageType type) {
            build = registry.timer(name(type, "build"), () -> new Timer(LockFreeExponentiallyDecayingReservoir.builder().build()));
            sent = registry.meter(name(type, "sent"));
            failed = registry.meter(name(type, "failed"));
            size = registry.histogram(name(type, "size"), () -> new Histogram(LockFreeExponentiallyDecayingReservoir.builder().build()));
        }
    }
}
