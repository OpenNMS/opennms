package org.opennms.netmgt.dao.jdbc.svctype;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsServiceType;

public class ServiceTypeFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new ServiceTypeFactory(dataSource);
	}

	private ServiceTypeFactory(DataSource dataSource) {
	    this();
	    setDataSource(dataSource);
	    afterPropertiesSet();
	}
	
	public ServiceTypeFactory() {
        super(OnmsServiceType.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsServiceType)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyServiceType(getDataSource());
	}

}
