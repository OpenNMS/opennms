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
