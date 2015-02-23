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
import java.util.SortedMap;

/**
 * Used to store the results of a fetch.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public class FetchResults {

    private final Map<String, Object> m_constants;

    private final SortedMap<Long, Map<String, Double>> m_rows;

    private final long m_step;

    public FetchResults(SortedMap<Long, Map<String, Double>> rows, long step, Map<String, Object> constants) {
        m_rows = rows;
        m_step = step;
        m_constants = constants;
    }

    public SortedMap<Long, Map<String, Double>> getRows() {
        return m_rows;
    }

    public long getStep() {
        return m_step;
    }

    public Map<String, Object> getConstants() {
        return m_constants;
    }
}
