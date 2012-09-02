/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.reporting.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.reportd.Parameter;
import org.opennms.netmgt.config.reportd.Report;

/**
 * <p>DefaultReportService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultReportService implements ReportService {
    
    private enum Format { pdf,html,xml,xls,csv };
    
    /** {@inheritDoc} 
     * @throws ReportRunException */
    public synchronized String runReport(Report report,String reportDirectory) throws ReportRunException {

        String outputFile = null;
        try {
            outputFile = generateReportName(reportDirectory,report.getReportName(), report.getReportFormat());
            JasperPrint print = runAndRender(report);
            outputFile = saveReport(print,report.getReportFormat(),outputFile);    
            
        } catch (JRException e) {
            LogUtils.errorf(this, e, "Error running report: %s", e.getMessage());
            throw new ReportRunException("Caught JRException: " + e.getMessage());
        }  catch (Throwable e){
            LogUtils.errorf(this, e, "Unexpected exception: %s", e.getMessage());
            throw new ReportRunException("Caught unexpected " + e.getClass().getName() + ": " + e.getMessage());
        }        
 
        return outputFile;
    
    }
 
    
    private String generateReportName(String reportDirectory, String reportName, String reportFormat){
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyyMMddHHmmss");
        return  reportDirectory + reportName + sdf.format(new Date())  + "." + reportFormat;
    }

    
    private String saveReport(JasperPrint jasperPrint, String format, String destFileName) throws JRException, Exception{
        String reportName=null;
        switch(Format.valueOf(format)){    
        case pdf:
            JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
            reportName = destFileName;
            break;
        case html:
            JasperExportManager.exportReportToHtmlFile(jasperPrint,destFileName);
            reportName = createZip(destFileName);
            break;
        case xml:
            JasperExportManager.exportReportToXmlFile(jasperPrint,destFileName,true);
            reportName = createZip(destFileName);
            break;
        case csv:
            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFileName);
            exporter.exportReport();
            reportName = destFileName;
            break;
        default:
            LogUtils.errorf(this, "Error Running Report: Unknown Format: %s",format);
        }    
        
        return reportName;
        
    }
        
    
    private JasperPrint runAndRender(Report report) throws Exception, JRException {
        JasperPrint jasperPrint = new JasperPrint();
        
        JasperReport jasperReport = JasperCompileManager.compileReport(
                                                                       System.getProperty("opennms.home") + 
                                                                       File.separator + "etc" +
                                                                       File.separator + "report-templates" + 
                                                                       File.separator + report.getReportTemplate() );
        
        if(report.getReportEngine().equals("jdbc")){
            Connection connection = DataSourceFactory.getDataSource().getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport,
                                                       paramListToMap(report.getParameterCollection()),
                                                       connection );
            connection.close();
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

    
    private String createZip(String baseFileName) {
        File reportResourceDirectory = new File(baseFileName + "_files");
        String zipFile = baseFileName + ".zip";
        
        if (reportResourceDirectory.exists() && reportResourceDirectory.isDirectory()){
            ZipOutputStream reportArchive;
        
            try {
                reportArchive = new ZipOutputStream(new FileOutputStream(zipFile));
                addFileToArchive(reportArchive,baseFileName);

                reportArchive.putNextEntry(new ZipEntry(baseFileName));
                for(String file : Arrays.asList(reportResourceDirectory.list()) ){
                    addFileToArchive(reportArchive, file);
                }
                reportArchive.close();
            }
            catch (final Exception e) {
                LogUtils.warnf(this, e, "unable to create %s", zipFile);
            }

        }

        return zipFile;
    }

    private void addFileToArchive(ZipOutputStream reportArchive, String file)
    throws FileNotFoundException, IOException {
        FileInputStream asf = new FileInputStream(file);
        reportArchive.putNextEntry(new ZipEntry(file));
        byte[] buffer = new byte[18024]; 
        int len;
        while ((len = asf.read(buffer)) > 0){
            reportArchive.write(buffer, 0, len);
        }

        asf.close();
        reportArchive.closeEntry();
    }
    
    
    private Map<String,String> paramListToMap(List<Parameter> parameters){
        Map<String,String> parmMap = new HashMap<String, String>();

        for(Parameter parm : parameters)
            parmMap.put(parm.getName(), parm.getValue());
        
        return Collections.unmodifiableMap(parmMap);
    }
    

}
