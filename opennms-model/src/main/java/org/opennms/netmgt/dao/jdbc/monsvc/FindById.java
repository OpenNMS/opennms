/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsMonitoredService;

public abstract class FindById extends MonitoredServiceMappingQuery {
	
	public static FindById get(DataSource ds, MonitoredServiceId id) {
		if (id.getIfIndex() == null)
			return new FindByIdNullIfIndex(ds);
		else
			return new FindByIdIfIndex(ds);
	}
    
    public FindById(DataSource ds, String sql) {
        super(ds, sql);
    }
    
    public abstract OnmsMonitoredService find(MonitoredServiceId id);
    
    
}