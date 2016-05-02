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

import java.util.List;

import org.opennms.netmgt.measurements.model.Source;

/**
 * Used to retrieve measurements from the current persistence mechanism.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public interface MeasurementFetchStrategy {

    /**
     * Fetches the measurements for the given sources.
     *
     * @param start      timestamp in milliseconds
     * @param end        timestamp in milliseconds
     * @param step       desired resolution in milliseconds - actual resolution might differ
     * @param maxrows    maximum number of rows - no limit when <= 0
     * @param interval   duration in milliseconds, used by strategies that implement late aggregation
     * @param heartbeat  duration in milliseconds, used by strategies that implement late aggregation 
     * @param sources    array of sources - these should have unique labels
     * @param relaxed    if <code>false</code> a missing source results in a return of <code>null</code>.
     *                   <code>true</code> on the other hand ignores that source.
     * @return           null when a resource id or attribute cannot be found
     * @throws Exception
     */
    public FetchResults fetch(long start, long end, long step, int maxrows,
                              Long interval, Long heartbeat,
                              List<Source> sources, boolean relaxed) throws Exception;
}
