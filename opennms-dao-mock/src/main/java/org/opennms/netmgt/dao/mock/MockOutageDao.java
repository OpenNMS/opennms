package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;

public class MockOutageDao extends AbstractMockDao<OnmsOutage, Integer> implements OutageDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsOutage outage) {
        outage.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsOutage outage) {
        return outage.getId();
    }

    @Override
    public Integer currentOutageCount() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> currentOutages() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> matchingCurrentOutages(final ServiceSelector selector) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countOutagesByNode() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OutageSummary> getNodeOutageSummaries(final int rows) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
