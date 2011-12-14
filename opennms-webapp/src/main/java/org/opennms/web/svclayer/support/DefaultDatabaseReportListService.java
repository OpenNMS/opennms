/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2009-2011 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.global.DefaultGlobalReportRepository;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.features.reporting.repository.ReportRepository;

import org.opennms.web.svclayer.DatabaseReportListService;

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

    private GlobalReportRepository m_globalRepository = new DefaultGlobalReportRepository();

    /**
     * <p>
     * getAll Reports from all Repositories
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    public List<DatabaseReportDescription> getAll() {

        List<DatabaseReportDescription> allReports = new ArrayList<DatabaseReportDescription>();

        for (ReportRepository globalRepository : m_globalRepository.getRepositoryList()) {
            for (BasicReportDefinition report : globalRepository.getReports()) {
                DatabaseReportDescription summary = new DatabaseReportDescription();
                summary.setRepositoryId(globalRepository.getRepositoryId());
                summary.setId(report.getId());
                summary.setDisplayName(report.getDisplayName());
                summary.setDescription(report.getDescription());
                allReports.add(summary);
            }
        }
        return allReports;
    }

    /**
     * <p>
     * getAll Reports from all Repositories
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    public List<DatabaseReportDescription> getAllOnline() {

        List<DatabaseReportDescription> onlineReports = new ArrayList<DatabaseReportDescription>();
        for (ReportRepository m_repo : m_globalRepository.getRepositoryList()) {
            for (BasicReportDefinition report : m_repo.getOnlineReports()) {
                DatabaseReportDescription summary = new DatabaseReportDescription();
                summary.setRepositoryId(m_repo.getRepositoryId());
                summary.setId(report.getId());
                summary.setDisplayName(report.getDisplayName());
                summary.setDescription(report.getDescription());
                onlineReports.add(summary);
            }
        }
        return onlineReports;
    }
}
