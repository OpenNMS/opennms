package org.opennms.netmgt.reporting.service;

import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.reportd.Report;

public class DefaultReportService implements ReportService {
    
    public void runReport(Report report) {
       // String fileName = new String();
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(System.getProperty("opennms.home") + "/etc/report-templates/" + report.getReportTemplate());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap());
            //LogUtils.debugf(this, "writing report file: %s", fileName);
        } catch (JRException e) {
            LogUtils.errorf(this, "error running report: %s",e.getMessage());
            e.printStackTrace();
        }  catch (Exception e){
            LogUtils.errorf(this, "Exception: %s", e.getMessage());
        }
        
    }
    
}
