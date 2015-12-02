/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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


public abstract class TopologyLink {

    private final Integer m_srcNodeId;
    private final String m_srcLabel;
    private final String m_srcSysoid;
    private final String m_srcLocation;
    private final NodeType m_srcNodeType;
    private final Integer m_targetNodeId;
    private final String m_targetLabel;
    private final String m_targetSysoid;
    private final String m_targetLocation;
    private final NodeType m_targetNodeType;

    public TopologyLink( 
            Integer nodeId, 
            String srcLabel, String srcSysoid, String srcLocation,
            NodeType srcNodeType,
            Integer targetNodeId, 
            String targetLabel, String targetSysoid, String targetLocation,
            NodeType targetNodeType
            ) {
        m_srcNodeId = nodeId;
        m_srcLabel = srcLabel;
        m_srcSysoid = srcSysoid;
        m_srcLocation = srcLocation;
        m_srcNodeType = srcNodeType;
        m_targetNodeId = targetNodeId;
        m_targetLabel = targetLabel;
        m_targetSysoid = targetSysoid;
        m_targetLocation = targetLocation;
        m_targetNodeType = targetNodeType;
    }

    public Integer getSrcNodeId() {
        return m_srcNodeId;
    }


    public Integer getTargetNodeId() {
        return m_targetNodeId;
    }

    public String getSrcLabel() {
        return m_srcLabel;
    }

    public String getSrcSysoid() {
        return m_srcSysoid;
    }

    public String getSrcLocation() {
        return m_srcLocation;
    }

    public NodeType getSrcNodeType() {
        return m_srcNodeType;
    }

    public String getTargetLabel() {
        return m_targetLabel;
    }

    public String getTargetSysoid() {
        return m_targetSysoid;
    }

    public String getTargetLocation() {
        return m_targetLocation;
    }

    public NodeType getTargetNodeType() {
        return m_targetNodeType;
    }

}
