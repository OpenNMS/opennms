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
 * <p>Abstract JDBCTemplate class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
abstract public class JDBCTemplate {

    private DataSource m_db;
    private String m_sql;
    
    /**
     * <p>Constructor for JDBCTemplate.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     */
    protected JDBCTemplate(DataSource db, String sql) {
        m_db = db;
        m_sql = sql;
    }

    /**
     * <p>execute</p>
     */
    public void execute() {
         execute(new Object[0]);
     }

    /**
     * <p>execute</p>
     *
     * @param o a {@link java.lang.Object} object.
     */
    public void execute(Object o) {
         execute(new Object[] { o });
     }

    /**
     * <p>execute</p>
     *
     * @param o1 a {@link java.lang.Object} object.
     * @param o2 a {@link java.lang.Object} object.
     */
    public void execute(Object o1, Object o2) {
         execute(new Object[] { o1, o2 } );
     }
    
    /**
     * <p>execute</p>
     *
     * @param o1 a {@link java.lang.Object} object.
     * @param o2 a {@link java.lang.Object} object.
     * @param o3 a {@link java.lang.Object} object.
     */
    public void execute(Object o1, Object o2, Object o3) {
        execute (new Object[] { o1, o2, o3 } );
    }
    
    /**
     * <p>execute</p>
     *
     * @param o1 a {@link java.lang.Object} object.
     * @param o2 a {@link java.lang.Object} object.
     * @param o3 a {@link java.lang.Object} object.
     * @param o4 a {@link java.lang.Object} object.
     */
    public void execute(Object o1, Object o2, Object o3, Object o4) {
        execute (new Object[] { o1, o2, o3, o4 } );
    }

    /**
     * <p>execute</p>
     *
     * @param o1 a {@link java.lang.Object} object.
     * @param o2 a {@link java.lang.Object} object.
     * @param o3 a {@link java.lang.Object} object.
     * @param o4 a {@link java.lang.Object} object.
     * @param o5 a {@link java.lang.Object} object.
     */
    public void execute(Object o1, Object o2, Object o3, Object o4, Object o5) {
        execute (new Object[] { o1, o2, o3, o4, o5 } );
    }



    /**
     * <p>execute</p>
     *
     * @param values an array of {@link java.lang.Object} objects.
     */
    public void execute(Object values[]) {
         try {
             doExecute(values);
         } catch (SQLException e) {
             String vals = argsToString(values);
             throw new DataRetrievalFailureException("Problem executing statement: "+m_sql+" with values "+vals, e);
         }
     }

    private String argsToString(Object[] values) {
        String vals = "[";
         for(int i = 0; i < values.length; i++) {
             if (i != 0)
                 vals += ", ";
             vals += values[i];
         }
         vals += "]";
        return vals;
    }

    private void doExecute(Object values[]) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
             
            Connection conn = m_db.getConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement(m_sql);
            d.watch(stmt);
            for(int i = 0; i < values.length; i++) {
                stmt.setObject(i+1, values[i]);
            }
            executeStmt(stmt);
        } finally {
            d.cleanUp();
        }
    }
    
    /**
     * <p>reproduceStatement</p>
     *
     * @param values an array of {@link java.lang.Object} objects.
     * @return a {@link java.lang.String} object.
     */
    public String reproduceStatement(Object values[]) {
    		return m_sql+": with vals "+argsToString(values);
    }
    
    abstract void executeStmt(PreparedStatement stmt) throws SQLException;

}
