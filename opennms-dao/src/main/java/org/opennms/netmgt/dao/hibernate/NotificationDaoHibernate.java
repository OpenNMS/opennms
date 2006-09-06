package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.model.OnmsNotification;

public class NotificationDaoHibernate extends
		AbstractDaoHibernate<OnmsNotification, Integer> implements
		NotificationDao {

	public NotificationDaoHibernate() {
		super(OnmsNotification.class);
	}

}
