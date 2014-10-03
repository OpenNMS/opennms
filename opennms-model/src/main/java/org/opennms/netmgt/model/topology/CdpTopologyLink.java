/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;


public class CdpTopologyLink {

    private final Integer m_sourceId;
    private final Integer m_srcNodeId;
    private final Integer m_srcIfIndex;
    private final String m_srcIfName;
    private final Integer m_targetId;
    private final Integer m_targetNodeId;
    private final String m_targetIfName;

    public CdpTopologyLink(Integer sourceId, Integer srcNodeId, Integer srcIfIndex, String srcIfName, Integer targetId, Integer targetNodeId, String targetIfName) {
        m_sourceId = sourceId;
        m_srcNodeId = srcNodeId;
        m_srcIfIndex = srcIfIndex;
        m_srcIfName = srcIfName;
        m_targetId = targetId;
        m_targetNodeId = targetNodeId;
        m_targetIfName = targetIfName;
    }

    public Integer getSrcNodeId() {
        return m_srcNodeId;
    }

    public Integer getSrcIfIndex() {
        return m_srcIfIndex;
    }

    public String getSrcIfName() {
        return m_srcIfName;
    }

    public Integer getTargetNodeId() {
        return m_targetNodeId;
    }

    public String getTargetIfName() {
        return m_targetIfName;
    }

    public Integer getTargetId() { return m_targetId; }

    public Integer getSourceId() { return m_sourceId; }
}
