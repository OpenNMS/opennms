package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.ResourceReferenceDao;
import org.opennms.netmgt.model.ResourceReference;

public class MockResourceReferenceDao extends AbstractMockDao<ResourceReference, Integer> implements ResourceReferenceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public ResourceReference getByResourceId(final String resourceId) {
        for (final ResourceReference reference : findAll()) {
            if (resourceId.equals(reference.getResourceId())) {
                return reference;
            }
        }
        return null;
    }

    @Override
    protected Integer getId(final ResourceReference reference) {
        return reference.getId();
    }

    @Override
    protected void generateId(final ResourceReference reference) {
        reference.setId(m_id.incrementAndGet());
    }

}
