/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.dao.jdbc.snmpif.SnmpInterfaceId;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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
        userNotification.setId(userNotificationId.getNotifyID());
    }

	protected Object create() {
		return new LazyUserNotification(getDataSource());
	}

	
}
