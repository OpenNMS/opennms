/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.repository;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;

import java.io.InputStream;
import java.util.List;

/**
 * @author tak
 *
 */
public interface ReportRepository {
    /**
     * <p>getReports</p>
     *
     * Get *ALL* reports from a report repository
     *
     * @return a {@link java.util.List} object
     */
    List <BasicReportDefinition> getReports();
    
    /**
     * <p>getOnlineReports</p>
     *
     * Get all *ONLINE* reports from a report repository
     *
     * @return a {@link java.util.List} object
     */
    List <BasicReportDefinition> getOnlineReports();
    
    /**
     * <p>getReportService</p>
     *
     * Get report service from a specific report by ID
     *
     * @param reportId a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getReportService(String reportId);
    
    /**
     * <p>getDisplayName</p>
     *
     * Get display name from a specific report by ID
     *
     * @param reportId a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getDisplayName(String reportId);

    /**
     * <p>getEngine</p>
     *
     * Get engine for database access from a specific report by ID
     *
     * @param reportId a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getEngine(String reportId);

    /**
     * <p>getTemplateStream</p>
     *
     * Get report template stream from a specific report by ID
     *
     * @param reportId a {@link java.lang.String} object
     * @return a {@link java.io.InputStream} object
     */
    InputStream getTemplateStream(String reportId);

    /**
     * <p>getRepositoryId</p>
     *
     * Get report repository ID
     *
     * @return a {@link java.lang.String} object
     */
    String getRepositoryId();

    /**
     * <p>getRepositoryName</p>
     *
     * Get report repository name
     *
     * @return a {@link java.lang.String} object
     */
    String getRepositoryName();

    /**
     * <p>getRepositoryDescription</p>
     *
     * Get report repository description
     *
     * @return a {@link java.lang.String} object
     */
    String getRepositoryDescription();

    /**
     * <p>getManagementUrl</p>
     *
     * Get repository management URL for subscription services
     *
     * @return a {@link java.lang.String} object
     */
    String getManagementUrl();

    void loadConfiguration();
}
