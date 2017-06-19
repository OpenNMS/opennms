/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

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
    public Map<String, String> getStyleProperties() {
        return Maps.newHashMap();
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", m_label, m_alarmCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof DefaultStatus) {
            DefaultStatus other = (DefaultStatus) obj;
            return Objects.equals(computeStatus(), other.computeStatus())
                    && Objects.equals(getStatusProperties(), other.getStatusProperties());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(computeStatus(), getStatusProperties());
    }
}
