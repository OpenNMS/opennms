package org.opennms.netmgt.dao.mock;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;

public class MockAlarmDao extends AbstractMockDao<OnmsAlarm, Integer> implements AlarmDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public void save(final OnmsAlarm alarm) {
        super.save(alarm);
        updateSubObjects(alarm);
    }

    @Override
    public void update(final OnmsAlarm alarm) {
        super.update(alarm);
        updateSubObjects(alarm);
    }

    private void updateSubObjects(final OnmsAlarm alarm) {
        getDistPollerDao().save(alarm.getDistPoller());
        getEventDao().save(alarm.getLastEvent());
        getNodeDao().save(alarm.getNode());
        getServiceTypeDao().save(alarm.getServiceType());
    }

    @Override
    protected void generateId(final OnmsAlarm alarm) {
        alarm.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsAlarm alarm) {
        return alarm.getId();
    }

    @Override
    public OnmsAlarm findByReductionKey(final String reductionKey) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummaries(final Integer... nodeIds) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
