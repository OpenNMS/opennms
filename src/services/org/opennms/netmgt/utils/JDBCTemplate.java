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

import org.opennms.netmgt.config.DbConnectionFactory;

/**
 * @author brozow
 *
 */
abstract public class JDBCTemplate {

    private DbConnectionFactory m_db;
    private String m_sql;
    
    protected JDBCTemplate(DbConnectionFactory db, String sql) {
        m_db = db;
        m_sql = sql;
    }

    public void execute() {
         execute(new Object[0]);
     }

    public void execute(Object o) {
         execute(new Object[] { o });
     }

    public void execute(Object o1, Object o2) {
         execute(new Object[] { o1, o2 } );
     }
    
    public void execute(Object o1, Object o2, Object o3) {
        execute (new Object[] { o1, o2, o3 } );
    }
    
    public void execute(Object o1, Object o2, Object o3, Object o4) {
        execute (new Object[] { o1, o2, o3, o4 } );
    }

    public void execute(Object o1, Object o2, Object o3, Object o4, Object o5) {
        execute (new Object[] { o1, o2, o3, o4, o5 } );
    }



    public void execute(Object values[]) {
         try {
             doExecute(values);
         } catch (SQLException e) {
             String vals = "[";
             for(int i = 0; i < values.length; i++) {
                 if (i != 0)
                     vals += ", ";
                 vals += values[i];
             }
             vals += "]";
             throw new RuntimeException("Problem executing statement: "+m_sql+" with values "+vals, e);
         }
     }

    private void doExecute(Object values[]) throws SQLException {
        
        Connection conn = m_db.getConnection();
         try {
             PreparedStatement stmt = conn.prepareStatement(m_sql);
             for(int i = 0; i < values.length; i++) {
                 stmt.setObject(i+1, values[i]);
             }
             executeStmt(stmt);
         } finally {
             conn.close();
         }
    }
    
    abstract void executeStmt(PreparedStatement stmt) throws SQLException;

}
