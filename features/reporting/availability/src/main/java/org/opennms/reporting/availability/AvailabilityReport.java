//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Feb 04: Cleaned up logging and added a ConsoleLogger for the main()
// method. - dj@opennms.org
// 2006 May 30: added a way to choose the date to run the availability
// reports.
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
// Tab Size = 8
//

package org.opennms.reporting.availability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.reporting.availability.render.HTMLReportRenderer;
import org.opennms.reporting.availability.render.PDFReportRenderer;
import org.springframework.util.StringUtils;

/**
 * AvailabilityReport generates the Availability report in PDF format
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">Oculan </A>
 */
public class AvailabilityReport extends Object {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    /*
     * classic month format
     */

    private static final String MONTH_FORMAT_CLASSIC = "classic";

    /**
     * Castor object that holds all the information required for the
     * generating xml to be translated to the pdf.
     */
    private Report m_report = null;

    /**
     * String of Months
     */

    public static String[] months = new String[] { "January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };

    /**
     * Default constructor
     *
     * @param author a {@link java.lang.String} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     */
    public AvailabilityReport(String author, String startMonth,
            String startDate, String startYear) {
        String oldPrefix = ThreadCategory.getPrefix();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        if (log().isDebugEnabled()) {
            log().debug("Inside AvailabilityReport");
        }

        Calendar today = new GregorianCalendar();
        int day = Integer.parseInt(startDate);
        int year = Integer.parseInt(startYear);
        // int month = Integer.parseInt(startMonth);
        // int day = today.get(Calendar.DAY_OF_MONTH);
        // int year = today.get(Calendar.YEAR);
        // SimpleDateFormat smpMonth = new SimpleDateFormat("MMMMMMMMMMM");
        // String month = smpMonth.format(new
        // java.util.Date(today.getTime().getTime()));
        // int month = today.get(Calendar.MONTH) + 1;
        String month = months[Integer.parseInt(startMonth)];
        int hour = today.get(Calendar.HOUR);
        int minute = today.get(Calendar.MINUTE);
        int second = today.get(Calendar.SECOND);
        Created created = new Created();
        created.setDay(day);
        created.setHour(hour);
        created.setMin(minute);
        created.setMonth(month);
        created.setSec(second);
        created.setYear(year);
        created.setContent(new BigDecimal(today.getTime().getTime()));

        m_report = new Report();
        m_report.setCreated(created);
        m_report.setAuthor(author);

        if (log().isDebugEnabled()) {
            log().debug("Leaving AvailabilityReport");
        }
        ThreadCategory.setPrefix(oldPrefix);
    }

    /**
     * This when invoked generates the data into report castor classes.
     *
     * @param logourl
     *            location of the logo to be displayed on the report
     * @param categoryName
     *            of the logo to be displayed on the report
     * @param reportFormat
     *            Report Format ("SVG" / all)
     * @param monthFormat
     *            Format for month data ("classic"/"calendar")
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     */
    public void getReportData(String logourl, String categoryName,
            String reportFormat, String monthFormat, String startMonth,
            String startDate, String startYear) {

        if (log().isDebugEnabled()) {
            log().debug("inside getReportData");
            log().debug("Category name  " + categoryName);
            log().debug("Report format   " + reportFormat);
            log().debug("logo  " + logourl);
            log().debug("monthFormat " + monthFormat);
        }
        populateReport(logourl, categoryName, reportFormat, monthFormat,
                       startMonth, startDate, startYear);
        try {
            marshalReport();
        } catch (Exception e) {
            log().fatal("Exception: " + e, e);
        }
    }

    /**
     * This when invoked populates the castor classes.
     *
     * @param logourl
     *            location of the logo to be displayed on the report
     * @param categoryName
     *            of the logo to be displayed on the report
     * @param reportFormat
     *            Report Format ("SVG" / all)
     * @param monthFormat
     *            Format for month data ("classic"/"calendar")
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     */
    public void populateReport(String logourl, String categoryName,
            String reportFormat, String monthFormat, String startMonth,
            String startDate, String startYear) {
        m_report.setLogo(logourl);
        ViewInfo viewInfo = new ViewInfo();
        m_report.setViewInfo(viewInfo);
        org.opennms.reporting.availability.Categories categories = new org.opennms.reporting.availability.Categories();
        m_report.setCategories(categories);
        try {
            AvailabilityData reportSource = new AvailabilityData();
            
            reportSource.fillReport(categoryName, m_report, reportFormat,
                                 monthFormat, startMonth,
                                 startDate, startYear);
        } catch (Exception e) {
            log().fatal("Exception: " + e, e);
        }
    }

    /**
     * This when invoked marshals the report XML from the castor classes.
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public void marshalReport() throws ValidationException, MarshalException,
            IOException, Exception {

        File file = new File(ConfigFileConstants.getHome()
                + "/share/reports/AvailReport.xml");
        try {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            Marshaller marshaller = new Marshaller(fileWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(m_report);
            if (log().isDebugEnabled()) {
                log().debug(
                            "The xml marshalled from the castor classes is saved in "
                                    + ConfigFileConstants.getHome()
                                    + "/share/reports/AvailReport.xml");
            }
            fileWriter.close();
        } catch (Exception e) {
            log().fatal("Exception: " + e, e);
        }
    }

    /**
     * Generate PDF from castor classes.
     *
     * @param xsltFileName a {@link java.lang.String} object.
     * @param out a {@link java.io.OutputStream} object.
     * @param format a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void generatePDF(String xsltFileName, OutputStream out,
            String format) throws Exception {
        String oldPrefix = ThreadCategory.getPrefix();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        if (log().isDebugEnabled()) {
            log().debug("inside generatePDF");
        }
        File file = new File(ConfigFileConstants.getHome()
                + "/share/reports/AvailReport.xml");
        try {
            if (log().isDebugEnabled()) {
                log().debug(
                            "The xml marshalled from the castor classes is saved in "
                                    + ConfigFileConstants.getHome()
                                    + "/share/reports/AvailReport.xml");
            }
            Reader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            if (!format.equals("HTML")) {
                new PDFReportRenderer().render(fileReader, out, new InputStreamReader(new FileInputStream(xsltFileName), "UTF-8"));
            } else {
                new HTMLReportRenderer().render(fileReader, out, new InputStreamReader(new FileInputStream(xsltFileName), "UTF-8"));
            }
        } catch (Exception e) {
            log().fatal("Exception: " + e, e);
        }
        if (log().isInfoEnabled()) {
            log().info("leaving generatePDF");
        }
        ThreadCategory.setPrefix(oldPrefix);
    }

    /**
     * Main method
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String args[]) {
        // Spit warning level and higher messages out to the console
        ConsoleAppender consoleAppender = new ConsoleAppender(
                                                              new PatternLayout(
                                                                                "%m%n"),
                                                              ConsoleAppender.SYSTEM_ERR);
        consoleAppender.setThreshold(Level.WARN);
        Logger logger = Logger.getLogger(LOG4J_CATEGORY);
        logger.addAppender(consoleAppender);

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log().debug(
                    "main() called with args: "
                            + StringUtils.arrayToDelimitedString(args, ", "));

        System.setProperty("java.awt.headless", "true");

        String logourl = System.getProperty("image");
        String categoryName = System.getProperty("catName");
        if (categoryName == null || categoryName.equals("")) {
            categoryName = "all";
        }
        String format = System.getProperty("format");
        if (format == null || format.equals("")) {
            format = "SVG";
        }
        String monthFormat = System.getProperty("MonthFormat");
        if (monthFormat == null || format.equals("")) {
            monthFormat = MONTH_FORMAT_CLASSIC;
        }
        String startMonth = System.getProperty("startMonth");
        String startDate = System.getProperty("startDate");
        String startYear = System.getProperty("startYear");

        if (startMonth == null || startDate == null || startYear == null) {
            throw new NumberFormatException("missing date properties");
        }
        
        try {
            generateReport(logourl, categoryName, format, monthFormat,
                           startMonth, startDate, startYear);
        } catch (Exception e) {
            log().error("Caught exception while generating report: " + e, e);
            e.printStackTrace();
        }
    }

    /**
     * <p>generateReport</p>
     *
     * @param logourl a {@link java.lang.String} object.
     * @param categoryName a {@link java.lang.String} object.
     * @param format a {@link java.lang.String} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public static void generateReport(String logourl, String categoryName,
            String format, String monthFormat, String startMonth,
            String startDate, String startYear) throws Exception {

        // This report will be invoked by the mailer script.
        // Only SVG formatted reports are needed.
        // 
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        String catFileName = categoryName.replace(' ', '-');

        String pdfFileName;
        String xslFileName;
        if (format.equals("SVG")) {
            pdfFileName = ConfigFileConstants.getHome()
                    + "/share/reports/AVAIL-SVG-" + catFileName
                    + fmt.format(new java.util.Date()) + ".pdf";
            xslFileName = ConfigFileConstants.getFilePathString()
                    + ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_SVG_XSL);
        } else if (format.equals("PDF")) {
            pdfFileName = ConfigFileConstants.getHome()
                    + "/share/reports/AVAIL-PDF-" + catFileName
                    + fmt.format(new java.util.Date()) + ".pdf";
            xslFileName = ConfigFileConstants.getFilePathString()
                    + ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_PDF_XSL);
        } else if (format.equals("HTML")) {
            pdfFileName = ConfigFileConstants.getHome()
                    + "/share/reports/AVAIL-HTML-" + catFileName
                    + fmt.format(new java.util.Date()) + ".html";
            xslFileName = ConfigFileConstants.getFilePathString()
                    + ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_HTML_XSL);
        } else {
            log().fatal(
                        "Format '"
                                + format
                                + "' is unsupported.  Must be one of: SVG, PDF, or HTML.");
            return;
        }

        try {
            AvailabilityReport report = new AvailabilityReport("Unknown",
                                                               startMonth,
                                                               startDate,
                                                               startYear);
            report.getReportData(logourl, categoryName, format, monthFormat,
                                 startMonth, startDate, startYear);
            if (log().isInfoEnabled()) {
                log().info("Generated Report Data... ");
            }
            File file = new File(pdfFileName);
            FileOutputStream pdfFileWriter = new FileOutputStream(file);
            report.generatePDF(xslFileName, pdfFileWriter, format);
            if (log().isInfoEnabled()) {
                log().debug(
                            "xsl -> " + xslFileName + " pdfFileName -> "
                                    + pdfFileName + " format -> " + format);
                log().info("Generated Report ... and saved as " + pdfFileName);
            }
        } catch (Exception e) {
            log().fatal("Exception " + e, e);
        }
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(AvailabilityReport.class);
    }
}
