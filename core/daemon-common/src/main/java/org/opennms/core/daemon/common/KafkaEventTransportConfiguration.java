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
package org.opennms.core.daemon.common;

import org.opennms.core.event.forwarder.kafka.KafkaEventForwarder;
import org.opennms.core.event.forwarder.kafka.KafkaEventForwarderFactory;
import org.opennms.core.event.forwarder.kafka.KafkaEventIpcManagerAdapter;
import org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionService;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring {@link Configuration} that replaces the OSGi Blueprint
 * {@code blueprint-event-forwarder-kafka.xml} for Spring Boot daemon containers.
 *
 * <p>Creates the Kafka-backed event transport stack:</p>
 * <ol>
 *   <li>{@link KafkaEventForwarder} — publishes events to Kafka topics</li>
 *   <li>{@link KafkaEventSubscriptionService} — consumes events from Kafka topics
 *       and dispatches to registered listeners</li>
 *   <li>{@link KafkaEventIpcManagerAdapter} — composes forwarder + subscription
 *       into the {@link EventIpcManager} interface expected by daemon code</li>
 * </ol>
 *
 * <p>The forwarder uses {@code NoOpEventProcessor} (no eventconf expansion) because
 * daemon containers do not have access to eventconf. Events are enriched separately
 * by the core Eventd pipeline before reaching Kafka.</p>
 */
@Configuration
public class KafkaEventTransportConfiguration {

    @Value("${opennms.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    @Value("${opennms.kafka.event-topic:opennms-fault-events}")
    private String eventTopic;

    @Value("${opennms.kafka.ipc-topic:opennms-ipc-events}")
    private String ipcTopic;

    @Value("${opennms.kafka.consumer-group:opennms-core}")
    private String consumerGroup;

    @Value("${opennms.kafka.poll-timeout-ms:100}")
    private long pollTimeoutMs;

    @Bean
    public KafkaEventForwarder kafkaEventForwarder() {
        KafkaEventForwarder forwarder = KafkaEventForwarderFactory.create(bootstrapServers, eventTopic);
        forwarder.setIpcTopicName(ipcTopic);
        return forwarder;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public KafkaEventSubscriptionService kafkaEventSubscriptionService() {
        return KafkaEventSubscriptionService.create(
                bootstrapServers,
                consumerGroup,
                eventTopic + "," + ipcTopic,
                pollTimeoutMs);
    }

    @Bean
    public EventIpcManager eventIpcManager(KafkaEventForwarder forwarder,
                                           KafkaEventSubscriptionService subscriptionService) {
        return new KafkaEventIpcManagerAdapter(forwarder, subscriptionService);
    }
}
