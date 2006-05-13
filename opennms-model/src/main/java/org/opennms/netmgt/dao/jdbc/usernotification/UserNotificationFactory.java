/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new UserNotificationFactory(dataSource);
	}

	public UserNotificationFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public UserNotificationFactory() {
        super(OnmsUserNotification.class);
    }

    protected void assignId(Object obj, Object id) {
        OnmsUserNotification userNotification = (OnmsUserNotification) obj;
        UserNotificationId userNotificationId = (UserNotificationId) id;
        userNotification.setUserId(userNotificationId.getUserID());
        userNotification.setNotification(getNotification(userNotificationId.getNotifyID()));
    }

    private OnmsNotification getNotification(Integer notifyId) {
        return (OnmsNotification)Cache.obtain(OnmsNotification.class, notifyId);
    }

	protected Object create() {
		return new LazyUserNotification(getDataSource());
	}

	
}
