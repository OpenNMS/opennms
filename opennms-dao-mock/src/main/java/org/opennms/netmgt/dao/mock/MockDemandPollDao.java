package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.DemandPollDao;
import org.opennms.netmgt.model.DemandPoll;

public class MockDemandPollDao extends AbstractMockDao<DemandPoll, Integer> implements DemandPollDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final DemandPoll poll) {
        poll.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final DemandPoll poll) {
        return poll.getId();
    }

}
