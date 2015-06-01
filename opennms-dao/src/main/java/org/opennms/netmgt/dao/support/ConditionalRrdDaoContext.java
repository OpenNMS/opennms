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

package org.opennms.netmgt.dao.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Conditionally loads the RRD specific beans.
 *
 * These are loaded conditionally since alternate persistence implementations i.e. Newts
 * may have different implementations.
 *
 * @author jwhite
 */
@Configuration
@Conditional(ConditionalRrdDaoContext.Condition.class)
@ImportResource("/META-INF/opennms/component-dao-ext-rrd.xml")
public class ConditionalRrdDaoContext {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionalRrdDaoContext.class);

    private static final String RRD_STRATEGY_CLASS_PROPETERY_NAME = "org.opennms.rrd.strategyClass";

    private static final String[] SUPPORTED_RRD_STRATEGY_CLASS_NAMES = new String[] {
        "org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy",
        "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy"
    };

    static class Condition implements ConfigurationCondition {
         @Override
         public ConfigurationPhase getConfigurationPhase() {
             return ConfigurationPhase.PARSE_CONFIGURATION;
         }

         @Override
         public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
             final String rrdStrategyClass = System.getProperty(RRD_STRATEGY_CLASS_PROPETERY_NAME);
             for (int k = 0; k < SUPPORTED_RRD_STRATEGY_CLASS_NAMES.length; k++) {
                 if (SUPPORTED_RRD_STRATEGY_CLASS_NAMES.equals(rrdStrategyClass)) {
                     return true;
                 }
             }

             LOG.info("The RRD Stragegy class {} does not support any of the known RRD implementations. Context will not be loaded.",
                     rrdStrategyClass);
             return false;
         }
    }
}
