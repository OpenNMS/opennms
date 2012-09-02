/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

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
