package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public interface AvailabilityReportLocatorDao extends OnmsDao  {
	
	public abstract void delete(int id);
	
	public abstract void save(AvailabilityReportLocator locator);
    
    public abstract Collection findAll();

    public abstract Collection findByCategory(String category);
    
    public abstract AvailabilityReportLocator get(Integer id);
	
}
