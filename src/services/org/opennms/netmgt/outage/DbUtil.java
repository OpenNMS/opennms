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
package org.opennms.netmgt.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DbUtil {

    static public boolean countQueryIsPositive(PreparedStatement openStmt) throws SQLException {
        if (DbUtil.executeCountQuery(openStmt) > 0)
            return true;
        else
            return false;
    }

    static public int executeCountQuery(PreparedStatement openStmt) throws SQLException {
        int numOpenOutages = -1;
        ResultSet rs = openStmt.executeQuery();
        if (rs.next()) {
            numOpenOutages = rs.getInt(1);
        }
    
        // close result set
        rs.close();
    
        // close statement
        openStmt.close();
        return numOpenOutages;
    }

    public static void close(Connection dbConn) {
        Category log = ThreadCategory.getInstance(DbUtil.class);

        // close database connection
        try {
            if (dbConn != null)
                dbConn.close();
        } catch (SQLException e) {
            log.warn("Exception closing JDBC connection", e);
        }
    }

    public static void rollback(Connection dbConn, String msg, SQLException se) {
        Category log = ThreadCategory.getInstance(DbUtil.class);
        log.warn(msg, se);
    
        try {
            if (dbConn != null) dbConn.rollback();
        } catch (SQLException sqle) {
            log.warn("SQL exception during rollback, reason", sqle);
        }
    }

    public static void commit(Connection dbConn, String successMsg, String failMsg) {
        Category log = ThreadCategory.getInstance(DbUtil.class);
        try {
            dbConn.commit();
    
            if (log.isDebugEnabled())
                log.debug(successMsg);
        } catch (SQLException se) {
            String msg = "Rolling back transaction, "+failMsg;
            rollback(dbConn, msg, se);
    
        }
    }

}
