//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.report.availability;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.report.availability.render.ReportRenderException;
import org.opennms.report.availability.render.ReportRenderer;

/**
 * Send an availability report to the intended recipient.
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class AvailabilityReportRunner implements Runnable {

    private String m_logo;

    private String m_categoryName;

    private String m_monthFormat;

    private String m_format;

    private String m_email;

    private Date m_periodEndDate;

    private AvailabilityCalculator m_classicCalculator;

    private AvailabilityCalculator m_calendarCalculator;

    private ReportRenderer m_htmlReportRenderer;

    private ReportRenderer m_pdfReportRenderer;

    private ReportRenderer m_svgReportRenderer;

    private Category log;

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String HTML_FORMAT = "HTML";

    private static final String SVG_FORMAT = "SVG";

    private static final String PDF_FORMAT = "PDF";

    /**
     * <p>Constructor for AvailabilityReportRunner.</p>
     */
    public AvailabilityReportRunner() {

        // TODO: sorto out logo bits here

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(AvailabilityReport.class);
        log.debug("initialised AvailablilityReportMailer");

        /*
         * ApplicationContext context = new ClassPathXmlApplicationContext(
         * new String[]
         * {"META-INF/opennms/standaloneApplicationContext-reporting.xml",
         * "META-INF/opennms/applicationContext-reporting.xml"}); BeanFactory
         * bf = (BeanFactory) context;
         */

    }

    /**
     * <p>run</p>
     */
    public void run() {

        ReportRenderer renderer;
        AvailabilityCalculator calculator;

        if (m_monthFormat == null || m_monthFormat.equals("")
                || m_monthFormat.equals("classic")) {
            calculator = m_classicCalculator;
        } else {
            calculator = m_calendarCalculator;
        }

        calculator.setCalendar(new GregorianCalendar());
        calculator.setCategoryName(m_categoryName);
        calculator.setLogoURL(m_logo);
        calculator.setPeriodEndDate(m_periodEndDate);
        
        if (m_format == null || m_format.equals(SVG_FORMAT)) {
            log.debug("report will be rendered as PDF with embedded SVG");
            renderer = m_svgReportRenderer;
            calculator.setReportFormat(SVG_FORMAT);
        } else if (m_format.equals(PDF_FORMAT)) {
            log.debug("report will be rendered as PDF");
            renderer = m_pdfReportRenderer;
            calculator.setReportFormat(PDF_FORMAT);
        } else {
            log.debug("report will be rendered as html");
            renderer = m_htmlReportRenderer;
            // renderer.setOutputFileName(categoryName + "-adhoc.html");
            calculator.setReportFormat(HTML_FORMAT);
        }

        try {
            log.debug("Starting Availability Report Calculations");
            calculator.calculate();
            calculator.writeLocateableXML();
            String outputFile = calculator.getOutputFileName();
            log.debug("Written Availability Report as XML to " + outputFile);
            renderer.setInputFileName(outputFile);
            log.debug("rendering XML " + outputFile + " as "
                    + renderer.getOutputFileName());
            renderer.render();
            ReportMailer mailer = new ReportMailer(
                                                   m_email,
                                                   renderer.getBaseDir()
                                                           + renderer.getOutputFileName());
            mailer.send();
        } catch (AvailabilityCalculationException ce) {
            log.fatal("Unable to calculate report data ", ce);
        } catch (ReportRenderException re) {
            log.fatal("Unable to render report ", re);
        } catch (IOException ioe) {
            log.fatal("Unable to render report ", ioe);
        }

    }

    /**
     * <p>setCalendarCalculator</p>
     *
     * @param calculator a {@link org.opennms.report.availability.AvailabilityCalculator} object.
     */
    public void setCalendarCalculator(AvailabilityCalculator calculator) {
        m_calendarCalculator = calculator;
    }

    /**
     * <p>setClassicCalculator</p>
     *
     * @param calulator a {@link org.opennms.report.availability.AvailabilityCalculator} object.
     */
    public void setClassicCalculator(AvailabilityCalculator calulator) {
        m_classicCalculator = calulator;
    }

    /**
     * <p>setHtmlReportRenderer</p>
     *
     * @param reportRenderer a {@link org.opennms.report.availability.render.ReportRenderer} object.
     */
    public void setHtmlReportRenderer(ReportRenderer reportRenderer) {
        m_htmlReportRenderer = reportRenderer;
    }

    /**
     * <p>setPdfReportRenderer</p>
     *
     * @param reportRenderer a {@link org.opennms.report.availability.render.ReportRenderer} object.
     */
    public void setPdfReportRenderer(ReportRenderer reportRenderer) {
        m_pdfReportRenderer = reportRenderer;
    }

    /**
     * <p>setSvgReportRenderer</p>
     *
     * @param reportRenderer a {@link org.opennms.report.availability.render.ReportRenderer} object.
     */
    public void setSvgReportRenderer(ReportRenderer reportRenderer) {
        m_svgReportRenderer = reportRenderer;
    }

    /**
     * <p>setCategoryName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setCategoryName(String name) {
        m_categoryName = name;
    }

    /**
     * <p>setEmail</p>
     *
     * @param m_email a {@link java.lang.String} object.
     */
    public void setEmail(String m_email) {
        this.m_email = m_email;
    }

    /**
     * <p>setFormat</p>
     *
     * @param m_format a {@link java.lang.String} object.
     */
    public void setFormat(String m_format) {
        this.m_format = m_format;
    }

    /**
     * <p>setLogo</p>
     *
     * @param m_logo a {@link java.lang.String} object.
     */
    public void setLogo(String m_logo) {
        this.m_logo = m_logo;
    }

    /**
     * <p>setMonthFormat</p>
     *
     * @param format a {@link java.lang.String} object.
     */
    public void setMonthFormat(String format) {
        m_monthFormat = format;
    }

    /**
     * <p>setPeriodEndDate</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    public void setPeriodEndDate(Date date) {
        m_periodEndDate = date;
    }

}
