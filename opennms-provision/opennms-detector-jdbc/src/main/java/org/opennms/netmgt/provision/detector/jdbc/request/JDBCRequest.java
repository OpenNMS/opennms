/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
