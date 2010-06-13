//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author brozow
 *
 */
abstract public class JDBCTemplate {

    private DataSource m_db;
    private String m_sql;
    
    protected JDBCTemplate(final DataSource db, final String sql) {
        m_db = db;
        m_sql = sql;
    }

    public void execute(final Object ... values) {
         try {
             doExecute(values);
         } catch (final SQLException e) {
             final String vals = argsToString(values);
             throw new DataRetrievalFailureException("Problem executing statement: "+m_sql+" with values "+vals, e);
         }
     }

    private String argsToString(final Object[] values) {
        final StringBuffer sb = new StringBuffer("[");
         for(int i = 0; i < values.length; i++) {
             if (i != 0)
                 sb.append(", ");
             sb.append(values[i]);
         }
         sb.append("]");
         return sb.toString();
    }

    private void doExecute(final Object values[]) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            final Connection conn = m_db.getConnection();
            d.watch(conn);
            final PreparedStatement stmt = conn.prepareStatement(m_sql);
            d.watch(stmt);
            for(int i = 0; i < values.length; i++) {
                stmt.setObject(i+1, values[i]);
            }
            executeStmt(stmt);
        } finally {
            d.cleanUp();
        }
    }
    
    public String reproduceStatement(final Object values[]) {
    		return m_sql+": with vals "+argsToString(values);
    }
    
    abstract void executeStmt(PreparedStatement stmt) throws SQLException;

}
