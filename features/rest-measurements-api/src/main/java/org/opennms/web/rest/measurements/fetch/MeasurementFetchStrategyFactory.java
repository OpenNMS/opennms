/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.measurements.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Used to instantiate a fetch strategy based on the
 * current persistence strategy.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class MeasurementFetchStrategyFactory implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementFetchStrategyFactory.class);

    private static final String RRD_STRATEGY_CLASS_PROPERTY = "org.opennms.rrd.strategyClass";

    private ApplicationContext m_context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        m_context = context;
    }

	public MeasurementFetchStrategy getFetchStrategy() {
	    final String rrdStrategyClass = System.getProperty(RRD_STRATEGY_CLASS_PROPERTY);
	    for (MeasurementFetchStrategy fetchStrategy : m_context.getBeansOfType(MeasurementFetchStrategy.class).values()) {
	        if (fetchStrategy.supportsRrdStrategy(rrdStrategyClass)) {
	            return fetchStrategy;
	        }
	    }

        LOG.error("No supported fetch strategy found for {}. Defaulting to NullFetchStrategy.", rrdStrategyClass);
        return new NullFetchStrategy();
	}
}
