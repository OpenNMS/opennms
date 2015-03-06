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

package org.opennms.netmgt.model.topology;

public class IsIsTopologyLink {

    private final Integer m_sourceId;
    private final Integer m_sourceNodeId;
    private final int m_sourceIfIndex;
    private final int m_targetId;
    private final int m_targetNodeId;
    private final int m_targetIfIndex;

    public IsIsTopologyLink(Integer link1Id,
                            Integer link1Nodeid,
                            Integer link1IfIndex,
                            Integer link2Id,
                            Integer link2Nodeid,
                            Integer link2IfIndex) {

        m_sourceId = link1Id;
        m_sourceNodeId = link1Nodeid;
        m_sourceIfIndex = link1IfIndex;
        m_targetId = link2Id;
        m_targetNodeId = link2Nodeid;
        m_targetIfIndex = link2IfIndex;

    }

    public Integer getSourceId() {
        return m_sourceId;
    }

    public Integer getSourceNodeId() {
        return m_sourceNodeId;
    }

    public int getSourceIfIndex() {
        return m_sourceIfIndex;
    }

    public int getTargetId() {
        return m_targetId;
    }

    public int getTargetNodeId() {
        return m_targetNodeId;
    }

    public int getTargetIfIndex() {
        return m_targetIfIndex;
    }
}
