package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public interface AvailabilityReportLocatorDao extends OnmsDao<AvailabilityReportLocator, Integer>  {
	
	public abstract void delete(int id);
	
	public List<AvailabilityReportLocator> findByCategory(String category);
	
}
