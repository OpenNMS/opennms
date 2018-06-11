/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>LegacyLocalReportsDao class.</p>
 * <p/>
 * Class realize the data access and preserve compatibility to database-reports.xml.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.10.1
 */
public class LegacyLocalReportsDao implements LocalReportsDao {

    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(LegacyLocalReportsDao.class);

    /**
     * List of generic report definitions
     */
    private LegacyLocalReportsDefinition m_legacyLocalReportsDefinition;

    /**
     * Config resource for database reports configuration file
     */
    private Resource m_configResource;

    public LegacyLocalReportsDao(Resource configResource) {
        m_configResource = configResource;
        Assert.notNull(m_configResource, "property configResource must be set to a non-null value");
        logger.debug("Config resource is set to " + m_configResource.toString());
        
        try {
            loadConfiguration();
        } catch (Exception e) {
            logger.error("Error could not load database-reports.xml. Error message: '{}'", e.getMessage());
        }
        logger.debug("Configuration '{}' successfully loaded and unmarshalled.", m_configResource.getFilename());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConfiguration() throws Exception {
        File file = null;
        try {
            file = m_configResource.getFile();
            Assert.notNull(file, "config file must be sot to a non-null value");
        } catch (IOException e) {
            logger.error("Resource '{}' does not seem to have an underlying File object.", m_configResource);
        }

        setLegacyLocalReportsDefinition(JaxbUtils.unmarshal(LegacyLocalReportsDefinition.class, file));
        Assert.notNull(m_legacyLocalReportsDefinition, "unmarshall config file returned a null value.");
        logger.debug("Unmarshalling config file '{}'", file.getAbsolutePath());
        logger.debug("Local report definitions assigned: '{}'", m_legacyLocalReportsDefinition.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getReports() {
        ArrayList<BasicReportDefinition> resultList = new ArrayList<>();
        for (BasicReportDefinition report : m_legacyLocalReportsDefinition.getReportList()) {
            resultList.add(report);
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> onlineReports = new ArrayList<>();
        for (BasicReportDefinition report : m_legacyLocalReportsDefinition.getReportList()) {
            if (report.getOnline()) {
                onlineReports.add(report);
            }
        }
        return onlineReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReportService(String id) {
        for (BasicReportDefinition report : m_legacyLocalReportsDefinition.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getReportService();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(String id) {
        for (BasicReportDefinition report : m_legacyLocalReportsDefinition.getReportList()) {
            if (id.equals(report.getId())) {
                return report.getDisplayName();
            }
        }
        return null;
    }

    /**
     * <p>setLegacyLocalReportsDefinition</p>
     * <p/>
     * Set list with legacy report definition
     *
     * @param legacyLocalReportsDefinition {@link org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition} object
     */
    public void setLegacyLocalReportsDefinition(LegacyLocalReportsDefinition legacyLocalReportsDefinition) {
        this.m_legacyLocalReportsDefinition = legacyLocalReportsDefinition;
    }

    /**
     * <p>getLegacyLocalReportsDefinition</p>
     * <p/>
     * Get list with legacy report definition
     *
     * @return a {@link org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition} object
     */
    public LegacyLocalReportsDefinition getLegacyLocalReportsDefinition() {
        return m_legacyLocalReportsDefinition;
    }
}
