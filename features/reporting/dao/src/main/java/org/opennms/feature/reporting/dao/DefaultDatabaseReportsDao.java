package org.opennms.feature.reporting.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.DatabaseReports;
import org.opennms.features.reporting.model.Report;

public class DefaultDatabaseReportsDao implements DatabaseReportsDao {

    private final String DATABASE_REPORTS_CONFIG_XML = 
            System.getProperty("opennms.home") + 
            File.separator + 
            "etc" + 
            File.separator + 
            "database-reports2.xml";
    
    private DatabaseReports m_reports;
    
    public DefaultDatabaseReportsDao() {
        m_reports = JAXB.unmarshal(new File(DATABASE_REPORTS_CONFIG_XML), DatabaseReports.class);
    }
    
    @Override
    public List<Report> getReports() {
       return m_reports.getReportList();
    }

    @Override
    public List<Report> getOnlineReports() {
       List<Report> onlineReports = new ArrayList<Report>();
       for (Report report : m_reports.getReportList()) {
           if (report.isOnline()) {
               onlineReports.add(report);
           }
       }
       return onlineReports;
    }

    @Override
    public String getReportService(String id) {
        for (Report report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getReportService();
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(String id) {
        for (Report report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getDisplayName();
            }
        }
        return null;
    }
    
}
