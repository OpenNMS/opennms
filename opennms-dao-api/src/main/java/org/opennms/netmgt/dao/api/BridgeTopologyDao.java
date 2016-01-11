package org.opennms.netmgt.dao.api;


import java.util.Set;

import org.opennms.netmgt.model.topology.BroadcastDomain;

public interface BridgeTopologyDao {
        
    
    // clear topology for all BroadcastDomain that are empty
    void clear();
    // update broadcastdomain
    void save(BroadcastDomain domain);
    // Load the topology from the scratch
    void load(Set<BroadcastDomain> domain);

    void delete(BroadcastDomain domain);

    // getting BroacastDomain for calculation
    BroadcastDomain get(int nodeid);
    
    Set<BroadcastDomain> get();

}
