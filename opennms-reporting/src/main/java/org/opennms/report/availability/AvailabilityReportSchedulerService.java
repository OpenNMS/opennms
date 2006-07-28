package org.opennms.report.availability;

import java.util.Date;

import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportSchedulerService implements
		ReportSchedulerService {
	
	AvailabilityReportLocatorService locatorService;
	private AvailabilityReportLocatorDao availabilityReportLocatorDao;
	
	public void setAvailabilityReportLocatorDao(AvailabilityReportLocatorDao dao) {
		availabilityReportLocatorDao = dao;
	}
	
	public void Schedule(String category, String type, String format, Date date) {
		
		AvailabilityReportLocator locator = new AvailabilityReportLocator();
		locator.setCategory(category);
		locator.setFormat(format);
		locator.setType(type);
		locator.setDate(date);
		locator.setLocation("not yet available");
		locator.setAvailable(false);
		locatorService = new AvailabilityReportLocatorService();
		locatorService.setAvailabilityReportLocatorDao(availabilityReportLocatorDao);
		//		TODO add 
		locatorService.addReport(locator);
		System.out.println("should have added a report to the locator");
	}

}
