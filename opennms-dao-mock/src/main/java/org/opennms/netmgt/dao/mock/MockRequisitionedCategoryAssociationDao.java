package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao;
import org.opennms.netmgt.model.RequisitionedCategoryAssociation;

public class MockRequisitionedCategoryAssociationDao extends AbstractMockDao<RequisitionedCategoryAssociation, Integer> implements RequisitionedCategoryAssociationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public void save(final RequisitionedCategoryAssociation cat) {
        super.save(cat);
        updateSubObjects(cat);
    }

    private void updateSubObjects(final RequisitionedCategoryAssociation cat) {
        getNodeDao().save(cat.getNode());
        getCategoryDao().save(cat.getCategory());
    }

    @Override
    public List<RequisitionedCategoryAssociation> findByNodeId(final Integer nodeId) {
        final List<RequisitionedCategoryAssociation> ret = new ArrayList<>();
        if (nodeId != null) {
            for (final RequisitionedCategoryAssociation assoc : findAll()) {
                if (assoc.getNode() != null && nodeId.equals(assoc.getNode().getId())) {
                    ret.add(assoc);
                }
            }
        }
        return ret;
    }

    @Override
    protected Integer getId(final RequisitionedCategoryAssociation entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final RequisitionedCategoryAssociation entity) {
        entity.setId(m_id.incrementAndGet());
    }

}
