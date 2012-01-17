package org.opennms.netmgt.jasper.resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;

import org.junit.Before;
import org.junit.Test;

public class ResourceQueryExecuterTest {
    
    private JasperReport m_jasperReport;
    private JasperPrint m_jasperPrint;
    
    @Before
    public void setUp() {
        File reportDir = new File("target/reports");
        reportDir.mkdirs();
    }
    
    
    @Test
    public void test() throws JRException {
        compile();
        fill();
        pdf();
        xhtml();
    }


    private void xhtml() throws JRException {
        File destFile = new File("target/reports/ResourceTypeTest.x.html");
        
        JRXhtmlExporter exporter = new JRXhtmlExporter();
        
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, m_jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
        
        exporter.exportReport();
    }


    private void pdf() throws JRException {
        JasperExportManager.exportReportToPdfFile(m_jasperPrint, "target/reports/ResourceTypeTest.pdf");
    }


    private void fill() throws JRException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nodeid", 1);
        params.put("resourceType", "nsVpnMonitor");
        
        m_jasperPrint = JasperFillManager.fillReport(m_jasperReport, params);
    }


    private void compile() throws JRException {
        m_jasperReport = JasperCompileManager.compileReport("src/test/resources/ResourceTest.jrxml");
    }

}
