package org.opennms.netmgt.dao.mock;

import java.util.UUID;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;

public class MockDistPollerDao extends AbstractMockDao<OnmsDistPoller,String> implements DistPollerDao {
    @Override
    protected void generateId(final OnmsDistPoller dp) {
        dp.setName(UUID.randomUUID().toString());
    }

    @Override
    protected String getId(final OnmsDistPoller dp) {
        return dp == null? null : dp.getName();
    }
}
