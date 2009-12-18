package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;

public interface ReportService {
    
    public String runReport(Report report,String reportDirectory);

}
