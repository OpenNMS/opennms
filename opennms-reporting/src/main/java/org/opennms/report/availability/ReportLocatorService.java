package org.opennms.report.availability;

import java.util.Collection;

public interface ReportLocatorService {
	
	Collection locateReports();

	Collection locateReports(String categoryName);
	
	void deleteReport(int id);
	

}
