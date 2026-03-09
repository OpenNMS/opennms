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
package org.opennms.core.event.forwarder.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.eventd.processor.TsidAssigner;
import org.opennms.netmgt.eventd.router.EventClassifier;

/**
 * Blueprint factory for creating {@link KafkaEventForwarder} instances.
 *
 * <p>Aries Blueprint 1.10.3 can't match constructor/factory-method args when
 * parameters include interfaces with multiple implementations (EventProcessor),
 * generic types (KafkaProducer), or cross-bundle types loaded from different
 * OSGi classloaders. This factory takes only String args and creates all
 * internal objects directly, bypassing Blueprint type matching.</p>
 */
public class KafkaEventForwarderFactory {

    private KafkaEventForwarderFactory() {
        // static factory — prevent instantiation
    }

    /**
     * Creates a fully-configured {@link KafkaEventForwarder} with no-op event
     * expansion (for daemon containers where eventconf is unavailable).
     *
     * @param bootstrapServers Kafka broker addresses
     * @param topicName        Kafka topic for fault events
     * @return configured KafkaEventForwarder; caller should set IPC topic via setter
     */
    public static KafkaEventForwarder create(String bootstrapServers, String topicName) {
        KafkaProducer<Long, byte[]> kafkaProducer = KafkaProducerFactory.create(bootstrapServers);

        return new KafkaEventForwarder(
                new NoOpEventProcessor(),
                new TsidAssigner(new TsidFactory(0L)),
                new EventClassifier(),
                kafkaProducer,
                topicName
        );
    }
}
