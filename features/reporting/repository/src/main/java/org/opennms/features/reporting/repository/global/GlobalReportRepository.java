/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;

public interface GlobalReportRepository {
    
    public List<BasicReportDefinition> getAllReports();
    public List<BasicReportDefinition> getAllOnlineReports();
    public List<BasicReportDefinition> getReports(String repoId);
    public List<BasicReportDefinition> getOnlineReports(String repoId);
    public String getReportService(String reportId);
    public String getDisplayName(String reportId);
    public String getEngine(String reportId);
    public InputStream getTemplateStream(String reportId);
    public List<ReportRepository> getRepositoryList();
    public void addReportRepository(ReportRepository repository);
    public ReportRepository getRepositoryById(String repoId);
    public void reloadConfigurationFiles();
}
