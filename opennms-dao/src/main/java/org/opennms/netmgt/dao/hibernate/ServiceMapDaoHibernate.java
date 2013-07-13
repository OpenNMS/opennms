package org.opennms.netmgt.dao.hibernate;

import java.util.List;

import org.opennms.netmgt.dao.api.ServiceMapDao;
import org.opennms.netmgt.model.OnmsServiceMap;

public class ServiceMapDaoHibernate extends AbstractDaoHibernate<OnmsServiceMap, Integer> implements ServiceMapDao{

    public ServiceMapDaoHibernate() {
        super(OnmsServiceMap.class);
    }
    
    public List<OnmsServiceMap> findbyIpAddr(String ipaddr) {
        String query = "from OnmsServiceMap as serviceMap where serviceMap.ipAddr = ?";
        return find(query,ipaddr);
    }
    
    public OnmsServiceMap findbyIpAddrAndServiceName(String ipaddr, String serviceName) {
        String query = "from OnmsServiceMap as serviceMap where serviceMap.ipAddr = ? and serviceMap.serviceMapName = ?";
        return findUnique(query,ipaddr,serviceName);
    }
}
