package org.opennms.features.reporting.dao.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.jasperreport.JasperReportDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLocalJasperReportsDao implements LocalJasperReportsDao {

    Logger logger = LoggerFactory.getLogger(LegacyLocalJasperReportsDao.class.getSimpleName());
    
    private final String LOCAL_JASPER_REPORTS_CONFIG_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "local-jasper-reports.xml";

    //TODO Tak: move out of dao
    private final String LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "report-templates"
            + File.separator;

    private LocalJasperReports reports;

    public LegacyLocalJasperReportsDao() {
        try {
            reports = JAXB.unmarshal(new File(LOCAL_JASPER_REPORTS_CONFIG_XML),
                                     LocalJasperReports.class);
        } catch (Exception e) {
            logger.error("Faild to unmarshal file '{}', '{}'", LOCAL_JASPER_REPORTS_CONFIG_XML, e);
            e.printStackTrace();
        }
    }

    @Override
    public String getTemplateLocation(String id) {
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getTemplate();
            }
        }
        return null;
    }

    @Override
    public String getEngine(String id) {
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getEngine();
            }
        }
        return null;
    }
    
    @Override
    public InputStream getTemplateStream(String id) {
        InputStream reportTemplateStream = null;
        for (JasperReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                try {
                    reportTemplateStream = new FileInputStream(
                                                               new File(
                                                                        LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER
                                                                                + report.getTemplate()));
                } catch (FileNotFoundException e) {
                    logger.error("Template file '{}' at folder '{}' not found", report.getTemplate(), LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER);
                    e.printStackTrace();
                }
            }
        }
        return reportTemplateStream;
    }
}
