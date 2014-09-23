package org.opennms.netmgt.dao.api;

import java.util.List;

import org.opennms.netmgt.model.RequisitionedCategoryAssociation;

public interface RequisitionedCategoryAssociationDao extends OnmsDao<RequisitionedCategoryAssociation, Integer> {
    public List<RequisitionedCategoryAssociation> findByNodeId(final Integer nodeId);
}
