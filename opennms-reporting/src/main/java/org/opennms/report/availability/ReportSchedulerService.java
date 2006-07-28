package org.opennms.report.availability;

import java.util.Date;

public interface ReportSchedulerService {

	void Schedule(String category, String type, String format, Date date);
	
}
