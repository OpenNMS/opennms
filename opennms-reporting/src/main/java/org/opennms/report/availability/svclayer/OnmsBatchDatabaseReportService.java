/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 23, 2009 jonathan@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.report.availability.svclayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.OnmsDatabaseReportConfigDao;
import org.opennms.report.availability.AvailabilityCalculationException;
import org.opennms.report.availability.AvailabilityCalculator;
import org.opennms.report.availability.ReportMailer;
import org.opennms.report.availability.render.HTMLReportRenderer;
import org.opennms.report.availability.render.PDFReportRenderer;
import org.opennms.report.availability.render.ReportRenderException;
import org.opennms.report.availability.render.ReportRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class OnmsBatchDatabaseReportService implements BatchReportService, ReportValidationService {
    
    private AvailabilityCalculator m_classicCalculator;

    private AvailabilityCalculator m_calendarCalculator;
    
    private OnmsDatabaseReportConfigDao m_configDao;
    
    private Category log;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String HTML_FORMAT = "HTML";
    private static final String SVG_FORMAT = "SVG";
    private static final String PDF_FORMAT = "PDF";
    private static final String CAL_TYPE = "calendar";
    
    private static final String HTML_OUTPUT_FILE_NAME = "AvailReport.html";
    private static final String SVG_OUTPUT_FILE_NAME = "SVGAvailReport.pdf";
    private static final String PDF_OUTPUT_FILE_NAME = "PDFAvailReport.pdf";
    
    public OnmsBatchDatabaseReportService() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(OnmsBatchDatabaseReportService.class);
        
    }
    
    
    /* (non-Javadoc)
     * @see org.opennms.report.availability.svclayer.BatchDatabaseReportService#validate(java.util.HashMap, java.lang.String)
     */
    public boolean validate(HashMap<String, Object> reportParms, String reportID){
        
        if (!reportParms.containsKey("endDate")) {
            log.fatal("report parameters should contain parameter endDate");
            return false;
        }
        
        if (!(reportParms.get("endDate") instanceof Date)){
            log.fatal("report parameters 'endDate' should be a Date");
            return false;
        }

        if (!reportParms.containsKey("reportCategory")) {
            log.fatal("report parameters should contain parameter reportCategory");
            return false;
        }

        if (!(reportParms.get("reportCategory") instanceof String)){
            log.fatal("report parameter 'reportCategory' should be a String");
            return false;
        }
        
        return true;
        
    }

    /* (non-Javadoc)
     * @see org.opennms.report.availability.svclayer.BatchDatabaseReportService#runAndPersist(java.util.HashMap, java.lang.String)
     */
    public void runAndPersist(HashMap<String, Object> reportParms, String reportID) {
        
        runInternal(reportParms, reportID);
        
    }
    
    private String runInternal(HashMap<String, Object> reportParms, String reportId) {
        
        AvailabilityCalculator calculator;
        String reportFileName = null;
        
        log.debug("running OpenNMS database report " + reportId);
        
        if (m_configDao.getType(reportId).equalsIgnoreCase(CAL_TYPE)) {
            calculator = m_calendarCalculator;
            log.debug("Calendar report format selected");
        } else {
            calculator = m_classicCalculator;
            log.debug("Classic report format selected");
        }
        
        calculator.setCategoryName((String) reportParms.get("reportCategory"));
        
        calculator.setCalendar(new GregorianCalendar());
        calculator.setPeriodEndDate((Date) reportParms.get("endDate"));
        
        calculator.setLogoURL(m_configDao.getLogo(reportId));
        

        // have the calculator calculate everything to enable any of the templates to work
        // This will have some performance impact.
        
        calculator.setReportFormat("all");
        
        log.debug("Starting Availability Report Calculations");
        try {
            calculator.calculate();
            reportFileName = calculator.writeLocateableXML();
        } catch (AvailabilityCalculationException ce) {
            log.fatal("Unable to calculate report data ", ce);
        }

        return reportFileName;
        
    }
    
    
    /* (non-Javadoc)
     * @see org.opennms.report.availability.svclayer.BatchDatabaseReportService#runAndMail(java.util.HashMap, java.lang.String, java.lang.String, java.lang.String)
     */
    public void runAndMail(HashMap<String, Object> reportParms, String reportId, String recipient, String format) {
    
        Resource xsltResource;
        String inputFileName;
        String outputFileName;
        ReportRenderer renderer;
        
        inputFileName = runInternal(reportParms, reportId);
        
        if (inputFileName != null) {
 
            try {
                
                if (format.equalsIgnoreCase(HTML_FORMAT)) {
                    renderer = new HTMLReportRenderer();
                    xsltResource =  new UrlResource(m_configDao.getHtmlStylesheetLocation(reportId));
                    outputFileName = HTML_OUTPUT_FILE_NAME;
                } else {
                    renderer = new PDFReportRenderer();
                    if (format.equalsIgnoreCase(SVG_FORMAT)) {
                        xsltResource =  new UrlResource(m_configDao.getSvgStylesheetLocation(reportId));
                        outputFileName = SVG_OUTPUT_FILE_NAME;
             
                    } else {
                        xsltResource =  new UrlResource(m_configDao.getPdfStylesheetLocation(reportId));
                        outputFileName = PDF_OUTPUT_FILE_NAME;
                    }
                }
                String baseDir = System.getProperty("opennms.report.dir");
                log.debug("render base dir: " + baseDir);
                log.debug("render input file: " + inputFileName);
                log.debug("render output file: " + outputFileName);
                log.debug("render template: " + xsltResource);
                renderer.setBaseDir(baseDir);
                renderer.render(inputFileName, outputFileName, xsltResource);
                ReportMailer mailer = new ReportMailer(
                                                       recipient,
                                                       baseDir + "/" + outputFileName);
                mailer.send();
                
            } catch (MalformedURLException e) {
                log.fatal("Malformed URL for xslt template");
            } catch (ReportRenderException e) {
                log.fatal("unable to render report");
            } catch (IOException e) {
                log.fatal("unable to mail report");
            }
        }


    }
    
    public void setCalendarCalculator(AvailabilityCalculator calculator) {
        m_calendarCalculator = calculator;
    }

    public void setClassicCalculator(AvailabilityCalculator calulator) {
        m_classicCalculator = calulator;
    }

    public void setConfigDao(OnmsDatabaseReportConfigDao configDao) {
        m_configDao = configDao;
    }

}
