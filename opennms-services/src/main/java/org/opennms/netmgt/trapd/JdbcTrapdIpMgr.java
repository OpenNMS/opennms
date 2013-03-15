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

package org.opennms.netmgt.trapd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

/**
 * This class represents a singular instance that is used to map trap IP
 * addresses to known nodes.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 */
public class JdbcTrapdIpMgr implements TrapdIpMgr, InitializingBean {
    private DataSource m_dataSource;
    
    /**
     * The SQL statement used to extract the list of currently known IP
     * addresses and their node IDs from the IP Interface table.
     */
    private final  String IP_LOAD_SQL = "SELECT ipAddr, nodeid FROM ipInterface";

    /**
     * A Map of IP addresses and node IDs
     */
    private Map<String, Long> m_knownips = new HashMap<String, Long>();

    /**
     * Default construct for the instance.
     */
    public JdbcTrapdIpMgr() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#dataSourceSync()
     */
    /**
     * <p>dataSourceSync</p>
     */
    public synchronized void dataSourceSync() {
        m_knownips.clear();

        new JdbcTemplate(m_dataSource).query(IP_LOAD_SQL, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                m_knownips.put(rs.getString(1), rs.getLong(2));
            }
        });
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#getNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    public synchronized long getNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.get(addr));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#setNodeId(java.lang.String, long)
     */
    /** {@inheritDoc} */
    public synchronized long setNodeId(String addr, long nodeid) {
        if (addr == null || nodeid == -1) {
            return -1;
        }
        
        return longValue(m_knownips.put(addr, Long.valueOf(nodeid)));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#removeNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    public synchronized long removeNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.remove(addr));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#clearKnownIpsMap()
     */
    /**
     * <p>clearKnownIpsMap</p>
     */
    public synchronized void clearKnownIpsMap() {
        m_knownips.clear();
    }

    private static long longValue(Long result) {
        return (result == null ? -1 : result.longValue());
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

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_dataSource != null, "property dataSource must be set");
    }
}
