package org.opennms.netmgt.dao.api;


import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public interface BridgeTopologyDao {
    
    // delete is deleting nodeid from topology
    void delete(int nodeid);
    
    // The store methods are used to check the Bridge Forwarding Table
    void store(BridgeElement element);
    void store(BridgeMacLink maclink);
    void store(BridgeStpLink stpLink);

    // load node into broadcast
    void update(int nodeid);
    // Load the topology from the scratch
    void loadTopology(List<BridgeElement> bridgeelements, List<BridgeMacLink> bridgemaclinks,List<BridgeBridgeLink> bridgelinks, List<BridgeStpLink> stplinks);

    // getting BroacastDomain for calculation
    BroadcastDomain getBroadcastDomain(int nodeid);

}
