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
package org.opennms.web.svclayer.support;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.model.DatabaseReportDescription;
import org.opennms.web.svclayer.model.ReportRepositoryDescription;

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

        List<DatabaseReportDescription> allReports = new ArrayList<>();

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
        List<ReportRepositoryDescription> result = new ArrayList<>();
        List<ReportRepository> reportRepositoryList = new ArrayList<>();

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
        List<DatabaseReportDescription> onlineReportList = new ArrayList<>();

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
        List<DatabaseReportDescription> reportList = new ArrayList<>();

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

        List<DatabaseReportDescription> onlineReports = new ArrayList<>();
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
