package org.opennms.feature.reporting.dao;

import java.util.List;

import org.opennms.features.reporting.model.Report;

public interface BasicReportDataProvider {
    
    public List<Report> getReports();

    public List<Report> getOnlineReports();
    
    public String getReportService(String id);

    public String getDisplayName(String id);
    
}
