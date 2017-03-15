/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;

public class LinkdEdge extends AbstractEdge {

    private Integer m_sourceNodeid;
    private Integer m_targetNodeid;

    private String m_sourceEndPoint;
    private String m_targetEndPoint;
    
    public LinkdEdge(String namespace, String id, Vertex source, Vertex target) {
        super(namespace, id, source, target);
    }

    public LinkdEdge(String namespace, String id, SimpleConnector source,
            SimpleConnector target) {
        super(namespace, id, source, target);
    }

    public Integer getSourceNodeid() {
        return m_sourceNodeid;
    }

    public void setSourceNodeid(Integer sourceNodeid) {
        m_sourceNodeid = sourceNodeid;
    }

    public Integer getTargetNodeid() {
        return m_targetNodeid;
    }

    public void setTargetNodeid(Integer targetNodeid) {
        m_targetNodeid = targetNodeid;
    }

    public String getSourceEndPoint() {
        return m_sourceEndPoint;
    }

    public void setSourceEndPoint(String sourceEndPoint) {
        m_sourceEndPoint = sourceEndPoint;
    }

    public String getTargetEndPoint() {
        return m_targetEndPoint;
    }

    public void setTargetEndPoint(String targetEndPoint) {
        m_targetEndPoint = targetEndPoint;
    }

    public boolean containsVertexEndPoint(String vertexRef, String endpointRef) {
        if (vertexRef == null)
            return false;
        if (endpointRef == null)
            return false;
        if (getSource() != null && getSourceEndPoint() != null 
                && getSource().getVertex().getId().equals(vertexRef) && getSourceEndPoint().equals(endpointRef))
            return true;
        if (getTarget() != null && getTargetEndPoint() != null 
                && getTarget().getVertex().getId().equals(vertexRef) && getTargetEndPoint().equals(endpointRef))
            return true;
        return false;
    }
}
