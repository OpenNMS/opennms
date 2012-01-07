/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.dao.jasper;

import org.opennms.features.reporting.model.jasperreport.JasperReportDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import javax.xml.bind.JAXB;
import java.io.*;

/**
 * <p>LegacyLocalJasperReportsDao class.</p>
 *
 * Class realize the data access and preserve compatibility to local-jasper-reports.xml.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
@ContextConfiguration(locations = {"classpath:reportingDao-context.xml"})
public class LegacyLocalJasperReportsDao implements LocalJasperReportsDao {
    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(LegacyLocalJasperReportsDao.class);

    /**
     * List of generic report definitions
     */

    private LocalJasperReports m_LocalJasperReports;

    /**
     * Config resource for database reports configuration file
     */
    private Resource m_configResource;

    /**
     * Config resource for jasper report templates
     */
    private Resource m_jasperReportResources;

    /**
     * <p>afterPropertiesSet</p>
     *
     * Sanity check for configuration file and load local-jasper-reports.xml
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_configResource != null, "property configResource must be set to a non-null value");
        loadLegacyLocalJasperReports();
    }

    private final String LOCAL_JASPER_REPORTS_TEMPLATE_FOLDER = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "report-templates"
            + File.separator;

    /**
     * <p>loadLegacyLocalJasperReports</p>
     *
     * File handling for database-reports.xml and unmarshal in LegacyLocalReportsDefinition class
     *
     * @throws Exception
     */
    private void loadLegacyLocalJasperReports() throws Exception {
        InputStream stream = null;
        long lastModified;

        File file = null;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            logger.error("Resource '{}' does not seem to have an underlying File object.", getConfigResource());
        }

        if (file != null) {
            lastModified = file.lastModified();
            stream = new FileInputStream(file);
        } else {
            lastModified = System.currentTimeMillis();
            stream = getConfigResource().getInputStream();
        }
        setLocalJasperReports(JAXB.unmarshal(file, LocalJasperReports.class));
    }

    /**
     * <p>getTemplateLocation</p>
     *
     * Get jasper report template location
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    @Override
    public String getTemplateLocation(String id) {
        for (JasperReportDefinition report : m_LocalJasperReports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getTemplate();
            }
        }
        return null;
    }

    /**
     * <p>getEngine</p>
     * 
     * Get jasper report database engine
     * 
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    @Override
    public String getEngine(String id) {
        for (JasperReportDefinition report : m_LocalJasperReports.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getEngine();
            }
        }
        return null;
    }

    /**
     * <p>getTemplateStream</p>
     * 
     * Get jasper report template as input stream
     * 
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    @Override
    public InputStream getTemplateStream(String id) {
        InputStream reportTemplateStream = null;

        try {
            String reportTemplateFolder = getJasperReportResources().getFile().getPath();

            for (JasperReportDefinition report : m_LocalJasperReports.getReportList()) {
                if (id.equals(report.getId())) {
                    try {
                        reportTemplateStream = new FileInputStream(
                                new File(
                                        reportTemplateFolder + "/" + report.getTemplate()));
                    } catch (FileNotFoundException e) {
                        logger.error("Template file '{}' at folder '{}' not found.", report.getTemplate(), reportTemplateFolder);

                        //TODO indigo: Add e.message to error message
                        e.printStackTrace();
                    } catch (IOException e) {
                        logger.error("Template file '{}' at folder '{}' not available.", report.getTemplate(), reportTemplateFolder);

                        //TODO indigo: Add e.message to error message
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return reportTemplateStream;
    }

    /**
     * <p>getLocalJasperReports</p>
     * 
     * Get list with legacy report definition
     *
     * @return a {@link org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition} object
     */
    public LocalJasperReports getLocalJasperReports() {
        return m_LocalJasperReports;
    }

    /**
     * <p>setLocalJasperReports</p>
     * 
     * Set list with legacy report definition
     *
     * @param localJasperReports {@link org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition} object
     */
    public void setLocalJasperReports(LocalJasperReports localJasperReports) {
        this.m_LocalJasperReports = localJasperReports;
    }

    /**
     * <p>setConfigResource</p>
     * 
     * Set resource for local-jasper-reports.xml
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object
     */
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    /**
     * <p>getConfigResource</p>
     * 
     * Get resource for local-jasper-reports.xml
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * <p>setJasperReportResources</p>
     * 
     * Set resource for jasper report template folder
     * 
     * @param jasperReportResources a {@link org.springframework.core.io.Resource} object
     */
    public void setJasperReportResources(Resource jasperReportResources) {
        m_jasperReportResources = jasperReportResources;
    }

    /**
     * <p>setConfigResource</p>
     * 
     * Get resource for jasper report template folder
     * 
     * @return a {@link org.springframework.core.io.Resource} object
     */
    public Resource getJasperReportResources() {
        return m_jasperReportResources;
    }
}
