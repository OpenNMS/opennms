package org.opennms.report.availability;

import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public interface ReportLocatorService {
	
	Collection locateReports();

	Collection locateReports(String categoryName);
	
	void deleteReport(int id);
	
	void addReport(AvailabilityReportLocator locator);
	
}
