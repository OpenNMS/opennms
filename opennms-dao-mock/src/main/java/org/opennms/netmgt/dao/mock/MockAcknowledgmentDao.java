package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;

public class MockAcknowledgmentDao extends AbstractMockDao<OnmsAcknowledgment, Integer> implements AcknowledgmentDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsAcknowledgment ack) {
        ack.setId(m_id.incrementAndGet());
    }

    @Override
    public Integer getId(final OnmsAcknowledgment ack) {
        return ack.getId();
    }

    @Override
    public List<Acknowledgeable> findAcknowledgables(final OnmsAcknowledgment ack) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void updateAckable(final Acknowledgeable ackable) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void processAck(final OnmsAcknowledgment ack) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void processAcks(final Collection<OnmsAcknowledgment> acks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
