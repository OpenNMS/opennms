/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.event;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsEvent;

public class EventFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new EventFactory(dataSource);
	}

	public EventFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public EventFactory() {
        super(OnmsEvent.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsEvent)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyEvent(getDataSource());
	}

	
}