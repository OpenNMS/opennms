/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsEvent;

public class AlarmFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new AlarmFactory(dataSource);
	}

	public AlarmFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public AlarmFactory() {
        super(OnmsEvent.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsEvent)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyAlarm(getDataSource());
	}

	
}