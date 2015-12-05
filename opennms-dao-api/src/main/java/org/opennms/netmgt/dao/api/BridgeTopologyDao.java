package org.opennms.netmgt.dao.api;


import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public interface BridgeTopologyDao {
    
    void delete(int nodeid);
    // The parse methods are used to check the Bridge Forwarding Table
    void parse(int nodeid, BridgeMacLink maclink);
    void parse(int nodeid, BridgeStpLink stpLink);
    
    // Storing is saving data without calculations
    void store(BridgeElement bridge);
    void store(BridgeMacLink maclink);
    void store(BridgeBridgeLink maclink);

    void walked(int nodeid);
    
    BroadcastDomain getBroadcastDomain(int nodeid);

}
