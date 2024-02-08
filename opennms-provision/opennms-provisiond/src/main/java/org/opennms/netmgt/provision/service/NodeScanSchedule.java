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
package org.opennms.netmgt.provision.service;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.Duration;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * <p>NodeScanSchedule class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeScanSchedule {
    private int m_nodeId;
    private String m_foreignSource;
    private String m_foreignId;
    private OnmsMonitoringLocation m_location;
    private Duration m_initialDelay;
    private Duration m_scanInterval;
    private String monitorKey;
    
    /**
     * <p>Constructor for NodeScanSchedule.</p>
     *
     * @param nodeId a int.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param initialDelay a {@link org.joda.time.Duration} object.
     * @param scanInterval a {@link org.joda.time.Duration} object.
     * @param monitorKey a {@link java.lang.String} object. (optional)
     */
    public NodeScanSchedule(int nodeId, String foreignSource, String foreignId, OnmsMonitoringLocation location, Duration initialDelay, Duration scanInterval, String monitorKey) {
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_location = location;
        m_initialDelay = initialDelay;
        m_scanInterval = scanInterval;
        this.monitorKey = monitorKey;
    }

    /**
     * <p>getForeignId</p>
     *
     * @return the foreignId
     */
    public String getForeignId() {
        return m_foreignId;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return the nodeId
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return the foreignSource
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    public OnmsMonitoringLocation getLocation() {
        return m_location;
    }

    /**
     * <p>getInitialDelay</p>
     *
     * @return the initialDelay
     */
    public Duration getInitialDelay() {
        return m_initialDelay;
    }

    /**
     * <p>getScanInterval</p>
     *
     * @return the scanInterval
     */
    public Duration getScanInterval() {
        return m_scanInterval;
    }

    public String getMonitorKey() {
        return monitorKey;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("foreign source", m_foreignSource)
            .append("foreign id", m_foreignId)
            .append("node id", m_nodeId)
            .append("location", m_location.getLocationName())
            .append("initial delay", m_initialDelay)
            .append("scan interval", m_scanInterval)
            .append("monitorKey", monitorKey)
            .toString();
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(m_foreignSource)
            .append(m_foreignId)
            .append(m_nodeId)
            .append(m_initialDelay)
            .append(m_scanInterval)
            .append(monitorKey)
            .toHashCode();
    }
}
