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
package org.opennms.netmgt.dao.api;

import java.util.List;

import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>ReportdConfigurationDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ReportdConfigurationDao {
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    ReportdConfiguration getConfig();
    
    /**
     * <p>getReport</p>
     *
     * @param defName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.reportd.Report} object.
     */
    Report getReport(String defName);
    
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<Report> getReports();
        
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;

    /**
     * <p>getPersistFlag</p>
     *
     * @return a boolean.
     */
    boolean  getPersistFlag();
    
    /**
     * <p>getStorageDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getStorageDirectory();
    
    /**
     * <p>deleteReport</p>
     *
     * @param reportName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean deleteReport(String reportName);
    
    
}
