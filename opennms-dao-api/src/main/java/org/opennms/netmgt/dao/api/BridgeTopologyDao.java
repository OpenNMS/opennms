package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BridgeTopology;

public interface BridgeTopologyDao {
    
    void store(BridgeElement bridge);
    void store(BridgeMacLink maclink);
    void store(BridgeStpLink stpLink);
    void store(BridgeBridgeLink bridgeLink);
    void walked(int nodeid, Date now);
    boolean topologyChanged(int nodeid);
    BridgeTopology getTopology(int nodeid);
    List<Integer> getUpdatedNodes(int nodeid); 
    Date getUpdateTime(int nodeid);
}
