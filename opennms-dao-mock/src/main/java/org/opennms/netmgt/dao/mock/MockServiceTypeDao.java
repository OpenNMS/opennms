package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;

public class MockServiceTypeDao extends AbstractMockDao<OnmsServiceType, Integer> implements ServiceTypeDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsServiceType service) {
        service.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsServiceType service) {
        return service.getId();
    }

    @Override
    public OnmsServiceType findByName(final String name) {
        if (name == null) return null;
        for (final OnmsServiceType serviceType : findAll()) {
            if (name.equals(serviceType.getName())) {
                return serviceType;
            }
        }
        return null;
    }

}
