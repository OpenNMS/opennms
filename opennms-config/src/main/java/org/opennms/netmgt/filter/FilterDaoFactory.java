/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.filter;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>FilterDaoFactory class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FilterDaoFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FilterDaoFactory.class);
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
    public static void setInstance(final FilterDao filterDao) {
        LOG.debug("setInstance({})", filterDao);
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
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Could not initialize DataSourceFactory: " + e, e);
        }
        jdbcFilterDao.setDataSource(DataSourceFactory.getInstance());
        
        try {
            DatabaseSchemaConfigFactory.init();
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Could not initialize DatabaseSchemaConfigFactory: " + e, e);
        }
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        
        jdbcFilterDao.afterPropertiesSet();
        
        setInstance(jdbcFilterDao);
    }

}
