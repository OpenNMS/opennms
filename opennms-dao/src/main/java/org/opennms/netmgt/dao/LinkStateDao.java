package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsLinkState;

public interface LinkStateDao extends OnmsDao<OnmsLinkState, Integer> {
    Collection<OnmsLinkState> findAll(Integer offset, Integer limit);
    OnmsLinkState findById(Integer id);
    OnmsLinkState findByDataLinkInterfaceId(Integer interfaceId);
    Collection<OnmsLinkState> findByNodeId(Integer nodeId);
    Collection<OnmsLinkState> findByNodeParentId(Integer nodeParentId);
}
