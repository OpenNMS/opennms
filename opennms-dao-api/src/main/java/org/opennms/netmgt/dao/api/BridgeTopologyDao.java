package org.opennms.netmgt.dao.api;


import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public interface BridgeTopologyDao {
    
    void delete(int nodeid);
    
    // The parse methods are used to check the Bridge Forwarding Table
    void parse(int nodeid, BridgeElement element);
    void parse(int nodeid, BridgeMacLink maclink);
    void parse(int nodeid, BridgeStpLink stpLink);
    void walked(int nodeid);

    // Storing is saving data without calculations
    void loadTopology(List<BridgeElement> bridgeelements, List<BridgeMacLink> bridgemaclinks,List<BridgeBridgeLink> bridgelinks);

    BroadcastDomain getBroadcastDomain(int nodeid);

}
