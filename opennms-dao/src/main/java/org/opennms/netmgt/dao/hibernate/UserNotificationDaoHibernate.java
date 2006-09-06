package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.UserNotificationDao;
import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationDaoHibernate extends
		AbstractDaoHibernate<OnmsUserNotification, Integer> implements
		UserNotificationDao {

	public UserNotificationDaoHibernate() {
		super(OnmsUserNotification.class);
	}

}
