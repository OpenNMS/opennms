package org.opennms.features.topology.api.geo;

import java.util.Collection;

import org.opennms.features.topology.api.topo.VertexRef;

public interface GeoAssetProvider {
    public Collection<VertexRef> getNodesWithCoordinates();
}
