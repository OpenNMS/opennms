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

import java.util.List;

import org.opennms.web.rest.measurements.model.Source;

/**
 * Used to retrieve measurements from the current persistence mechanism.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public interface MeasurementFetchStrategy {

    /**
     * Fetches the measurements for the given sources.
     *
     * @param step       desired resolution in seconds - actual resolution might differ
     * @param start      timestamp in seconds
     * @param end        timestamp in seconds
     * @param sources    array of sources - these should have unique labels
     * @return           null when a resource id or attribute cannot be found
     * @throws Exception
     */
	public FetchResults fetch(final long step, final long start,
            final long end, final List<Source> sources) throws Exception;
}
