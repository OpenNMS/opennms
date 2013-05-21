/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.Duration;

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
    private Duration m_initialDelay;
    private Duration m_scanInterval;
    
    /**
     * <p>Constructor for NodeScanSchedule.</p>
     *
     * @param nodeId a int.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param initialDelay a {@link org.joda.time.Duration} object.
     * @param scanInterval a {@link org.joda.time.Duration} object.
     */
    public NodeScanSchedule(int nodeId, String foreignSource, String foreignId, Duration initialDelay, Duration scanInterval) {
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_initialDelay = initialDelay;
        m_scanInterval = scanInterval;
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
            .append("initial delay", m_initialDelay)
            .append("scan interval", m_scanInterval)
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
            .toHashCode();
    }
}
