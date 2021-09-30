/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Loads the component-rrd-tcp.xml if and only if useTcp is enabled.
 *
 * Avoids having modules that don't use the TCP Output Strategy
 * rely on its poms.
 *
 * @author jwhite
 */
@Configuration
@Conditional(ConditionalTcpStrategyContext.Condition.class)
@ImportResource("/META-INF/opennms/component-rrd-tcp.xml")
public class ConditionalTcpStrategyContext {
    static class Condition implements ConfigurationCondition {
         @Override
         public ConfigurationPhase getConfigurationPhase() {
             return ConfigurationPhase.PARSE_CONFIGURATION;
         }
         @Override
         public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
             // Only load component-rrd-tcp.xml if useTcp is enabled
             return new Boolean(System.getProperty("org.opennms.rrd.usetcp", "false"));
         }
    }
}
