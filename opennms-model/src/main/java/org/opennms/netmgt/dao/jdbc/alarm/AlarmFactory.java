/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsAlarm;

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
        super(OnmsAlarm.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsAlarm)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyAlarm(getDataSource());
	}

	
}