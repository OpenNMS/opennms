/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
