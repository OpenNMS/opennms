package org.opennms.netmgt.dao.hibernate;

import java.util.List;

import org.opennms.netmgt.dao.api.ServerMapDao;
import org.opennms.netmgt.model.OnmsServerMap;

public class ServerMapDaoHibernate extends AbstractDaoHibernate<OnmsServerMap, Integer> implements ServerMapDao{

    public ServerMapDaoHibernate() {
        super(OnmsServerMap.class);
    }

    public List<OnmsServerMap> findByIpAddrAndServerName(String ipAddr, String serverName) {
        String query = "from OnmsServerMap as serverMap where serverMap.ipAddr = ? and serverMap.serverName = ?";
        return find(query, ipAddr, serverName);
    }
}
