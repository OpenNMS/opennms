package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public interface AvailabilityReportLocatorDao extends OnmsDao<AvailabilityReportLocator, Integer>  {
	
	public abstract void delete(int id);
	
	public Collection<AvailabilityReportLocator> findByCategory(String category);
	
}
