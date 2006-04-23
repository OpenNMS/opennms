/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsOutage;

public class OutageFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new OutageFactory(dataSource);
	}

	public OutageFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public OutageFactory() {
        super(OnmsOutage.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsOutage)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyOutage(getDataSource());
	}

	
}