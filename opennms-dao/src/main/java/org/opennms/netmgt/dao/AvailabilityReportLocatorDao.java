package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public interface AvailabilityReportLocatorDao extends OnmsDao  {
	
	public abstract void delete(AvailabilityReportLocator locator);
	
	public abstract void save(AvailabilityReportLocator locator);
    
    public abstract Collection findAll();

    public abstract Collection findByCategoryName(String categoryName);
    
    public abstract AvailabilityReportLocator get(Integer id);
	
}
