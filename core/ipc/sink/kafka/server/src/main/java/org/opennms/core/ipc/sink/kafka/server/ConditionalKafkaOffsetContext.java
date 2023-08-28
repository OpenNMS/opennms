/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
