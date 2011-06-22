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

package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.web.svclayer.support.DatabaseReportDescription;

/**
 * <p>DatabaseReportListService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface DatabaseReportListService {
    
    /**
     * <p>getAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<DatabaseReportDescription> getAll();
    
    /**
     * <p>getAllOnline</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<DatabaseReportDescription> getAllOnline();
    
    /**
     * <p>setDatabaseReportConfigDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.DatabaseReportConfigDao} object.
     */
    void setDatabaseReportConfigDao(DatabaseReportConfigDao dao);

}
