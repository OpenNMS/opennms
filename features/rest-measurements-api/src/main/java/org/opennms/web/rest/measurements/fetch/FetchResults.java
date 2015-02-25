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

import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Used to store the results of a fetch.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public class FetchResults {

    private final long[] m_timestamps;

    private final Map<String, double[]> m_columns;

    private final long m_step;

    private final Map<String, Object> m_constants;

    public FetchResults(final long[] timestamps, Map<String, double[]> columns, final long step, final Map<String, Object> constants) {
        Preconditions.checkNotNull(timestamps, "timestamps argument");
        Preconditions.checkNotNull(columns, "columns argument");
        Preconditions.checkNotNull(constants, "constants argument");

        m_timestamps = timestamps;
        m_columns = columns;
        m_step = step;
        m_constants = constants;
    }

    public long[] getTimestamps() {
        return m_timestamps;
    }

    public Map<String, double[]> getColumns() {
        return m_columns;
    }

    public long getStep() {
        return m_step;
    }

    public Map<String, Object> getConstants() {
        return m_constants;
    }
}
