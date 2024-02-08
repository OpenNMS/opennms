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
package org.opennms.netmgt.rrd;

public class RrdDataSource {
    private final String m_name;
    private final RrdAttributeType m_type;
    private final int m_heartBeat;
    private final String m_min;
    private final String m_max;

    public RrdDataSource(String name, RrdAttributeType type, int heartBeat, String min, String max) {
        m_name = name;
        m_type = type;
        m_heartBeat = heartBeat;
        m_min = min;
        m_max = max;
    }

    /**
     * <p>getHeartBeat</p>
     *
     * @return a int.
     */
    public int getHeartBeat() {
        return m_heartBeat;
    }

    /**
     * <p>getMax</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMax() {
        return m_max;
    }

    /**
     * <p>getMin</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMin() {
        return m_min;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdAttributeType} object.
     */
    public RrdAttributeType getType() {
        return m_type;
    }

}
