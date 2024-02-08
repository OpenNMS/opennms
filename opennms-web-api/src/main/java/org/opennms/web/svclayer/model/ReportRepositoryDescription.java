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

public class ReportRepositoryDescription {
    
    private String m_id;
    private String m_displayName;
    private String m_description;
    private String m_managementUrl;
    
    /**
     * <p>getId</p>
     *
     * @return a {@link String} object.
     */
    public String getId() {
        return m_id;
    }
    /**
     * <p>setId</p>
     *
     * @param id a {@link String} object.
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * <p>getDisplayName</p>
     *
     * @return a {@link String} object.
     */
    public String getDisplayName() {
        return m_displayName;
    }
    /**
     * <p>setDisplayName</p>
     *
     * @param displayName a {@link String} object.
     */

    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * <p>getManagementUrl</p>
     *
     * @return a {@link String} object.
     */
    public String getManagementUrl() {
        return m_managementUrl;
    }
    /**
     * <p>setManagementUrl</p>
     *
     * @param managementUrl a {@link String} object.
     */
    public void setManagementUrl(String managementUrl) {
        m_managementUrl = managementUrl;
    }
}
