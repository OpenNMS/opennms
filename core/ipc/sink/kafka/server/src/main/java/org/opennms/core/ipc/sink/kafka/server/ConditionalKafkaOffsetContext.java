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
package org.opennms.core.ipc.sink.kafka.server;

import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.common.SinkStrategy;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX;
import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX;
import static org.opennms.core.ipc.sink.common.SinkStrategy.Strategy.KAFKA;

@Configuration
@Conditional(ConditionalKafkaOffsetContext.Condition.class)
@ImportResource("/META-INF/opennms/applicationContext-ipc-offset-provider.xml")
public class ConditionalKafkaOffsetContext {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionalKafkaOffsetContext.class);

    static class Condition implements ConfigurationCondition {
        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            final boolean kafkaSinkEnabled = KAFKA.equals(SinkStrategy.getSinkStrategy());
            if (!kafkaSinkEnabled) {
                return false;
            }
            // Default to enabled, and require an explicit value to disable
            boolean disabled = Boolean.getBoolean(KAFKA_CONFIG_SYS_PROP_PREFIX + "offset.disabled");
            if (!disabled) {
                disabled = Boolean.getBoolean(KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX + "offset.disabled");
            }
            final var offsetEnabled = !disabled;
            try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
                if (offsetEnabled) {
                    LOG.debug("Kafka offset provider is enabled.");
                } else {
                    LOG.debug("Kafka offset provider is disabled.");
                }
            }
            return offsetEnabled;
        }
    }
}
