/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
