/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.jdbc.request;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

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
}
