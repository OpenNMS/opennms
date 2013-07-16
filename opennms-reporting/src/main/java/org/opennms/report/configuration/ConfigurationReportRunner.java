/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.report.configuration;

import java.io.IOException;
import java.util.Date;



import org.opennms.report.ReportMailer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ConfigurationReportRunner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ConfigurationReportRunner implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationReportRunner.class);

        
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
    @Override
    public void run() {

        LOG.debug("run: getting configuration report on Date [{}]. Requested by User: {}on Date {}", theDate, user,  reportRequestDate.toString());

        ReportRenderer renderer;
        calculator.setReportRequestDate(reportRequestDate);
        calculator.setTheDate(theDate);
        calculator.setUser(user);
        
        if (reportFormat.compareTo("pdftype") == 0){
            LOG.debug("run: generating pdf is still not supported :( sending xml");
            
            renderer = m_nullReportRenderer;
        } else {
            LOG.debug("runRancidListReport generating html");
            renderer =  m_htmlReportRenderer;
        }
       
        try {
            calculator.calculate();
            calculator.writeXML();

            String outputFile = calculator.getOutputFileName();
            LOG.debug("Written Configuration Report as XML to {}", outputFile);
            renderer.setInputFileName(outputFile);
            LOG.debug("rendering XML {} as {}", outputFile, renderer.getOutputFileName());
            renderer.render();
            ReportMailer mailer = new ReportMailer(
                                                   reportEmail,
                                                   renderer.getBaseDir()
                                                           + renderer.getOutputFileName(), "OpenNMS Configuration Report");
            mailer.send();
        } catch (ConfigurationCalculationException ce) {
            LOG.error("Unable to calculate report data ", ce);
        } catch (ReportRenderException re) {
            LOG.error("Unable to render report ", re);
        } catch (IOException ioe) {
            LOG.error("Unable to render report ", ioe);
        }
    }

}
