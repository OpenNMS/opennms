/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.filter;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.support.JdbcFilterDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>FilterDaoFactory class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FilterDaoFactory {
    private static FilterDao m_filterDao;

    // Only static methods, so don't let the constructor be called
    private FilterDaoFactory() {
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.dao.FilterDao} object.
     */
    public static FilterDao getInstance() {
        if (m_filterDao == null) {
            init();
        }
        
        return m_filterDao;
    }

    /**
     * <p>setInstance</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.dao.FilterDao} object.
     */
    public static void setInstance(FilterDao filterDao) {
        m_filterDao = filterDao;
    }

    /**
     * <p>init</p>
     */
    protected static synchronized void init() {
        if (m_filterDao != null) {
            return;
        }
        
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not initialize DataSourceFactory: " + e, e);
        }
        jdbcFilterDao.setDataSource(DataSourceFactory.getInstance());
        
        try {
            DatabaseSchemaConfigFactory.init();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not initialize DatabaseSchemaConfigFactory: " + e, e);
        }
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        
        jdbcFilterDao.afterPropertiesSet();
        
        setInstance(jdbcFilterDao);
    }

}
