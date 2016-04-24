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
