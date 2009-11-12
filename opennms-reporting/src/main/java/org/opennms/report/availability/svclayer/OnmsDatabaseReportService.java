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
 * Created: November 11, 2009 jonathan@opennms.org
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
import java.util.GregorianCalendar;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.OnmsDatabaseReportConfigDao;
import org.opennms.netmgt.model.DatabaseReportCriteria;
import org.opennms.report.availability.AvailabilityCalculationException;
import org.opennms.report.availability.AvailabilityCalculator;
import org.opennms.report.availability.AvailabilityReport;
import org.opennms.report.availability.ReportMailer;
import org.opennms.report.availability.render.HTMLReportRenderer;
import org.opennms.report.availability.render.PDFReportRenderer;
import org.opennms.report.availability.render.ReportRenderException;
import org.opennms.report.availability.render.ReportRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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
 * Created: November 11, 2009 jonathan@opennms.org
 * 
 * TODO Make this match up with the DatabaseReportService interface
 * TODO Wire this as prototype
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
public class OnmsDatabaseReportService implements Runnable {
    
    private AvailabilityCalculator m_classicCalculator;

    private AvailabilityCalculator m_calendarCalculator;
    
    private OnmsDatabaseReportConfigDao m_configDao;
    
    private Category log;

    private DatabaseReportCriteria m_criteria;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String HTML_FORMAT = "HTML";
    private static final String SVG_FORMAT = "SVG";
    private static final String PDF_FORMAT = "PDF";
    private static final String CAL_TYPE = "calendar";
    
    private static final String HTML_OUTPUT_FILE_NAME = "AvailReport.html";
    private static final String SVG_OUTPUT_FILE_NAME = "SVGAvailReport.pdf";
    private static final String PDF_OUTPUT_FILE_NAME = "PDFAvailReport.pdf";
    
    public OnmsDatabaseReportService() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(OnmsDatabaseReportService.class);
        
    }

    public void run() {
        
        Resource xsltResource;
        
        AvailabilityCalculator calculator;
        String inputFileName;
        String outputFileName;
        ReportRenderer renderer;
        
        log.debug("running OpenNMS database report " + m_criteria.getReportId());
        log.debug(m_configDao.getType(m_criteria.getReportId()) + " type report selected");
        
        if (m_configDao.getType(m_criteria.getReportId()).equalsIgnoreCase(CAL_TYPE)) {
            calculator = m_calendarCalculator;
            log.debug("Calendar report format selected");
        } else {
            calculator = m_classicCalculator;
            log.debug("Classic report format selected");
        }
        
        calculator.setCalendar(new GregorianCalendar());
        
        // TODO fix this so that it uses the right name for the category
        calculator.setCategoryName(getCriteria().getCategories().get(0).getCategory());
        // TODO fix this so that it uses the right name for the date
        calculator.setPeriodEndDate(getCriteria().getDates().get(0).getDate());
        
        calculator.setLogoURL(m_configDao.getLogo(getCriteria().getReportId()));
        
        if (getCriteria().getMailFormat() == null || getCriteria().getMailFormat().equalsIgnoreCase(SVG_FORMAT)) {
            log.debug("report will be calculated as PDF with embedded SVG");
            calculator.setReportFormat(SVG_FORMAT);
        } else if (getCriteria().getMailFormat().equalsIgnoreCase(SVG_FORMAT)) {
            log.debug("report will be calculated as PDF");
            calculator.setReportFormat(PDF_FORMAT);
        } else {
            log.debug("report will be calculated as html");
            calculator.setReportFormat(HTML_FORMAT);
        }
        
        
        try {
            log.debug("Starting Availability Report Calculations");
            calculator.calculate();
            if (getCriteria().getPersist() == true) {
                inputFileName = calculator.writeLocateableXML();
            } else {
                inputFileName = calculator.writeXML();
            }
            if (getCriteria().getSendMail() == true) {
                if (getCriteria().getMailFormat().equalsIgnoreCase(HTML_FORMAT)) {
                    renderer = new HTMLReportRenderer();
                    xsltResource =  new UrlResource(m_configDao.getHtmlStylesheetLocation(getCriteria().getReportId()));
                    outputFileName = HTML_OUTPUT_FILE_NAME;
                } else {
                    renderer = new PDFReportRenderer();
                    if (getCriteria().getMailFormat().equalsIgnoreCase(SVG_FORMAT)) {
                        xsltResource =  new UrlResource(m_configDao.getSvgStylesheetLocation(getCriteria().getReportId()));
                        outputFileName = SVG_OUTPUT_FILE_NAME;
             
                    } else {
                        xsltResource =  new UrlResource(m_configDao.getPdfStylesheetLocation(getCriteria().getReportId()));
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
                                                       getCriteria().getMailTo(),
                                                       baseDir + "/" + outputFileName);
                mailer.send();
            }
            
        } catch (AvailabilityCalculationException ce) {
            log.fatal("Unable to calculate report data ", ce);
        } catch (MalformedURLException e) {
            log.fatal("Malformed URL for xslt template");
        } catch (ReportRenderException e) {
            log.fatal("unable to render report");
        } catch (IOException e) {
            log.fatal("unable to mail report");
        }


    }
    
    public void setCalendarCalculator(AvailabilityCalculator calculator) {
        m_calendarCalculator = calculator;
    }

    public void setClassicCalculator(AvailabilityCalculator calulator) {
        m_classicCalculator = calulator;
    }

    public void setCriteria(DatabaseReportCriteria criteria) {
        m_criteria = criteria;
    }

    public DatabaseReportCriteria getCriteria() {
        return m_criteria;
    }

    public void setConfigDao(OnmsDatabaseReportConfigDao configDao) {
        m_configDao = configDao;
    }

}
