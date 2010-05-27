package org.opennms.report.configuration;

import java.io.IOException;
import java.util.Date;


import org.apache.log4j.Logger;

import org.opennms.report.ReportMailer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;

public class ConfigurationReportRunner implements Runnable {
        
    String theDate;
    String reportFormat;
    String reportEmail;
    String user;
    Date reportRequestDate;

    ConfigurationReportCalculator calculator;

    ReportRenderer m_htmlReportRenderer;    
    ReportRenderer m_nullReportRenderer;

    public ReportRenderer getNullReportRenderer() {
        return m_nullReportRenderer;
    }

    public void setNullReportRenderer(ReportRenderer nullReportRenderer) {
        m_nullReportRenderer = nullReportRenderer;
    }

    public ReportRenderer getHtmlReportRenderer() {
        return m_htmlReportRenderer;
    }

    public void setHtmlReportRenderer(ReportRenderer htmlReportRenderer) {
        m_htmlReportRenderer = htmlReportRenderer;
    }

    public String getTheDate() {
        return theDate;
    }

    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getReportEmail() {
        return reportEmail;
    }

    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    public ConfigurationReportCalculator getConfigurationReportCalculator() {
        return calculator;
    }

    public void setConfigurationReportCalculator(
            ConfigurationReportCalculator configurationReportCalculator) {
        calculator = configurationReportCalculator;
    }

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
