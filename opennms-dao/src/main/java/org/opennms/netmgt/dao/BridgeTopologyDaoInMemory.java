package org.opennms.netmgt.dao;

import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {

    @Override
    public void delete(int nodeid) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parse(BridgeMacLink maclink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void parse(BridgeStpLink stplink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeElement bridge) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeMacLink maclink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeBridgeLink bridgelink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void walked(int nodeid) {
        // TODO Auto-generated method stub

    }

    @Override
    public BroadcastDomain getBroadcastDomain(int nodeid) {
        // TODO Auto-generated method stub
        return null;
    }

}
