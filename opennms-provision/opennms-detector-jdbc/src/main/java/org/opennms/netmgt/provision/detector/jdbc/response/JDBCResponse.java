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
package org.opennms.netmgt.provision.detector.jdbc.response;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author thedesloge
 *
 */
public class JDBCResponse {
    
    private ResultSet m_result;
    private boolean m_isValidProcedureCall = false;
    
    public void receive(Connection conn) throws SQLException {
        
        DatabaseMetaData metadata = conn.getMetaData();
        log().debug("got database metadata");

        m_result = metadata.getCatalogs();
        
    }
    
    public boolean resultSetNotNull() {
        try {
            while (m_result.next())
            {
                m_result.getString(1);
                if (log().isDebugEnabled()) {
                    log().debug("Metadata catalog: '" + m_result.getString(1) + "'");
                }
            }
            
            m_result.close();
            return true;
        } catch (SQLException e) {
            log().info("Unable to get result set", e);
        }

        return false;
    }
    
    public boolean validProcedureCall(){
        return isValidProcedureCall();
    }

    public void setValidProcedureCall(boolean isValidProcedureCall) {
        m_isValidProcedureCall = isValidProcedureCall;
    }

    public boolean isValidProcedureCall() {
        return m_isValidProcedureCall;
    }


    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
