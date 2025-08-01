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
package org.opennms.netmgt.dao.mock;

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
    
    /**
     * The SQL statement necessary to read service id and service name into map.
     */
    private static final String SQL_DB_SVC_TABLE_READ = "SELECT serviceID, serviceName FROM service";

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
                serviceId = new JdbcTemplate(m_dataSource).queryForObject("SELECT serviceID FROM service WHERE serviceName = ?", new Object[] { serviceName }, Integer.class);
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
        
        new JdbcTemplate(m_dataSource).query(SQL_DB_SVC_TABLE_READ, new RowCallbackHandler() {
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
