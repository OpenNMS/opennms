//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2007 Jun 24: Comment-out unused MONTH_FORMAT_CLASSIC field. - dj@opennms.org
// 2006 May 30: added a way to choose the date to run the availability reports.
//
// Orginal code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.StreamUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.utils.JavaMailer;
import org.opennms.netmgt.utils.JavaMailerException;
import org.opennms.report.availability.AvailabilityReport;

/**
 * <p>ReportMailer class.</p>
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class ReportMailer extends Object implements Runnable {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Report";
	
    // FIXME: This is unused
	//private static final String MONTH_FORMAT_CLASSIC = "classic";

    protected String scriptGenerateReport;

    protected String scriptMailReport;

    protected String finalEmailAddr;

    protected UserManager userFactory;

    protected String filename;

    protected String commandParms;

    protected boolean useScript = false;

    protected String logoUrl = null;

    protected String format = null;
	
	protected String monthFormat = null;

    protected String startMonth = null;

    protected String startDate = null;

    protected String startYear = null;


    protected String categoryName = null;

    Category log;

    /**
     * <p>Constructor for ReportMailer.</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public ReportMailer() throws ServletException {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
    }

    /**
     * <p>initialise</p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param userName a {@link java.lang.String} object.
     * @param generateReport a {@link java.lang.String} object.
     * @param mailReport a {@link java.lang.String} object.
     * @param parms a {@link java.lang.String} object.
     * @param fmt a {@link java.lang.String} object.
     * @param monthFmt a {@link java.lang.String} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     * @throws javax.servlet.ServletException if any.
     */
    public void initialise(String fileName, String userName, String generateReport, String mailReport, String parms, String fmt, String monthFmt, String startMonth, String startDate, String startYear) throws ServletException {

        filename = fileName;
        commandParms = parms;
        this.scriptGenerateReport = generateReport;
        this.scriptMailReport = mailReport;
        this.format = fmt;
        this.monthFormat = monthFmt;
        this.startMonth = startMonth;
        this.startDate = startDate;
        this.startYear = startYear;


        if (log.isDebugEnabled()) {
            log.debug("scriptGenerateReport " + scriptGenerateReport);
            log.debug("parms " + parms);
            log.debug("fmt " + fmt);
            log.debug("monthFmt " + monthFmt);
            log.debug("startMonth " + startMonth);
            log.debug("startDate " + startDate);
            log.debug("startYear " + startYear);

        }
        if (this.scriptGenerateReport == null) {
            throw new ServletException("Missing required init parameter: script.generateReport");
        }

        if (this.scriptMailReport == null) {
            throw new ServletException("Missing required init parameter: script.mailReport");
        }

        try {
            UserFactory.init();
            this.userFactory = UserFactory.getInstance();
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("could not initialize the UserFactory", e);
            throw new ServletException("could not initialize the UserFactory", e);
        }

        if (userName == null) {
            // shouldn't happen
            throw new IllegalStateException("OutageReportServlet can't work without authenticating the remote user.");
        }

        String emailAddr = null;

        try {
            emailAddr = this.getEmailAddress(userName);
            finalEmailAddr = emailAddr;

            if (emailAddr == null || emailAddr.trim().length() == 0) {
                return;
            }
        } catch (Exception e) {
            // if(log.isDebugEnabled())
            // log.debug("error looking up email address", e);
            throw new ServletException(e);
        }
    }

    /**
     * <p>getEmailAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEmailAddress() {
        return finalEmailAddr;
    }

    /**
     * <p>run</p>
     */
    public void run() {

        if (log.isInfoEnabled())
            log.info("thread to generate outage report started");

        try {
            generateFile(scriptGenerateReport);
            if (log.isInfoEnabled())
                log.info("outage report is generated.  filename is " + filename);
        } catch (InterruptedException e) {
            if (log.isDebugEnabled())
                log.debug("interrupted while generating report", e);
            return;
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("error generating report", e);
            return;
        }

        try {

            if (useScript) {
                log.debug("Sending "+filename+" via "+scriptMailReport);
                mailFileToUser(scriptMailReport, filename, finalEmailAddr);
            } else {
                log.debug("Sending "+filename+" via JavaMail");
                mailFileToUser(filename, finalEmailAddr);
            }
            if (log.isInfoEnabled())
                log.info("outage report has been mailed to user at " + finalEmailAddr);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("error mailing report", e);
            return;
        }
    }

    /**
     * returns null if no email address is configured for the user
     *
     * @param username a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected String getEmailAddress(String username) throws IOException, MarshalException, ValidationException {
        if (username == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        return this.userFactory.getEmail(username);
    }

    /**
     * returns the fully-qualified filename of the generated PDF report
     *
     * @param shellScript a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.InterruptedException if any.
     */
    protected void generateFile(String shellScript) throws IOException, InterruptedException {
        if (shellScript == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String[] cmdArgs;
        cmdArgs = new String[3];
        int i = 0;
        cmdArgs[i++] = shellScript;
        if (commandParms != null) {
            if (!commandParms.equals(""))
                cmdArgs[i++] = commandParms;
        } else
            cmdArgs[i++] = "";
        if (null != format) {
            if (!format.equals(""))
                cmdArgs[i] = format;
        } else
            cmdArgs[i] = "";
        if (log.isDebugEnabled()) {
            log.debug("Command Line Args " + cmdArgs[0]);
            log.debug("Command Line Args " + cmdArgs[1]);
            log.debug("Command Line Args " + cmdArgs[2]);
        }

        // java.lang.Process process = Runtime.getRuntime().exec( cmdArgs );
		// TODO: Add code to generate "calendar" as well as "classic" reports
        try {
            AvailabilityReport.generateReport(getLogoUrl(), getCategoryName(), getFormat(), getMonthFormat(), getStartMonth(), getStartDate(), getStartYear());
        } catch (Exception e) {
            log.error("Caught exception generating report: ", e);
        }

        // get the stderr to see if the command failed
        // BufferedReader err = new BufferedReader(new
        // InputStreamReader(process.getErrorStream()));

        /*
         * if( err.ready() ) { //get the error message StringWriter tempErr =
         * new StringWriter(); Util.streamToStream(err, tempErr); String
         * errorMessage = tempErr.toString();
         * 
         * //log the error message if(log.isDebugEnabled()) log.debug("Read from
         * stderr: " + errorMessage);
         * 
         * throw new IOException("Could not generate outage report" ); }
         * 
         * //wait until the file is completely generated process.waitFor();
         */

    }

    /**
     * <p>mailFileToUser</p>
     *
     * @param mailScript a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param emailAddr a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected void mailFileToUser(String mailScript, String filename, String emailAddr) throws IOException {
        if (mailScript == null || filename == null || emailAddr == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        String[] cmdArgs = { mailScript, filename, emailAddr };
        java.lang.Process process = Runtime.getRuntime().exec(cmdArgs);

        // get the stderr to see if the command failed
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        if (err.ready()) {
            // get the error message
            StringWriter tempErr = new StringWriter();
            StreamUtils.streamToStream(err, tempErr);
            String errorMessage = tempErr.toString();

            // log the error message
            if (log.isDebugEnabled())
                log.debug("Read from stderr: " + errorMessage);

            throw new IOException("Could not mail outage report");
        }
    }

    /**
     * <p>mailFileToUser</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param emailAddr a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected void mailFileToUser(String filename, String emailAddr) throws IOException {
        if (filename == null || emailAddr == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(emailAddr);
            jm.setSubject("OpenNMS Availability Report");
            jm.setFileName(filename);
            jm.setMessageText("Availability Report Mailed from JavaMailer class.");
            jm.mailSend();
        } catch (JavaMailerException e) {
            log.error("Caught JavaMailer exception sending file: " + filename, e);
            throw new IOException("Error sending file: " + filename);
        }
    }

    /**
     * <p>isUseScript</p>
     *
     * @return Returns the useScript value.
     */
    public boolean isUseScript() {
        return useScript;
    }

    /**
     * <p>Setter for the field <code>useScript</code>.</p>
     *
     * @param useScript
     *            Set this to use the script specified.
     */
    public void setUseScript(boolean useScript) {
        if (useScript)
            log.debug("Overiding script with JavaMailer.");
        this.useScript = useScript;
    }

    /**
     * <p>Getter for the field <code>logoUrl</code>.</p>
     *
     * @return Returns the logo.
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * <p>Setter for the field <code>logoUrl</code>.</p>
     *
     * @param logo
     *            The logo to set.
     */
    public void setLogoUrl(String logo) {
        this.logoUrl = logo;
    }

    /**
     * <p>Getter for the field <code>categoryName</code>.</p>
     *
     * @return Returns the categoryName.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * <p>Setter for the field <code>categoryName</code>.</p>
     *
     * @param categoryName
     *            The categoryName to set.
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * <p>Getter for the field <code>monthFormat</code>.</p>
     *
     * @return Returns the monthFormat.
     */
    public String getMonthFormat() {
        return monthFormat;
    }
	
    /**
     * <p>Setter for the field <code>monthFormat</code>.</p>
     *
     * @param monthFormat a {@link java.lang.String} object.
     */
    public void setMonthFormat(String monthFormat) {
        this.monthFormat = monthFormat;
    }
    /**
     * <p>Getter for the field <code>format</code>.</p>
     *
     * @return Returns the format.
     */
    public String getFormat() {
        return format;
    }
	
    /**
     * <p>Setter for the field <code>format</code>.</p>
     *
     * @param format
     *            The format to set.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * <p>Getter for the field <code>startMonth</code>.</p>
     *
     * @return Returns the startMonth.
     */
    public String getStartMonth() {
        return startMonth;
    }

    /**
     * <p>Setter for the field <code>startMonth</code>.</p>
     *
     * @param startMonth a {@link java.lang.String} object.
     */
    public void setStartMonth(String startMonth) {
        this.startMonth = startMonth;
    }

    /**
     * <p>Getter for the field <code>startDate</code>.</p>
     *
     * @return Returns the startDate.
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * <p>Setter for the field <code>startDate</code>.</p>
     *
     * @param startDate a {@link java.lang.String} object.
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * <p>Getter for the field <code>startYear</code>.</p>
     *
     * @return Returns the startYear.
     */
    public String getStartYear() {
        return startYear;
    }

    /**
     * <p>Setter for the field <code>startYear</code>.</p>
     *
     * @param startYear a {@link java.lang.String} object.
     */
    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }
 
}
