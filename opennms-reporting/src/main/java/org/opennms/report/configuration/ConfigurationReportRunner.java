package org.opennms.report.configuration;

import java.io.IOException;
import java.util.Date;


import org.apache.log4j.Logger;

import org.opennms.report.ReportMailer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;

/**
 * <p>ConfigurationReportRunner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ConfigurationReportRunner implements Runnable {
        
    String theDate;
    String reportFormat;
    String reportEmail;
    String user;
    Date reportRequestDate;

    ConfigurationReportCalculator calculator;

    ReportRenderer m_htmlReportRenderer;    
    ReportRenderer m_nullReportRenderer;

    /**
     * <p>getNullReportRenderer</p>
     *
     * @return a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public ReportRenderer getNullReportRenderer() {
        return m_nullReportRenderer;
    }

    /**
     * <p>setNullReportRenderer</p>
     *
     * @param nullReportRenderer a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public void setNullReportRenderer(ReportRenderer nullReportRenderer) {
        m_nullReportRenderer = nullReportRenderer;
    }

    /**
     * <p>getHtmlReportRenderer</p>
     *
     * @return a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public ReportRenderer getHtmlReportRenderer() {
        return m_htmlReportRenderer;
    }

    /**
     * <p>setHtmlReportRenderer</p>
     *
     * @param htmlReportRenderer a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public void setHtmlReportRenderer(ReportRenderer htmlReportRenderer) {
        m_htmlReportRenderer = htmlReportRenderer;
    }

    /**
     * <p>Getter for the field <code>theDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTheDate() {
        return theDate;
    }

    /**
     * <p>Setter for the field <code>theDate</code>.</p>
     *
     * @param theDate a {@link java.lang.String} object.
     */
    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    /**
     * <p>Getter for the field <code>reportFormat</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportFormat() {
        return reportFormat;
    }

    /**
     * <p>Setter for the field <code>reportFormat</code>.</p>
     *
     * @param reportFormat a {@link java.lang.String} object.
     */
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    /**
     * <p>Getter for the field <code>reportEmail</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportEmail() {
        return reportEmail;
    }

    /**
     * <p>Setter for the field <code>reportEmail</code>.</p>
     *
     * @param reportEmail a {@link java.lang.String} object.
     */
    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    /**
     * <p>Getter for the field <code>user</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return user;
    }

    /**
     * <p>Setter for the field <code>user</code>.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * <p>Getter for the field <code>reportRequestDate</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    /**
     * <p>Setter for the field <code>reportRequestDate</code>.</p>
     *
     * @param reportRequestDate a {@link java.util.Date} object.
     */
    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    /**
     * <p>getConfigurationReportCalculator</p>
     *
     * @return a {@link org.opennms.report.configuration.ConfigurationReportCalculator} object.
     */
    public ConfigurationReportCalculator getConfigurationReportCalculator() {
        return calculator;
    }

    /**
     * <p>setConfigurationReportCalculator</p>
     *
     * @param configurationReportCalculator a {@link org.opennms.report.configuration.ConfigurationReportCalculator} object.
     */
    public void setConfigurationReportCalculator(
            ConfigurationReportCalculator configurationReportCalculator) {
        calculator = configurationReportCalculator;
    }

    /**
     * <p>run</p>
     */
    public void run() {

        log().debug("run: getting configuration report on Date ["+ theDate +"]. Requested by User: " + user + "on Date " 
                    + reportRequestDate.toString()); 

        ReportRenderer renderer;
        calculator.setReportRequestDate(reportRequestDate);
        calculator.setTheDate(theDate);
        calculator.setUser(user);
        
        if (reportFormat.compareTo("pdftype") == 0){
            log().debug("run: generating pdf is still not supported :( sending xml");
            
            renderer = m_nullReportRenderer;
        } else {
            log().debug("runRancidListReport generating html");
            renderer =  m_htmlReportRenderer;
        }
       
        try {
            calculator.calculate();
            calculator.writeXML();

            String outputFile = calculator.getOutputFileName();
            log().debug("Written Configuration Report as XML to " + outputFile);
            renderer.setInputFileName(outputFile);
            log().debug("rendering XML " + outputFile + " as "
                    + renderer.getOutputFileName());
            renderer.render();
            ReportMailer mailer = new ReportMailer(
                                                   reportEmail,
                                                   renderer.getBaseDir()
                                                           + renderer.getOutputFileName(), "OpenNMS Configuration Report");
            mailer.send();
        } catch (ConfigurationCalculationException ce) {
            log().fatal("Unable to calculate report data ", ce);
        } catch (ReportRenderException re) {
            log().fatal("Unable to render report ", re);
        } catch (IOException ioe) {
            log().fatal("Unable to render report ", ioe);
        }
    }
        
    private static Logger log() {
        return Logger.getLogger("Rancid");
    }

}
