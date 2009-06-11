/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 1, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.threshd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.utils.IfLabel;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */

public class JdbcIfInfoGetter implements IfInfoGetter {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.threshd.IfInfoGetter#getIfInfoForNodeAndLabel(int, java.lang.String)
     */
    public Map<String, String> getIfInfoForNodeAndLabel(int nodeId, String ifLabel) {
        // Get database connection
        java.sql.Connection dbConn = null;
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
        } catch (SQLException e) {
            //log().error("checkIfDir: Failed getting connection to the database: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    
        // Make certain we close the connection
        Map<String, String> ifInfo = new HashMap<String, String>();
        try {
            ifInfo = IfLabel.getInterfaceInfoFromIfLabel(dbConn, nodeId, ifLabel);
        } catch (SQLException e) {
            /*
             * Logging a warning message but processing will
             * continue for
             * this thresholding event, when the event is
             * created it
             * will be created with an interface value set
             * to the primary
             * SNMP interface address and an event source
             * set to
             * <datasource>:<ifLabel>.
             */
            //log().warn("Failed to retrieve interface info from database using ifLabel '" + ifLabel + "': " + e, e);
        } finally {
            // Done with the database so close the connection
            try {
                if (dbConn != null) {
                    dbConn.close();
                }
            } catch (SQLException e) {
                //log().info("checkIfDir: SQLException while closing database connection: " + e, e);
            }
        }
        return ifInfo;
    }

    public String getIfLabel(int nodeId, String ipAddress) {
        // Get database connection
        java.sql.Connection dbConn = null;
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
        } catch (SQLException e) {
            //log().error("checkIfDir: Failed getting connection to the database: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    
        // Make certain we close the connection
        String label = null;
        try {
            label = IfLabel.getIfLabel(dbConn, nodeId, ipAddress);
        } catch (SQLException e) {
        } finally {
            // Done with the database so close the connection
            try {
                if (dbConn != null) {
                    dbConn.close();
                }
            } catch (SQLException e) {
            }
        }
        return label;
    }

}
