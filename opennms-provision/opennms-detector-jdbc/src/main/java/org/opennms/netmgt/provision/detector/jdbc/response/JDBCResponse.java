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
package org.opennms.netmgt.provision.detector.jdbc.response;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>JDBCResponse class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class JDBCResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(JDBCResponse.class);
    private ResultSet m_result;
    private boolean m_isValidProcedureCall = false;
    private boolean m_isValidQuery = false;
    
    /**
     * <p>receive</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void receive(Connection conn) throws SQLException {
        
        DatabaseMetaData metadata = conn.getMetaData();
        LOG.debug("got database metadata");

        m_result = metadata.getCatalogs();
        
    }
    
    /**
     * <p>resultSetNotNull</p>
     *
     * @return a boolean.
     */
    public boolean resultSetNotNull() {
        try {
            while (m_result.next())
            {
                m_result.getString(1);
                LOG.debug("Metadata catalog: '{}'", m_result.getString(1));
            }
            
            m_result.close();
            return true;
        } catch (SQLException e) {
            LOG.info("Unable to get result set", e);
        }

        return false;
    }
    
    /**
     * <p>validProcedureCall</p>
     *
     * @return a boolean.
     */
    public boolean validProcedureCall(){
        return isValidProcedureCall();
    }

    /**
     * <p>setValidProcedureCall</p>
     *
     * @param isValidProcedureCall a boolean.
     */
    public void setValidProcedureCall(boolean isValidProcedureCall) {
        m_isValidProcedureCall = isValidProcedureCall;
    }

    /**
     * <p>isValidProcedureCall</p>
     *
     * @return a boolean.
     */
    public boolean isValidProcedureCall() {
        return m_isValidProcedureCall;
    }


    /**
     * <p>isValidQuery</p>
     *
     * @return a boolean.
     */
    public boolean isValidQuery() {
        return m_isValidQuery;
    }

    /**
     * <p>setValidQuery</p>
     *
     * @param isValidQuery a boolean.
     */
    public void setValidQuery(boolean isValidQuery) {
        m_isValidQuery = isValidQuery;
    }

}
