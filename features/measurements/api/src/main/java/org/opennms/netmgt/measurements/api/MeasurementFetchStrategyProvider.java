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

/**
 * Every {@link MeasurementFetchStrategy} implementation is represented
 * by {@link MeasurementFetchStrategyProvider}, which can be used to determine
 * whether or not it can fetch measurements for a given Time Series / RRD strategy
 * combo.
 *
 * The {@link MeasurementFetchStrategyProvider} allows us to determine which (fetch) strategy
 * to use, without needed to actually instantiate the implementation classes, since these may have
 * dependencies which are not available on the current class-path.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public interface MeasurementFetchStrategyProvider {

    /**
     * Returns a reference to an implementation of
     * {@link org.opennms.netmgt.measurements.api.MeasurementFetchStrategy}
     * that supports retrieving measurements for the given Time Series / RRD Strategy.
     *
     * @param timeSeriesStrategyName name of the time series strategy
     * @param rrdStrategyClass canonical name of the {@link org.opennms.netmgt.rrd.RrdStrategy} implementation
     * @return null or a reference to a supported implementation
     */
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String timeSeriesStrategyName, String rrdStrategyClass);

}
