package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsLinkState;

public interface LinkStateDao extends OnmsDao<OnmsLinkState, Integer> {
    public abstract Collection<OnmsLinkState> findAll(Integer offset, Integer limit);
    public abstract OnmsLinkState findById(Integer id);
    public abstract OnmsLinkState findByDataLinkInterfaceId(Integer interfaceId);
    public abstract Collection<OnmsLinkState> findByNodeId(Integer nodeId);
    public abstract Collection<OnmsLinkState> findByNodeParentId(Integer nodeParentId);
}
