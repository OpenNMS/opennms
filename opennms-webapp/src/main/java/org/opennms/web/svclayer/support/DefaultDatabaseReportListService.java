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

package org.opennms.web.svclayer.support;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.web.svclayer.DatabaseReportListService;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * DefaultDatabaseReportListService class.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultDatabaseReportListService implements
        DatabaseReportListService {

    private GlobalReportRepository m_globalReportRepository;

    /**
     * <p>
     * getAll Reports from all Repositories
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    @Deprecated
    @Override
    public List<DatabaseReportDescription> getAll() {

        List<DatabaseReportDescription> allReports = new ArrayList<DatabaseReportDescription>();

        for (ReportRepository globalRepository : m_globalReportRepository.getRepositoryList()) {
            for (BasicReportDefinition report : globalRepository.getReports()) {
                DatabaseReportDescription summary = new DatabaseReportDescription();
                summary.setRepositoryId(globalRepository.getRepositoryId());
                summary.setId(report.getId());
                summary.setDisplayName(report.getDisplayName());
                summary.setDescription(report.getDescription());
                summary.setIsOnline(report.getOnline());
                summary.setAllowAccess(report.getAllowAccess());
                allReports.add(summary);
            }
        }
        return allReports;
    }

    @Override
    public List<ReportRepositoryDescription> getActiveRepositories() {
        List<ReportRepositoryDescription> result = new ArrayList<ReportRepositoryDescription>();
        List<ReportRepository> reportRepositoryList = new ArrayList<ReportRepository>();

        reportRepositoryList = m_globalReportRepository.getRepositoryList();

        for (ReportRepository repository : reportRepositoryList) {
            ReportRepositoryDescription reportRepositoryDescription = new ReportRepositoryDescription();
            reportRepositoryDescription.setId(repository.getRepositoryId());
            reportRepositoryDescription.setDescription(repository.getRepositoryDescription());
            reportRepositoryDescription.setDisplayName(repository.getRepositoryName());
            reportRepositoryDescription.setManagementUrl(repository.getManagementUrl());
            result.add(reportRepositoryDescription);
        }

        return result;
    }

    @Override
    public List<DatabaseReportDescription> getOnlineReportsByRepositoryId(String repositoryId) {
        List<DatabaseReportDescription> onlineReportList = new ArrayList<DatabaseReportDescription>();

        for (BasicReportDefinition reportDefinition : m_globalReportRepository.getRepositoryById(repositoryId).getOnlineReports()) {
                DatabaseReportDescription summary = new DatabaseReportDescription();
                summary.setRepositoryId(reportDefinition.getRepositoryId());
                summary.setId(reportDefinition.getId());
                summary.setDisplayName(reportDefinition.getDisplayName());
                summary.setDescription(reportDefinition.getDescription());
                summary.setIsOnline(reportDefinition.getOnline());
                summary.setAllowAccess(reportDefinition.getAllowAccess());
                onlineReportList.add(summary);
        }
        return onlineReportList;
    }

    @Override
    public List<DatabaseReportDescription> getReportsByRepositoryId(String repositoryId) {
        List<DatabaseReportDescription> reportList = new ArrayList<DatabaseReportDescription>();

        for (BasicReportDefinition reportDefinition : m_globalReportRepository.getRepositoryById(repositoryId).getReports()) {
            DatabaseReportDescription summary = new DatabaseReportDescription();
            summary.setRepositoryId(reportDefinition.getRepositoryId());
            summary.setId(reportDefinition.getId());
            summary.setDisplayName(reportDefinition.getDisplayName());
            summary.setDescription(reportDefinition.getDescription());
            summary.setIsOnline(reportDefinition.getOnline());
            summary.setAllowAccess(reportDefinition.getAllowAccess());
            reportList.add(summary);
        }
        return reportList;
    }

    /**
     * <p>
     * getAll Reports from all Repositories
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    @Deprecated
    @Override
    public List<DatabaseReportDescription> getAllOnline() {

        List<DatabaseReportDescription> onlineReports = new ArrayList<DatabaseReportDescription>();
        for (ReportRepository m_repo : m_globalReportRepository.getRepositoryList()) {
            for (BasicReportDefinition report : m_repo.getOnlineReports()) {
                DatabaseReportDescription summary = new DatabaseReportDescription();
                summary.setRepositoryId(m_repo.getRepositoryId());
                summary.setId(report.getId());
                summary.setDisplayName(report.getDisplayName());
                summary.setDescription(report.getDescription());
                summary.setIsOnline(report.getOnline());
                summary.setAllowAccess(report.getAllowAccess());
                onlineReports.add(summary);
            }
        }
        return onlineReports;
    }

    public void setGlobalReportRepository(GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }

    @Override
    public void reloadConfigurationFiles() {
        m_globalReportRepository.reloadConfigurationFiles();

    }
}
