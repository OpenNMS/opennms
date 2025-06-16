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
package org.opennms.netmgt.daemon;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Loads applicationContext-activemq.xml unless the
 * 'org.opennms.activemq.broker.disable' system property is set to 'true'.
 *
 * @author jwhite
 */
@Configuration
@Conditional(ConditionalActiveMQContext.Condition.class)
@ImportResource("/META-INF/opennms/applicationContext-activemq.xml")
public class ConditionalActiveMQContext {
    public static final String DISABLE_BROKER_SYS_PROP = "org.opennms.activemq.broker.disable";

    static class Condition implements ConfigurationCondition {
         @Override
         public ConfigurationPhase getConfigurationPhase() {
             return ConfigurationPhase.PARSE_CONFIGURATION;
         }
         @Override
         public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
             return !Boolean.getBoolean(DISABLE_BROKER_SYS_PROP);
         }
    }
}
