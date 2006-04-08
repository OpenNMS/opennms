package org.opennms.netmgt.dao.jdbc.distpoller;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsDistPoller;

public class DistPollerFactory extends Factory {

	public static void register(DataSource dataSource) {
        new DistPollerFactory(dataSource);
	}
    
    public DistPollerFactory() {
        super(OnmsDistPoller.class);
    }

	public DistPollerFactory(DataSource dataSource) {
        this();
        setDataSource(dataSource);
        afterPropertiesSet();
	}

	protected void assignId(Object obj, Object id) {
		((LazyDistPoller)obj).setName((String)id);
	}

	protected Object create() {
		return new LazyDistPoller(getDataSource());
	}
    

}
