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
package org.opennms.features.reporting.dao.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.reporting.model.jasperreport.JasperReportDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>LegacyLocalJasperReportsDao class.</p>
 * <p/>
 * Class realize the data access and preserve compatibility to jasper-reports.xml.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
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
    private Resource m_jrTemplateResource;

    public LegacyLocalJasperReportsDao(Resource configResource, Resource jrTemplateResource) {
        m_configResource = configResource;
        Assert.notNull(m_configResource, "property configResource must be set to a non-null value");
        
        m_jrTemplateResource = jrTemplateResource;
        Assert.notNull(m_jrTemplateResource, "property configResource must be sot to a non-null value");
        
        try {
            loadConfiguration();
        } catch (Exception e) {
            logger.error("Error could not load jasper-reports.xml. Error message: '{}'", e.getMessage());
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
        } catch (IOException e) {
            logger.error("Resource '{}' does not seem to have an underlying File object.", m_configResource, e);
            return;
        }

        setLocalJasperReports(JaxbUtils.unmarshal(LocalJasperReports.class, file));
        Assert.notNull(m_LocalJasperReports, "unmarshall config file returned a null value.");
        logger.debug("Unmarshalling config file '{}'", file.getAbsolutePath());
        logger.debug("Local report definitions assigned: '{}'", m_LocalJasperReports);
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
    public void setJrTemplateResource(Resource jrTemplateResource) {
        m_jrTemplateResource = jrTemplateResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getJrTemplateResource() {
        return m_jrTemplateResource;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public InputStream getTemplateStream(String id) {
        try {
            String reportTemplateFolder = m_jrTemplateResource.getFile().getPath();
            for (JasperReportDefinition report : m_LocalJasperReports.getReportList()) {
                if (id.equals(report.getId())) {
                    try {
                        return new FileInputStream(new File(reportTemplateFolder + "/" + report.getTemplate()));
                    } catch (FileNotFoundException e) {
                        logger.error("Template file '{}' at folder '{}' not found.", report.getTemplate(), reportTemplateFolder, e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * <p>getLocalJasperReports</p>
     * 
     * Get local jasper reports
     * 
     * @return a {@link org.opennms.features.reporting.model.jasperreport.LocalJasperReports} object
     */
    public LocalJasperReports getLocalJasperReports() {
        return m_LocalJasperReports;
    }

    /**
     * <p>setLocalJasperReports</p>
     * 
     * Set local jasper reports
     * 
     * @param localJasperReports a {@link org.opennms.features.reporting.model.jasperreport.LocalJasperReports} object
     */
    public void setLocalJasperReports(LocalJasperReports localJasperReports) {
        m_LocalJasperReports = localJasperReports;
    }
}
