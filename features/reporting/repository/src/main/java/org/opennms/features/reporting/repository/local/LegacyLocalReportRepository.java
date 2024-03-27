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
package org.opennms.features.reporting.repository.local;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.beanutils.BeanUtils;
import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>LegacyLocalReportRepository class.</p>
 * <p/>
 * Class realize the local repository for OpenNMS community reports.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.10.1
 */
public class LegacyLocalReportRepository implements ReportRepository {

    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(LegacyLocalReportRepository.class);

    /**
     * Data access to database-reports.xml
     */
    private LocalReportsDao m_localReportsDao;

    /**
     * Data access to jasper-reports.xml
     */
    private LocalJasperReportsDao m_localJasperReportsDao;

    /**
     * Repository tag to identify reports
     */
    private static final String REPOSITORY_ID = "local";

    /**
     * Name for the local community repository
     */
    private static final String REPOSITORY_NAME = "Local Report Repository";

    /**
     * Description for the local community repository
     */
    private static final String REPOSITORY_DESCRIPTION = "Providing OpenNMS community reports from local disk.";

    /**
     * URL to subscribe a repository
     */
    private static final String MANAGEMENT_URL = "blank";

    /**
     * Default constructor creates one local repositories for OpenNMS community reports.
     *
     * @param localReportsDao       a {@link org.opennms.features.reporting.dao.LegacyLocalReportsDao} object
     * @param localJasperReportsDao a {@link org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao} object
     */
    public LegacyLocalReportRepository(LocalReportsDao localReportsDao, LocalJasperReportsDao localJasperReportsDao) {
        m_localReportsDao = localReportsDao;
        Assert.notNull(m_localReportsDao, "property configResource must be set to a non-null value");
        logger.debug("Config resource is set to '{}'", m_localReportsDao.toString());

        m_localJasperReportsDao = localJasperReportsDao;
        Assert.notNull(m_localJasperReportsDao, "property configResource must be set to a non-null value");
        logger.debug("Config resource is set to '{}'", m_localJasperReportsDao.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> resultList = new ArrayList<>();
        for (BasicReportDefinition report : m_localReportsDao.getReports()) {
            BasicReportDefinition resultReport = new LegacyLocalReportDefinition();
            try {
                BeanUtils.copyProperties(resultReport, report);
                resultReport.setId(REPOSITORY_ID + "_" + report.getId());
                // Community reports are allowed by default, no permission restriction
                resultReport.setAllowAccess(true);
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                logger.error("InvocationTargetException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
                e.printStackTrace();
            }
            resultList.add(resultReport);
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultList = new ArrayList<>();
        for (BasicReportDefinition report : m_localReportsDao.getOnlineReports()) {
            BasicReportDefinition resultReport = new LegacyLocalReportDefinition();
            try {
                BeanUtils.copyProperties(resultReport, report);
                resultReport.setId(REPOSITORY_ID + "_" + report.getId());
                // Community reports are allowed by default, no permission restriction
                resultReport.setAllowAccess(true);
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                logger.error("InvocationTargetException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
                e.printStackTrace();
            }
            resultList.add(resultReport);
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReportService(String id) {
        id = id.substring(id.indexOf('_') + 1);
        return m_localReportsDao.getReportService(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(String id) {
        id = id.substring(id.indexOf('_') + 1);
        return m_localReportsDao.getDisplayName(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEngine(String id) {
        id = id.substring(id.indexOf('_') + 1);
        return m_localJasperReportsDao.getEngine(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getTemplateStream(String id) {
        id = id.substring(id.indexOf('_') + 1);
        InputStream result = null;
        try {
            result = m_localJasperReportsDao.getTemplateStream(id);
        } catch (FileNotFoundException e) {
            //TODO indigo: Catch this exception and logging
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryId() {
        return REPOSITORY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryName() {
        return REPOSITORY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryDescription() {
        return REPOSITORY_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getManagementUrl() {
        return MANAGEMENT_URL;
    }

    /**
     * <p>setLocalReportsDao</p>
     * <p/>
     * Set local reports DAO to access database-reports.xml
     *
     * @param localReportsDao a {@link org.opennms.features.reporting.dao.LocalReportsDao} object
     */
    public void setLocalReportsDao(LocalReportsDao localReportsDao) {
        m_localReportsDao = localReportsDao;
    }

    /**
     * <p>getLocalReportsDao</p>
     * <p/>
     * Get local reports DAO to access database-reports.xml
     *
     * @return a {@link org.opennms.features.reporting.dao.LocalReportsDao} object
     */
    public LocalReportsDao getLocalReportsDao() {
        return m_localReportsDao;
    }

    /**
     * <p>setLocalJasperReportsDao</p>
     * <p/>
     * Set local jasper reports dao to access jasper-reports.xml
     *
     * @param localJasperReportsDao a {@link org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao} object
     */
    public void setLocalJasperReportsDao(LocalJasperReportsDao localJasperReportsDao) {
        m_localJasperReportsDao = localJasperReportsDao;
    }

    /**
     * <p>getLocalJasperReportsDao</p>
     * <p/>
     * Get local jasper reports dao to access jasper-reports.xml
     *
     * @return a {@link org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao} object
     */
    public LocalJasperReportsDao getLocalJasperReportsDao() {
        return m_localJasperReportsDao;
    }

    @Override
    public void loadConfiguration() {
        try {
            m_localReportsDao.loadConfiguration();
            m_localJasperReportsDao.loadConfiguration();

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(LegacyLocalReportRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
