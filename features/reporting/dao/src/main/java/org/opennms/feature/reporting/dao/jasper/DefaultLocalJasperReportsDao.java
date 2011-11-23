package org.opennms.feature.reporting.dao.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.jasper.LocalJasperReport;
import org.opennms.features.reporting.model.jasper.LocalJasperReports;

public class DefaultLocalJasperReportsDao implements LocalJasperReportsDao,
        JasperReportDataProvider {

    private final String LOCAL_JASPER_REPORTS_CONFIG_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "local-jasper-reports.xml";

    private final String LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "report-templates"
            + File.separator;

    private LocalJasperReports reports;

    public DefaultLocalJasperReportsDao() {
        try {
            reports = JAXB.unmarshal(new File(LOCAL_JASPER_REPORTS_CONFIG_XML),
                                     LocalJasperReports.class);
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

    @Override
    public InputStream getTemplateStream(String id) {
        InputStream reportTemplateStream = null;
        for (LocalJasperReport report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                try {
                    reportTemplateStream = new FileInputStream(
                                                               new File(
                                                                        LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER
                                                                                + report.getTemplate()));
                } catch (FileNotFoundException e) {
                    // TODO Tak: logging and fail safety
                    e.printStackTrace();
                }
            }
        }
        return reportTemplateStream;
    }
}
