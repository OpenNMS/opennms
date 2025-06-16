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
package org.opennms.netmgt.timeseries.sampleread;

import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategyProvider;

/**
 * Provides a reference to the {@link TimeseriesFetchStrategy}
 * when using the 'integration' time-series series strategy.
 *
 * @author jwhite
 */
public class TimeseriesFetchStrategyProvider implements MeasurementFetchStrategyProvider {

    public static final String TIME_SERIES_STRATEGY_NAME = "integration";

    @Override
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String timeSeriesStrategyName, String rrdStrategyClass) {
        if (!TIME_SERIES_STRATEGY_NAME.equalsIgnoreCase(timeSeriesStrategyName)) {
            return null;
        }
        return TimeseriesFetchStrategy.class;
    }
}
