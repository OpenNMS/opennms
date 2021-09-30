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

package org.opennms.netmgt.measurements.api;

import java.util.ServiceLoader;

import org.opennms.core.utils.TimeSeries;
import org.opennms.netmgt.measurements.impl.NullFetchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Used to instantiate the appropriate fetch strategy. 
 *
 * The appropriate strategy is determined by querying
 * all of the available {@link org.opennms.netmgt.measurements.api.MeasurementFetchStrategyProvider}
 * registered via the ServiceLoader.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public class MeasurementFetchStrategyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementFetchStrategyFactory.class);

    private static ServiceLoader<MeasurementFetchStrategyProvider> providerLoader = ServiceLoader.load(MeasurementFetchStrategyProvider.class);

    @Bean(name="measurementFetchStrategy")
    public MeasurementFetchStrategy getStrategy() throws InstantiationException, IllegalAccessException {
        final String timeSeriesStrategyName = System.getProperty(TimeSeries.TIMESERIES_STRATEGY_PROPERTY, TimeSeries.RRD_TIME_SERIES_STRATEGY_NAME);
        final String rrdStrategyClass = System.getProperty(TimeSeries.RRD_STRATEGY_CLASS_PROPERTY, TimeSeries.DEFAULT_RRD_STRATEGY_CLASS);
        for (MeasurementFetchStrategyProvider provider : providerLoader) {
            Class<? extends MeasurementFetchStrategy> strategy = provider.getStrategyClass(timeSeriesStrategyName, rrdStrategyClass);
            if (strategy != null) {
                return strategy.newInstance();
            }
        }

        LOG.error("No supported fetch strategy found for {}/{}. Defaulting to NullFetchStrategy.", timeSeriesStrategyName, rrdStrategyClass);
        return new NullFetchStrategy();
    }
}
