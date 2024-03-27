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
package org.opennms.web.svclayer.model;

/**
 * <p>DatabaseReportDescription class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DatabaseReportDescription {

    /**
     * Id of a report template
     */
    private String m_id;

    /**
     * Id of the repository
     */
    private String m_repositoryId;

    /**
     * Display name for a report template
     */
    private String m_displayName;

    /**
     * Description for a report template
     */
    private String m_description;

    /**
     * Defines if a report is allowed to be accessed by report repository.
     */
    private boolean m_allowAccess;

    /**
     * Configuration as online report
     */
    private boolean m_isOnline;
    
    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return m_id;
    }
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        m_id = id;
    }
    /**
     * <p>getRepositoryId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepositoryId() {
        return m_repositoryId;
    }
    /**
     * <p>setId</p>
     *
     * @param repositoryId a {@link java.lang.String} object.
     */
    public void setRepositoryId(String repositoryId) {
        m_repositoryId = repositoryId;
    }
    /**
     * <p>getDisplayName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayName() {
        return m_displayName;
    }
    /**
     * <p>setDisplayName</p>
     *
     * @param displayName a {@link java.lang.String} object.
     */
    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * <p>setIsOnline</p>
     * Set isOnline property to define if a report can be executed instantly from the WebUI.
     *  
     * @param isOnline a {@link boolean} object.
     */
    public void setIsOnline(boolean isOnline) {
        m_isOnline = isOnline;
    }

    /**
     * <p>getIsOnline</p>
     * Get isOnline property for instant report execution.
     * 
     * @return a {@link boolean} object.
     */
    public boolean getIsOnline() {
        return m_isOnline;
    }

    /**
     * <p>setAllowAccess</p>
     * Set allowAccess for report execution permission. 
     * 
     * @param allowAccess a {@link boolean} object.
     */
    public void setAllowAccess(boolean allowAccess) {
        m_allowAccess = allowAccess;
    }

    /**
     * <p>getAllowAccess</p>
     * Get allowAccess for report execution permission.
     * 
     * @return a {@link boolean} object
     */
    public boolean getAllowAccess() {
        return m_allowAccess;
    }
}
