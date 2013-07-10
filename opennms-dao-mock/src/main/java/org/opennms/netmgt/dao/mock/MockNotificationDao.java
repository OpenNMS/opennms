package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.OnmsNotification;

public class MockNotificationDao extends AbstractMockDao<OnmsNotification, Integer> implements NotificationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsNotification not) {
        not.setNotifyId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsNotification notif) {
        return notif.getNotifyId();
    }

}
