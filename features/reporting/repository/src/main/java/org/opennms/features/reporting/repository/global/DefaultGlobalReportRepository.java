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

package org.opennms.features.reporting.repository.global;

import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>DefaultGlobalReportRepository class.</p>
 * <p/>
 * Class realize the global report repository. It provides a local repository for community reports and reads one or
 * more configurations for remote repositories.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultGlobalReportRepository implements GlobalReportRepository {

    /**
     * Logging
     */
    private final Logger logger = LoggerFactory.getLogger(DefaultGlobalReportRepository.class);

    /**
     * Configuration DAO for remote-reports.xml
     */
    private RemoteRepositoryConfigDao m_remoteRepositoryConfigDao;

    /**
     * Concatenated repositoryId and reportId by "_"
     */
    private final String REPOSITORY_REPORT_SEP = "_";

    /**
     * List for repositories, a local disk and every active remote repository configured at m_remoteRepositoryConfigDao.
     */
    private final List<ReportRepository> m_repositoryList;

    /**
     * JasperReports version number
     */
    private String JASPER_REPORTS_VERSION;

    /**
     * DAO for all configured local OpenNMS community reports
     */
    private LocalReportsDao m_localReportsDao;

    /**
     * DAO for all local jasper specific reports
     */
    private LocalJasperReportsDao m_localJasperReportsDao;

    /**
     * Default constructor creates one local and many remote repositories.
     */
    public DefaultGlobalReportRepository() {
        Assert.notNull(m_remoteRepositoryConfigDao, "remote repository config dao property configResource must be set to a non-null value");
        logger.debug("Config resource is set to '{}'", m_remoteRepositoryConfigDao.toString());

        Assert.notNull(m_localReportsDao, "local reports config dao property configResource must be set to a non-null value");
        logger.debug("Config resource is set to '{}'", m_localReportsDao.toString());

        Assert.notNull(m_localJasperReportsDao, "local jasper reports config dao property configResource must be set to a non-null value");
        logger.debug("Config resource is set to '{}'", m_localJasperReportsDao.toString());

        // The remote repository needs the JasperReport version for templates
        JASPER_REPORTS_VERSION = System.getProperty("org.opennms.jasperReportsVersion");
        Assert.notNull(JASPER_REPORTS_VERSION, "property jasper reports version must be set to a non-null value");
        logger.debug("JasperReports version is set to '{}'", JASPER_REPORTS_VERSION);

        this.m_repositoryList = new ArrayList<ReportRepository>();

        /**
         * The local disk repository provides the canned OpenNMS community reports.
         */
        this.m_repositoryList.add(new LegacyLocalReportRepository(m_localReportsDao, m_localJasperReportsDao));

        /**
         * Create a list with all remote repositories from remote repository for each remote repository from RemoteRepositoryConfig.
         */
        for (RemoteRepositoryDefinition repositoryDefinition : m_remoteRepositoryConfigDao.getActiveRepositories()) {
            this.m_repositoryList.add(new DefaultRemoteRepository(repositoryDefinition, JASPER_REPORTS_VERSION));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getAllReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : m_repositoryList) {
            results.addAll(repository.getReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getReports(String repositoryId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repository = this.getRepositoryById(repositoryId);
        if (repository != null) {
            results.addAll(repository.getReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getAllOnlineReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : m_repositoryList) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports(String repositoryId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repository = this.getRepositoryById(repositoryId);
        if (repository != null) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReportService(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getReportService(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getDisplayName(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEngine(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getEngine(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getTemplateStream(String reportId) {
        InputStream templateStream = null;
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            templateStream = repository.getTemplateStream(reportId);
        }
        return templateStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportRepository> getRepositoryList() {
        return m_repositoryList;
    }

    /**
     * Add a report repository.
     *
     * @param repository a ReportRepository
     */
    @Override
    public void addReportRepository(ReportRepository repository) {
        m_repositoryList.add(repository);
    }

    /**
     * Get a specific repository identified by repository id.
     *
     * @param repositoryId a String as repository identifier
     * @return report repository as {@link org.opennms.features.reporting.repository.ReportRepository} object
     */
    @Override
    public ReportRepository getRepositoryById(String repositoryId) {
        for (ReportRepository repository : m_repositoryList) {
            if (repositoryId.equals(repository.getRepositoryId())) {
                // leave if we have a repository
                return repository;
            }
        }
        logger.debug("Not repository with id '{}' was found, return null", repositoryId);
        // we haven't a repository with repositoryId
        return null;
    }

    /**
     * Get a specific repository identified by a report id.
     *
     * @param reportId a String as report identifier
     * @return report repository as {@link org.opennms.features.reporting.repository.ReportRepository} object
     */
    protected ReportRepository getRepositoryForReport(String reportId) {
        String repositoryId = reportId.substring(0, reportId.indexOf(REPOSITORY_REPORT_SEP));
        return this.getRepositoryById(repositoryId);
    }

    /**
     * <p>setLocalReportsDao</p>
     * 
     * Set local reports DAO against local-reports.xml 
     * 
     * @param localReportsDao a {@link org.opennms.features.reporting.dao.LocalReportsDao} object
     */
    public void setLocalReportsDao(LocalReportsDao localReportsDao) {
        this.m_localReportsDao = localReportsDao;
    }

    /**
     * <p>getLocalReportsDao</p>
     * 
     * Get local reports DAO from local-reports.xml 
     * 
     * @return a {@link org.opennms.features.reporting.dao.LocalReportsDao} object
     */
    public LocalReportsDao getLocalReportsDao() {
        return this.m_localReportsDao;
    }

    /**
     * <p>setLocalJasperReportsDao</p>
     *
     * Set local jasper reports DAO against local-jasper-reports.xml
     *
     * @param localJasperReportsDao a {@link org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao} object
     */
    public void setLocalJasperReportsDao(LocalJasperReportsDao localJasperReportsDao) {
        this.m_localJasperReportsDao = localJasperReportsDao;
    }

    /**
     * <p>getLocalJasperReportsDao</p>
     *
     * Get local jasper reports DAO from local-jasper-reports.xml
     * 
     * @return a {@link org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao} object
     */
    public LocalJasperReportsDao getLocalJasperReportsDao() {
        return this.m_localJasperReportsDao;
    }

    public void setRemoteRepositoryConfigDao(RemoteRepositoryConfigDao remoteRepositoryConfigDao) {
        m_remoteRepositoryConfigDao = remoteRepositoryConfigDao;
    }

    public RemoteRepositoryConfigDao getRemoteRepositoryConfigDao() {
        return m_remoteRepositoryConfigDao;
    }
}
