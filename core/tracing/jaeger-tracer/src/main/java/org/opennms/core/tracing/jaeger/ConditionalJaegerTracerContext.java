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
package org.opennms.core.tracing.jaeger;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Conditionally load Jaeger tracer if jaeger tracer is enabled in system properties.
 */
@Configuration
@Conditional(ConditionalJaegerTracerContext.Condition.class)
@ImportResource("/META-INF/opennms/applicationContext-tracer-jaeger.xml")
public class ConditionalJaegerTracerContext {

    public static final String TRACER = "org.opennms.core.tracer";
    private static final String JEAGER_TRACER = "jaeger";
    private static final String GLOBAL_TRACER = "global";

    static class Condition implements ConfigurationCondition {

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String tracerProperty = System.getProperty(TRACER, GLOBAL_TRACER);
            final boolean enabled = JEAGER_TRACER.equals(tracerProperty);
            return enabled;
        }
    }
}
