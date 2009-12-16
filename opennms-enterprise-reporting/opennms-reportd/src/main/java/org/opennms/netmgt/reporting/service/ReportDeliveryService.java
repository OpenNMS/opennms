package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;

public interface ReportDeliveryService {

    public void deliverReport(Report report,String fileName);
    
}
