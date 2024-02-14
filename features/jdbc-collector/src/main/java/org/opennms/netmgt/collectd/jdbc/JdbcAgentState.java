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
package org.opennms.netmgt.collectd.jdbc;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.DBTools;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcAgentState {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcAgentState.class);

    private static final String JAS_NO_DATASOURCE_FOUND = "NO_DATASOURCE_FOUND";
    
    private boolean m_useDataSourceName;
    private String m_dataSourceName;
    
    private String m_dbUrl;
    
    Driver m_driver = null;
    Properties m_dbProps = null;
    
    private String m_address;
    private Map<String, JdbcGroupState> m_groupStates = new HashMap<String, JdbcGroupState>();
    
    public JdbcAgentState(InetAddress address, Map<String, Object> parameters) {
        // Save the target's address or hostname.
        m_address = address.getCanonicalHostName();
        
        // Ensure that we have parameters to work with.
        if (parameters == null) {
            throw new NullPointerException("parameter cannot be null");
        }
        
        //setupDatabaseConnections(parameters);
    }
    
    public void setupDatabaseConnections(Map<String, Object> parameters) {
        String dataSourceName = ParameterMap.getKeyedString(parameters, "data-source", JAS_NO_DATASOURCE_FOUND);
        if(dataSourceName.equals(JAS_NO_DATASOURCE_FOUND)) {
            // No 'data-source' parameter was set in the configuration file.
            m_useDataSourceName = false;
            setupJdbcUrl(parameters);
        } else {
            // The 'data-source' parameter was set in the configuration file.
            m_useDataSourceName = true;
            m_dataSourceName = dataSourceName;
        }
    }
    
    protected void setupJdbcUrl(Map<String, Object> parameters) {
        m_useDataSourceName = false;
        
        // Extract the driver class name and create a driver class instance.
        try {
            String driverClass = ParameterMap.getKeyedString(parameters, "driver", DBTools.DEFAULT_JDBC_DRIVER);
            m_driver = (Driver)Class.forName(driverClass).newInstance();
        } catch (Throwable exp) {
            throw new RuntimeException("Unable to load driver class: "+exp.toString(), exp);
        }
        
        LOG.info("Loaded JDBC driver");

        // Get the JDBC url host part
        m_dbUrl = DBTools.constructUrl(ParameterMap.getKeyedString(parameters, "url", DBTools.DEFAULT_URL), m_address);
        LOG.debug("JDBC url: {}", m_dbUrl);

        String dbUser = ParameterMap.getKeyedString(parameters, "user", DBTools.DEFAULT_DATABASE_USER);
        String dbPass = ParameterMap.getKeyedString(parameters, "password", DBTools.DEFAULT_DATABASE_PASSWORD);

        m_dbProps = new Properties();
        m_dbProps.setProperty("user", dbUser);
        m_dbProps.setProperty("password", dbPass);
    }
    
    public Connection getJdbcConnection() throws JdbcCollectorException {
        if(m_useDataSourceName) {
            throw new JdbcCollectorException("Attempt to retrieve a JDBC Connection when the collector should be using the DataSourceFactory!");
        }

        try {
            final Connection con = m_driver.connect(m_dbUrl, m_dbProps);
            if (con == null) {
                throw new SQLException("Driver returned null!");
            }
            return con;
        } catch(SQLException e) {
            throw new JdbcCollectorException("Unable to connect to JDBC URL: '" + m_dbUrl +"'", e);
        }
    }
    
    public Statement createStatement(Connection con) {
        try {
            return con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch(SQLException e) {
            LOG.warn("Unable to create SQL statement: {}", e.getMessage());
            throw new JdbcCollectorException("Unable to create SQL statement: " + e.getMessage(), e);
        }
    }
    
    public ResultSet executeJdbcQuery(Statement stmt, JdbcQuery query) {
        try {
            return stmt.executeQuery(query.getJdbcStatement().getJdbcQuery());
        } catch(SQLException e) {
            //closeAgentConnection();
            
            throw new JdbcCollectorException("Unable to execute query '" + query.getQueryName() + "'! Check your jdbc-datacollection-config.xml configuration!", e);
        }
    }
    
    public void closeConnection(Connection con) {
        if (con == null) return;
        try {
            con.close();
        } catch (SQLException ignore) {
        }   
        
    }

    public void closeStmt(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public void closeResultSet(ResultSet resultset) {
        if (resultset != null) {
            try {
                resultset.close();
            } catch (SQLException ignore) {
            }
        }
    }
    

    public String getAddress() {
        return m_address;
    }

    public void setAddress(String address) {
        m_address = address;
    }
    
    public boolean groupIsAvailable(String groupName) {
        JdbcGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            return false; // If the group availability hasn't been set
            // yet, it's not available.
        }
        return groupState.isAvailable();
    }

    public void setGroupIsAvailable(String groupName, boolean available) {
        JdbcGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            groupState = new JdbcGroupState(available);
        }
        groupState.setAvailable(available);
        m_groupStates.put(groupName, groupState);
    }

    public boolean shouldCheckAvailability(String groupName, int recheckInterval) {
        JdbcGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // If the group hasn't got a status yet, then it should be
            // checked regardless (and setGroupIsAvailable will
            // be called soon to create the status object)
            return true;
        }
        Date lastchecked = groupState.getLastChecked();
        Date now = new Date();
        return (now.getTime() - lastchecked.getTime() > recheckInterval);
    }

    public void didCheckGroupAvailability(String groupName) {
        JdbcGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // Probably an error - log it as a warning, and give up
            LOG.warn("didCheckGroupAvailability called on a group without state - this is odd");
            return;
        }
        groupState.setLastChecked(new Date());
    }

    public String getDataSourceName() {
        return m_dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        m_dataSourceName = dataSourceName;
    }

    public boolean getUseDataSourceName() {
        return m_useDataSourceName;
    }

    public void setUseDataSourceName(boolean useDataSourceName) {
        m_useDataSourceName = useDataSourceName;
    }
    
    

}
