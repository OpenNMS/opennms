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
