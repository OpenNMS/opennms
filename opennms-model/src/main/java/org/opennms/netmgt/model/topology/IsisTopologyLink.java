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

import org.opennms.netmgt.model.OnmsNode.NodeType;


public class IsisTopologyLink extends TopologyLink {

    private final Integer m_sourceId;
    private final Integer m_srcIfIndex;
    private final Integer m_targetId;
    private final Integer m_targetIfIndex;

    public IsisTopologyLink(Integer sourceId, Integer srcNodeId,
            String srcLabel, String srcSysoid, String srcLocation,
            NodeType srcNodeType,
            Integer srcIfIndex, Integer targetId,
            Integer targetNodeId,
            String targetLabel, String targetSysoid, String targetLocation,
            NodeType targetNodeType,
            Integer targetIfIndex) {
        super(srcNodeId,srcLabel,srcSysoid,srcLocation,srcNodeType,targetNodeId,targetLabel,targetSysoid,targetLocation,targetNodeType);
        m_sourceId = sourceId;
        m_srcIfIndex = srcIfIndex;
        m_targetId = targetId;
        m_targetIfIndex = targetIfIndex;
    }

    public Integer getSrcIfIndex() {
        return m_srcIfIndex;
    }

    public Integer getTargetIfIndex() {
        return m_targetIfIndex;
    }

    public Integer getTargetId() { return m_targetId; }

    public Integer getSourceId() { return m_sourceId; }
}
