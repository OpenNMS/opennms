/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.notification;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsNotification;

public class NotificationFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new NotificationFactory(dataSource);
	}

	public NotificationFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public NotificationFactory() {
        super(OnmsNotification.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsNotification)obj).setNotifyId((Integer)id);
	}

	protected Object create() {
		return new LazyNotification(getDataSource());
	}

	
}