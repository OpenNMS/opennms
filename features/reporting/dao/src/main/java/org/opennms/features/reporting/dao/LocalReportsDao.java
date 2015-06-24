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
