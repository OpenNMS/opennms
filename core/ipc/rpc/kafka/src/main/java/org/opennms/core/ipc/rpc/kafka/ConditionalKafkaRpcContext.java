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
package org.opennms.core.ipc.rpc.kafka;

import static org.opennms.core.rpc.common.RpcStrategy.Strategy.KAFKA;

import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.common.RpcStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@Conditional(ConditionalKafkaRpcContext.Condition.class)
@ImportResource("/META-INF/opennms/applicationContext-rpc-client-kafka.xml")
public class ConditionalKafkaRpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(ConditionalKafkaRpcContext.class);

    static class Condition implements ConfigurationCondition {
        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            final boolean enabled = KAFKA.equals(RpcStrategy.getRpcStrategy());
            try (MDCCloseable mdc = Logging.withPrefixCloseable(RpcClientFactory.LOG_PREFIX)) {
                LOG.debug("Enable Kafka RPC: {}", enabled);
            }
            return enabled;
        }
   }
}
