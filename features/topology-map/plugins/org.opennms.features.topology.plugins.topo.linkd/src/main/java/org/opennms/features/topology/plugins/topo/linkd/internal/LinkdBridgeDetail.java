/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.model.topology.BridgePort;

public class LinkdBridgeDetail extends LinkdEdgeDetail<BridgePort,BridgePort> {

    private final Integer m_sourceBridgePort;
    private final Integer m_targetBridgePort;
    private final Integer m_sourceIfIndex;
    private final Integer m_targetifIndex;

    public LinkdBridgeDetail(Vertex source, BridgePort sourceBridgePort, Vertex target, BridgePort targetBridgePort) {
        super(LinkdEdge.getDefaultEdgeId(sourceBridgePort.getNodeId(), targetBridgePort.getNodeId()), source, sourceBridgePort, target, targetBridgePort);
        m_sourceBridgePort = sourceBridgePort.getBridgePort();
        m_targetBridgePort = targetBridgePort.getBridgePort();
        m_sourceIfIndex = sourceBridgePort.getBridgePortIfIndex();
        m_targetifIndex = targetBridgePort.getBridgePortIfIndex();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSourceLink() == null) ? 0 : getSource().getNodeID().hashCode()) + ((getTargetLink() == null) ? 0 : getTarget().getNodeID().hashCode());
        result = prime * result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LinkdBridgeDetail){
            LinkdBridgeDetail objDetail = (LinkdBridgeDetail)obj;

            return getId().equals(objDetail.getId());
        } else  {
            return false;
        }
    }
    
    public Integer getSourceBridgePort() {
        return m_sourceBridgePort;
    }

    public Integer getTargetBridgePort() {
        return m_targetBridgePort;
    }

    @Override
    public String getType() {
        return "Bridge";
    }

    @Override
    public Integer getSourceIfIndex() {
        return m_sourceIfIndex;
    }

    @Override
    public Integer getTargetIfIndex() {
        return m_targetifIndex;
    }
}