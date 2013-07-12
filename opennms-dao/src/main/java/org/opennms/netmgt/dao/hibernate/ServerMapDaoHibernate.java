package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.model.OnmsServerMap;

public class ServerMapDaoHibernate extends AbstractDaoHibernate<OnmsServerMap, Integer> {

    public ServerMapDaoHibernate() {
        super(OnmsServerMap.class);
    }

    public OnmsServerMap findByIpAddrAndServerName(String ipAddr, String serverName) {
        String query = "from OnmsServerMap as serverMap where serverMap.ipAddr = ? and serverMap.serverName = ?";
        return findUnique(query, ipAddr, serverName);
    }
}
