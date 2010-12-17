//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

public class TriggerSetIpInterfaceKeysOnUpdateTest extends
        PopulatedTemporaryDatabaseTestCase {
    
    public void testSetIpInterfaceKeysOnUpdate() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 2, 'FILTER-NEEDS-CHANGED' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        executeSQL("UPDATE ifServices SET serviceID = 2 WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ifServices id", 3, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", 2, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIpInterfaceKeysOnUpdateIpAddressAllZeroes() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 2, 'FILTER-NEEDS-CHANGED' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'UPDATE ifServices SET ipAddr = '0.0.0.0' WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1': ERROR: IfServices Trigger Exception, Condition 0: ipAddr of 0.0.0.0 is not allowed in ifServices table"));
        try {
            executeSQL("UPDATE ifServices SET ipAddr = '0.0.0.0' WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    
    public void testSetIpInterfaceKeysOnUpdateNullIfIndexBoth()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        executeSQL("UPDATE ifServices SET ifIndex = null WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ifServices id", 2, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", 1, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIpInterfaceKeysOnUpdateNullIfIndexInIfServices()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        executeSQL("UPDATE ifServices SET ifIndex = null WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIpInterfaceKeysOnUpdateNullIfIndexInIpInterface()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");
        executeSQL("UPDATE ifServices SET ifIndex = 1 WHERE nodeID = 1 AND ipAddr = '1.2.3.4' AND serviceID = 1");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ifServices id", 2, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", 1, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }


}
