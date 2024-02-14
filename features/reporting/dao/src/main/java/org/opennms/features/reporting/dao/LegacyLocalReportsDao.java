/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
            logger.error("Resource '{}' does not seem to have an underlying File object.", m_configResource, e);
            return;
        }

        setLegacyLocalReportsDefinition(JaxbUtils.unmarshal(LegacyLocalReportsDefinition.class, file));
        Assert.notNull(m_legacyLocalReportsDefinition, "unmarshall config file returned a null value.");
        logger.debug("Unmarshalling config file '{}'", file.getAbsolutePath());
        logger.debug("Local report definitions assigned: '{}'", m_legacyLocalReportsDefinition);
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
