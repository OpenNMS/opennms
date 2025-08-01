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
package org.opennms.features.reporting.dao;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.springframework.core.io.Resource;

import java.util.List;

public interface LocalReportsDao {
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object
     */
    List <BasicReportDefinition> getReports();
    
    /**
     * <p>getOnlineReports</p>
     *
     * @return a {@link java.util.List} object
     */
    List <BasicReportDefinition> getOnlineReports();
    
    /**
     * <p>getReportService</p>
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getReportService(String id);
    
    /**
     * <p>getDisplayName</p>
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getDisplayName(String id);

    /**
     * <p>loadConfiguration</p>
     *
     * Load XML configuration and unmarshalling
     */
    void loadConfiguration() throws Exception;

    /**
     * <p>setLocalReportConfigResource</p>
     * 
     * Set local report config resource for DAO
     * 
     * @param configResource a {@link org.springframework.core.io.Resource} object
     */
    void setConfigResource (Resource configResource);

    /**
     * <p>getConfigResource</p>
     * 
     * Get local report configuration resource for DAO
     * @return a {@link org.springframework.core.io.Resource} object
     */
    Resource getConfigResource ();
}
