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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDao;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DefaultGlobalReportRepository.java</p>
 * 
 * @author <a href="mailto:markus@opennms.com">Markus Neumann</a>
 *
 * @version $Id: $ 
 */
public class DefaultGlobalReportRepository implements GlobalReportRepository { 
    
    /**
     * Logging
     */
    private final Logger logger = LoggerFactory.getLogger(DefaultGlobalReportRepository.class);

    
    private final RemoteRepositoryConfigDao m_remoteRepositoryConfigDao = new DefaultRemoteRepositoryConfigDao();
    /**
     * Concatenated repositoryId and reportId by "_"
     */
    private final String REPOSITORY_REPORT_SEP = "_";

    /**
     * List for repositories, a local disk and every active remote repository configured at m_remoteRepositoryConfigDao.
     */
    private final List<ReportRepository> m_repositoryList;

    /**
     * Default constructor creates one local and many remote repositories.
     */
    public DefaultGlobalReportRepository() {
        this.m_repositoryList = new ArrayList<ReportRepository>();

        /**
         * The local disk repository provides the canned OpenNMS community reports.
         */
        this.m_repositoryList.add(new LegacyLocalReportRepository());

        /**
         * A remote repository for each remote repository from RemoteRepositoryConfig.
         */
        for (RemoteRepositoryDefinition repositoryDefinition : m_remoteRepositoryConfigDao.getActiveRepositories()) {
            this.m_repositoryList.add(new DefaultRemoteRepository(repositoryDefinition, System.getProperty("org.opennms.jasperReportsVersion")));
        }
    }

    /**
     * Legacy method to get all reports from local and remote repository.
     * 
     * @return a report definition as {@link java.util.List<BasicReportDefinition>} object
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
     * Get reports from a specific repository identified by repository id.
     * 
     * @param repositoryId a String as repository identifier
     * @return a report definition as {@link java.util.List<BasicReportDefinition>} object
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
     * Get all online reports from local and remote repository.
     *
     * @return a report definition as {@link java.util.List<BasicReportDefinition>} object
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
     * Get online reports from a specific repository identified by repository id.
     *
     * @param repositoryId a String as repository identifier
     * @return a report definition as {@link java.util.List<BasicReportDefinition>} object
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports(String repositoryId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repository = this.getRepositoryById(repositoryId);
        if (repository != null ) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }

    /**
     * Get the report service identified by report id.
     *
     * @param reportId a String as report identifier
     * @return a report service as {@link java.lang.String} object
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
     * Get the display name from a specific report identified by report id.
     * 
     * @param reportId a String as report identifier
     * @return a display name as {@link java.lang.String} object
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
     * Get the report engine from a specific report identified by report id.
     * 
     * @param reportId a String as report identifier
     * @return engine as {@link java.lang.String} object
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
     * Get a specific report template identified by report id.
     * 
     * @param reportId a String as report identifier                
     * @return template as {@link java.io.InputStream} object 
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
     * Get a list with the local and the remote repository.
     * 
     * @return repositories as {@link java.util.List<ReportRepository>} object
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
}
