/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.api.EventdServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

/**
 * <p>JdbcEventdServiceManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JdbcEventdServiceManager implements InitializingBean, EventdServiceManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(JdbcEventdServiceManager.class);
    
    private DataSource m_dataSource;

    /**
     * Cache of service names to service IDs.
     */
    private Map<String, Integer> m_serviceMap = new HashMap<String, Integer>();

    /**
     * <p>Constructor for JdbcEventdServiceManager.</p>
     */
    public JdbcEventdServiceManager() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#getServiceId(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public synchronized int getServiceId(String serviceName) throws DataAccessException {
        Assert.notNull(serviceName, "The serviceName argument must not be null");

        if (m_serviceMap.containsKey(serviceName)) {
            return m_serviceMap.get(serviceName).intValue();
        } else {
            LOG.debug("Could not find entry for '{}' in service name cache.  Looking up in database.", serviceName);
            
            int serviceId;
            try {
                serviceId = new JdbcTemplate(m_dataSource).queryForInt("SELECT serviceID FROM service WHERE serviceName = ?", new Object[] { serviceName });
            } catch (IncorrectResultSizeDataAccessException e) {
                if (e.getActualSize() == 0) {
                    LOG.debug("Did not find entry for '{}' in database.", serviceName);
                    return -1; // not found
                } else {
                    throw e; // more than one found... WTF?!?!
                }
            }
            
            m_serviceMap.put(serviceName, serviceId);
            
            LOG.debug("Found entry for '{}' (ID {}) in database.  Adding to service name cache.", serviceName, serviceId);
            
            return serviceId;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#dataSourceSync()
     */
    /**
     * <p>dataSourceSync</p>
     */
    @Override
    public synchronized void dataSourceSync() {
        m_serviceMap.clear();
        
        new JdbcTemplate(m_dataSource).query(EventdConstants.SQL_DB_SVC_TABLE_READ, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                m_serviceMap.put(resultSet.getString(2), resultSet.getInt(1));
            }
        });
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_dataSource != null, "property dataSource must be set");
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
}
