package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.UserNotificationDao;
import org.opennms.netmgt.model.OnmsUserNotification;

public class MockUserNotificationDao extends AbstractMockDao<OnmsUserNotification, Integer> implements UserNotificationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsUserNotification not) {
        not.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsUserNotification userNotif) {
        return userNotif.getId();
    }

}
