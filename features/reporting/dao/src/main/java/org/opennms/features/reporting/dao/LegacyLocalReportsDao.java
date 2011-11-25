package org.opennms.features.reporting.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;

//TODO Tak: add Interface for BasicReportDataProvider
public class LegacyLocalReportsDao implements LocalReportsDao {

    private final String LOCAL_REPORTS_CONFIG_XML = 
            System.getProperty("opennms.home") + 
            File.separator + 
            "etc" + 
            File.separator + 
            "local-reports.xml";
    
    private LegacyLocalReportsDefinition m_reports;
    
    public LegacyLocalReportsDao() {
        try {
            m_reports = JAXB.unmarshal(new File(LOCAL_REPORTS_CONFIG_XML), LegacyLocalReportsDefinition.class);
        }catch (Exception e) {
            // TODO Tak: logging and fail safety 
        }
    }
    
    @Override
    public List<BasicReportDefinition> getReports() {
       return m_reports.getReportList();
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
       List<BasicReportDefinition> onlineReports = new ArrayList<BasicReportDefinition>();
       for (BasicReportDefinition report : m_reports.getReportList()) {
           if (report.getOnline()) {
               onlineReports.add(report);
           }
       }
       return onlineReports;
    }

    @Override
    public String getReportService(String id) {
        for (BasicReportDefinition report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getReportService();
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(String id) {
        for (BasicReportDefinition report : m_reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getDisplayName();
            }
        }
        return null;
    }
    
}
