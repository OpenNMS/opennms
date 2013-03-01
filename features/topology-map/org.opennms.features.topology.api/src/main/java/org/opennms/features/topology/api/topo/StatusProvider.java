package org.opennms.features.topology.api.topo;

import java.util.Collection;


public interface StatusProvider {

    public Status getStatusForVertex(VertexRef vertex);
    public Collection<Status> getStatusForVertices(Collection<VertexRef> vertices);
    public String getNamespace();
}
