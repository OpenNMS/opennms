package org.opennms.netmgt.model;

public interface OnmsTopologyUpdater extends OnmsTopologyRef {
    
    OnmsTopology getTopology();
    
    OnmsTopologyProtocol getProtocol();

}
