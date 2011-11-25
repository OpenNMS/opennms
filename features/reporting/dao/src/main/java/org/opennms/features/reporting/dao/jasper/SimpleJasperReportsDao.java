package org.opennms.features.reporting.dao.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.JasperReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportsDefinition;

public class SimpleJasperReportsDao implements LocalReportsDao, LocalJasperReportsDao {

    private final String SIMPLE_JASPER_REPORTS_CONFIG_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "simple-jasper-reports.xml";

    private SimpleJasperReportsDefinition reports;

    public SimpleJasperReportsDao() {
        try {
            reports = JAXB.unmarshal(new File(SIMPLE_JASPER_REPORTS_CONFIG_XML),
                                     SimpleJasperReportsDefinition.class);
        } catch (Exception e) {
            // TODO Tak: logging and fail safety
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
                    reportTemplateStream = new FileInputStream(new File(URI.create(report.getTemplate())));
                } catch (FileNotFoundException e) {
                    // TODO Tak: logging and fail safety
                    e.printStackTrace();
                }
            }
        }
        return reportTemplateStream;
    }

    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        resultList.addAll(reports.getReportList());
        return resultList;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        for (BasicReportDefinition report : reports.getReportList()) {
            if (report.getOnline()) {
                resultList.add(report);
            }
        }
        return resultList;
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        for (BasicReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                report.getReportService();
            }
        }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        for (BasicReportDefinition report : reports.getReportList()) {
            if (id.equals(report.getId())) {
                report.getDescription();
            }
        }
        return result;
    }
}
