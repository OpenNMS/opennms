package org.opennms.netmgt.dao;

import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {
    volatile Set<BroadcastDomain> m_domains = new HashSet<BroadcastDomain>();

    @Override
    public synchronized void save(BroadcastDomain domain) {
        m_domains.add(domain);
    }

    @Override
    public synchronized void load(Set<BroadcastDomain> domains) {
        m_domains=domains;
    }

    @Override
    public synchronized void delete(BroadcastDomain domain) {
        m_domains.remove(domain);
    }

    @Override
    public synchronized BroadcastDomain get(int nodeid) {
        for (BroadcastDomain domain: m_domains) {
            if (domain.containBridgeId(nodeid))
                return domain;
        }
        return null;
    }

    public synchronized Set<BroadcastDomain> getAll() {
        return m_domains;
    }

    @Override
    public synchronized void clear() {
        Set<BroadcastDomain> empties = new HashSet<BroadcastDomain>();
        for (BroadcastDomain domain: m_domains) {
            if (domain.isEmpty())
                empties.add(domain);
        }
        m_domains.removeAll(empties);
    }
    

}
