/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter.rrd.converter;

import java.io.IOException;
import java.util.List;

public interface TimeSeriesDataSource {

    /**
     * The beginning of the time span for this data source.
     * @return The start time, in seconds.
     * @throws IOException
     */
    long getStartTime() throws IOException;
    
    /**
     * The end of the time span for this data source.
     * @return The end time, in seconds.
     * @throws IOException
     */
    long getEndTime() throws IOException;
    
    /**
     * The resolution of this data source.
     * @return The number of seconds per sample.
     * @throws IOException
     */
    long getNativeStep() throws IOException;
    
    /**
     * The number of samples in this data source.
     * @return The number of samples.
     * @throws IOException
     */
    int getRows() throws IOException;
    
    /**
     * The names of the keys in this data source.
     * @return The data source names.
     * @throws IOException
     */
    List<String> getDsNames() throws IOException;

    /**
     * The data in this data source, given a timestamp.
     * @param timestamp The time to retrieve data at.
     * @return An {@link RrdEntry} object, with the relevant sample data.
     * @throws IOException
     */
    RrdEntry getDataAt(long timestamp) throws IOException;

    /**
     * The data in this data source, given a step.
     * @param The step size to return data in.  The native step must be evenly divisible by this number.
     * @return A list of {@link RrdEntry} objects, with samples at the given step's resolution.
     * @throws IOException
     */
    List<RrdEntry> getData(long step) throws IOException;

    /**
     * Close the data source.
     * @throws IOException
     */
    void close() throws IOException;

}
