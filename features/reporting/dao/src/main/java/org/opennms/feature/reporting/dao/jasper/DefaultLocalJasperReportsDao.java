package org.opennms.feature.reporting.dao.jasper;

import java.io.File;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.jasper.LocalJasperReport;
import org.opennms.features.reporting.model.jasper.LocalJasperReports;

public class DefaultLocalJasperReportsDao implements LocalJasperReportsDao {
    
    private final String LOCAL_JASPER_REPORTS_CONFIG_XML = 
            System.getProperty("opennms.home") + 
            File.separator + 
            "etc" + 
            File.separator + 
            "local-jasper-reports.xml";
    
    private LocalJasperReports reports;

    public DefaultLocalJasperReportsDao() {
        try {
            reports = JAXB.unmarshal(new File(LOCAL_JASPER_REPORTS_CONFIG_XML), LocalJasperReports.class);
        } catch (Exception e) {
            // TODO Tak: logging and fail safety
        }
    }

    @Override
    public String getTemplateLocation(String id) {
        for (LocalJasperReport report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getTemplate();
            }
        }
        return null;
    }

    @Override
    public String getEngine(String id) {
        for (LocalJasperReport report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getEngine();
            }
        }
        return null;
    }
}
