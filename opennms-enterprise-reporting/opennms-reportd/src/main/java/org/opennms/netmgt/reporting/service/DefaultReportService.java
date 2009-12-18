package org.opennms.netmgt.reporting.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.config.reportd.Parameter;

public class DefaultReportService implements ReportService {
    
    private enum Format { pdf,html,xml,xls };
    
    public String runReport(Report report,String reportDirectory) {
            
        String outputFile = null;
        try {
            outputFile = generateReportName(reportDirectory,report.getReportName(), report.getReportFormat());
            JasperPrint print = runAndRender(report);
            saveReport(print,report.getReportFormat(),outputFile);
            
        } catch (JRException e) {
            LogUtils.errorf(this, "error running report: %s",e.getMessage());
        }  catch (Exception e){
            LogUtils.errorf(this, "Exception: %s", e.getMessage());
        }        
 
        return outputFile;
    
    }
 
    private String generateReportName(String reportDirectory, String reportName, String reportFormat){
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setCalendar(Calendar.getInstance());
        sdf.applyPattern("yyyymmddhhmm");
        String fileName = reportDirectory + reportName + sdf.toString()  + "." + reportFormat;
        return fileName;
    }
    
    private void saveReport(JasperPrint jasperPrint, String format, String destFileName) throws JRException, Exception{
        
        switch(Format.valueOf(format)){    
            case pdf:
                JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
            case html:
                JasperExportManager.exportReportToHtmlFile(jasperPrint,destFileName);
            case xml:
                JasperExportManager.exportReportToXmlFile(jasperPrint,destFileName,true);
            default:
                LogUtils.errorf(this, "Error Running Report: Unknown Format: %s",format);
       }    
    }
        
    
    private JasperPrint runAndRender(Report report) throws Exception, JRException {
        JasperPrint jasperPrint = new JasperPrint();
        
        JasperReport jasperReport = JasperCompileManager.compileReport(
                                                                       System.getProperty("opennms.home") + 
                                                                       "/etc/report-templates/" + 
                                                                       report.getReportTemplate() );
        if(report.getReportEngine().equals("jdbc")){
            jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                       paramListToMap(report.getParameterCollection()),
                                                       DataSourceFactory.getDataSource().getConnection() );              
        }
        
        else if(report.getReportEngine().equals("opennms")){
            LogUtils.errorf(this, "Sorry the OpenNMS Data source engine is not yet available");
            jasperPrint = null;
        }
        else{
            LogUtils.errorf(this,"Unknown report engine: %s ", report.getReportEngine());
            jasperPrint = null;
        }
        
        
        return jasperPrint;
        
        
        
    }
    
    
    private Map<String,String> paramListToMap(List<Parameter> parameters){
        Map<String,String> parmMap = new HashMap();

        for(Parameter parm : parameters)
            parmMap.put(parm.getName(), parm.getValue());
        
        return parmMap;
    }
    
}
