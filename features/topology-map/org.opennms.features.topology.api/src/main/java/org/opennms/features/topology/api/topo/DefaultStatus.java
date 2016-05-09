/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo;

import java.util.HashMap;
import java.util.Map;

public class DefaultStatus implements Status {

    private final String m_label;
    private final long m_alarmCount;

    public DefaultStatus(String label, long count) {
        m_label = label;
        m_alarmCount = count;
    }

    @Override
    public String computeStatus() {
        return m_label.toLowerCase();
    }

    @Override
    public Map<String, String> getStatusProperties() {
        Map<String, String> statusMap = new HashMap<String, String>();
        statusMap.put("status", m_label.toLowerCase());
        statusMap.put("statusCount", "" + m_alarmCount);
        return statusMap;
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", m_label, m_alarmCount);
    }
}
