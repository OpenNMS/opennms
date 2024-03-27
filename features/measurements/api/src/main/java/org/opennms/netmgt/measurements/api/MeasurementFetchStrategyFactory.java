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
