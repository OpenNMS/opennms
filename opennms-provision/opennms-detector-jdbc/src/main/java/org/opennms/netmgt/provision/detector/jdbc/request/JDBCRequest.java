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
package org.opennms.netmgt.provision.detector.jdbc.request;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;

/**
 * <p>JDBCRequest class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class JDBCRequest {
    
    /** Constant <code>Null</code> */
    public static final JDBCRequest Null = new JDBCRequest() {
        @Override
        public JDBCResponse send(Connection conn) throws SQLException {
            return null;
        }
    };
    
    private String m_sqyQuery;
    private String m_storedProcedure;
    private String m_schema = "test";
    
    /**
     * <p>Constructor for JDBCRequest.</p>
     */
    public JDBCRequest() {}

    /**
     * <p>send</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @return a {@link org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse} object.
     * @throws java.sql.SQLException if any.
     */
    public JDBCResponse send(Connection conn) throws SQLException {
        if(getStoredProcedure() != null){
            
            String procedureCall = "{ ? = call " + getStoredProcedure() + "()}";
            CallableStatement cs = conn.prepareCall(procedureCall);
            cs.registerOutParameter(1, java.sql.Types.BIT);
            cs.executeUpdate();

            JDBCResponse response = new JDBCResponse();
            response.setValidProcedureCall(cs.getBoolean(1));
            return response;
        }
        if(getSqyQuery() != null) {
            Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(getSqyQuery());
            rs.first();
            boolean validQuery = rs.getRow() == 1;
            st.close();

            JDBCResponse response = new JDBCResponse();
            response.setValidQuery(validQuery);
            return response;
        }
        return null;
    }

    /**
     * <p>setStoredProcedure</p>
     *
     * @param storedProcedure a {@link java.lang.String} object.
     */
    public void setStoredProcedure(String storedProcedure) {
        m_storedProcedure = storedProcedure;
    }

    /**
     * <p>getStoredProcedure</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoredProcedure() {
        return m_storedProcedure;
    }

    /**
     * <p>setSchema</p>
     *
     * @param schema a {@link java.lang.String} object.
     */
    public void setSchema(String schema) {
        m_schema = schema;
    }

    /**
     * <p>getSchema</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSchema() {
        return m_schema;
    }

    /**
     * <p>setSqyQuery</p>
     *
     * @param sqyQuery a {@link java.lang.String} object.
     */
    public void setSqyQuery(String sqyQuery) {
        m_sqyQuery = sqyQuery;
    }

    /**
     * <p>getSqyQuery</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSqyQuery() {
        return m_sqyQuery;
    }
}
