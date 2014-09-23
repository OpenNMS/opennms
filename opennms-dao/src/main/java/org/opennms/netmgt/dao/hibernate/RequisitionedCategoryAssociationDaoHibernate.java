package org.opennms.netmgt.dao.hibernate;

import java.util.List;

import org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao;
import org.opennms.netmgt.model.RequisitionedCategoryAssociation;
import org.springframework.util.Assert;

public class RequisitionedCategoryAssociationDaoHibernate extends AbstractDaoHibernate<RequisitionedCategoryAssociation, Integer> implements RequisitionedCategoryAssociationDao {

    public RequisitionedCategoryAssociationDaoHibernate() {
        super(RequisitionedCategoryAssociation.class);
    }

    @Override
    public List<RequisitionedCategoryAssociation> findByNodeId(final Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from RequisitionedCategoryAssociation as r where r.node.id = ?", nodeId);
    }

}
