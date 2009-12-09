package org.opennms.netmgt.reporting.service;

import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;

import org.opennms.core.utils.LogUtils;

public class DefaultReportService implements ReportService{
    
    public void runReport(String report, String[] emailDestinations) {
        String fileName = new String();
        try {
            fileName = JasperRunManager.runReportToPdfFile(report,new HashMap());
            LogUtils.debugf(this, "writing report file: %s", fileName);
        } catch (JRException e) {
            LogUtils.errorf(this, "error running report: %s",e.getMessage());
            e.printStackTrace();
        }    
        
    }
    
}
