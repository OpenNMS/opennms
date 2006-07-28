package org.opennms.report.availability;

import java.util.Date;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.annotation.Transactional;

public class AvailabilityReportSchedulerServiceTest extends AbstractTransactionalDataSourceSpringContextTests{
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"META-INF/opennms/applicationContext-dao.xml",
				"org/opennms/report/svclayer/applicationContext-svclayer.xml" };
	}

	AvailabilityReportLocatorService locatorService;
	
	AvailabilityReportSchedulerService schedulerService;
	
	@Transactional(readOnly=false)
	public void testScheduleReport() {

		Date date;
		date = new Date();
		schedulerService.Schedule("all", "html", "classic", date);
		assertNotNull(locatorService.locateReports());
		
		
	}


	public AvailabilityReportLocatorService getLocatorService() {
		return locatorService;
	}


	public void setLocatorService(AvailabilityReportLocatorService locatorService) {
		this.locatorService = locatorService;
	}


	public AvailabilityReportSchedulerService getSchedulerService() {
		return schedulerService;
	}


	public void setSchedulerService(
			AvailabilityReportSchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}
}
