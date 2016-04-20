package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;

public class LinkdEdge extends AbstractEdge {

    private String m_sourceEndPoint;
    private String m_targetEndPoint;
    
    public LinkdEdge(String namespace, String id, Vertex source, Vertex target) {
        super(namespace, id, source, target);
    }

    public LinkdEdge(String namespace, String id, SimpleConnector source,
            SimpleConnector target) {
        super(namespace, id, source, target);
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
