/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
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
        ((OnmsUserNotification)obj).setId((Integer)id);
    }

	protected Object create() {
		return new LazyUserNotification(getDataSource());
	}

	
}
