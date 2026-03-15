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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Verifies that {@link KafkaEventTransportConfiguration} is a valid Spring
 * {@link Configuration} class with the expected bean definitions.
 *
 * <p>This test does not start Kafka or a full Spring context. Integration
 * testing with a real Kafka broker is covered by the Alarmd integration
 * test (Task 12) using Testcontainers.</p>
 */
class KafkaEventTransportConfigurationTest {

    @Test
    void configurationClassHasSpringAnnotation() {
        assertThat(KafkaEventTransportConfiguration.class)
                .hasAnnotation(Configuration.class);
    }

    @Test
    void kafkaEventForwarderBeanMethodExists() throws NoSuchMethodException {
        var method = KafkaEventTransportConfiguration.class
                .getDeclaredMethod("kafkaEventForwarder");
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
    }

    @Test
    void kafkaEventSubscriptionServiceBeanMethodExists() throws NoSuchMethodException {
        var method = KafkaEventTransportConfiguration.class
                .getDeclaredMethod("kafkaEventSubscriptionService");
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();

        Bean beanAnnotation = method.getAnnotation(Bean.class);
        assertThat(beanAnnotation.initMethod()).contains("start");
        assertThat(beanAnnotation.destroyMethod()).contains("stop");
    }

    @Test
    void eventIpcManagerBeanMethodExists() throws NoSuchMethodException {
        var method = KafkaEventTransportConfiguration.class
                .getDeclaredMethod("eventIpcManager",
                        org.opennms.core.event.forwarder.kafka.KafkaEventForwarder.class,
                        org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionService.class);
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
    }
}
