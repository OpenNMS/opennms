package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;

public interface BridgeTopologyDao {
    
    void load(BridgeElement bridge);
    void parse(BridgeMacLink maclink);
    void parse(BridgeStpLink stpLink);
    void save();

}
