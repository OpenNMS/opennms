package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;

public class MockApplicationDao extends AbstractMockDao<OnmsApplication, Integer> implements ApplicationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsApplication app) {
        app.setId(m_id.incrementAndGet());
    }

    @Override
    public Integer getId(final OnmsApplication app) {
        return app.getId();
    }

    @Override
    public OnmsApplication findByName(final String label) {
        if (label == null) return null;
        for (final OnmsApplication app : findAll()) {
            if (label.equals(app.getName())) {
                return app;
            }
        }
        return null;
    }

}
