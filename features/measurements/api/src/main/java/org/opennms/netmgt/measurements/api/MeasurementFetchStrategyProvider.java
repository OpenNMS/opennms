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
