package org.opennms.report.availability;

import java.util.Collection;
import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportLocatorService implements ReportLocatorService {

	private AvailabilityReportLocatorDao availabilityReportLocatorDao;
    
	public void setAvailabilityReportLocatorDao(AvailabilityReportLocatorDao availabilityReportLocatorDao) {
        this.availabilityReportLocatorDao = availabilityReportLocatorDao;
}

	public Collection locateReports() {
		// TODO Auto-generated method stub
		return availabilityReportLocatorDao.findAll();
	}

	public Collection locateReports(String categoryName) {
		// TODO Auto-generated method stub
		return availabilityReportLocatorDao.findByCategory(categoryName);
	}
	
	public void deleteReport(int id) {
		// TODO This is actually meant to delete reports, not just remove them
		availabilityReportLocatorDao.delete(id);
	}
	
	public void addReport(AvailabilityReportLocator locator) {
		availabilityReportLocatorDao.save(locator);
	}

	
}
