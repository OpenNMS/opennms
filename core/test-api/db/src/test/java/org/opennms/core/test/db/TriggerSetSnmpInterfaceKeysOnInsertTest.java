/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

public class TriggerSetSnmpInterfaceKeysOnInsertTest extends
        PopulatedTemporaryDatabaseTestCase {
    
    public void testSetSnmpInterfaceIdInIpInterface() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected ipInterface id", 2, rs.getInt(1));

            int id = rs.getInt(2);
            assertFalse("expected ipInterface snmpInterfaceId to be non-null",
                        rs.wasNull());
            assertEquals("expected ipInterface snmpInterfaceId to be the same",
                         1, id);
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    /**
     * Test adding an entry to ipInterface with an ifIndex >= 1 that *does not*
     * point to an entry in snmpInterface.  This should be an error.
     */
    public void testSetSnmpInterfaceIdInIpInterfaceNoSnmpInterfaceEntry()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )': ERROR: IpInterface Trigger Notice, Condition 1: No SnmpInterface found for... nodeid: 1 ifindex: 1"));
        try {
            executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testSetSnmpInterfaceIdInIpInterfaceNullIfIndex()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        Connection connection = getConnection();
        try {
                Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("ipInterface id", 2, rs.getInt(1));

            int id = rs.getInt(2);
            assertTrue("ipInterface snmpInterfaceId to be null (was " + id + ")",
                       rs.wasNull());
            assertFalse("ResultSet contains more than one row", rs.next());
            rs.close();
            st.close();
        } finally {
            connection.close();
        }
    }



    public void testSetSnmpInterfaceIdInIpInterfaceNullIfIndexNoSnmpInterface()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("ipInterface id", 1, rs.getInt(1));

            int id = rs.getInt(2);
            assertTrue("ipInterface snmpInterfaceId to be null (was " + id + ")",
                       rs.wasNull());
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetSnmpInterfaceIdInIpInterfaceLessThanOneIfIndex()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 0 )': ERROR: IpInterface Trigger Notice, Condition 1: No SnmpInterface found for... nodeid: 1 ifindex: 0"));
        try {
            executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 0 )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Test adding an entry to ipInterface with an ifIndex < 1 where an
     * entry exists in snmpInterface for the same ifIndex.  The relationship
     * *should* be setup (previously, it would not have been set up for this
     * case).
     */
    public void testSetSnmpInterfaceIdInIpInterfaceLessThanOneIfIndexWithSnmpInterface()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 0)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 0 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT snmpInterfaceID from ipInterface");
            assertTrue("could not advance to read first row in results",
                       rs.next());

            rs.getInt(1);
            assertFalse("ipInterface.snmpInterfaceId should not be null",
                        rs.wasNull());
            assertFalse("results contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }
    
    /**
     * Test adding an ipInterface entry with an snmpInterfaceId with nodeId
     * and ifIndex set
     */
    public void testSetSnmpInterfaceIdInIpInterfaceWithSnmpInterfaceId() {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex, snmpInterfaceId) VALUES ( 1, '1.2.3.4', 1, 1 )");
    }

    /**
     * Test adding an ipInterface entry with an snmpInterfaceId with nodeId
     * set but null ifIndex
     * @throws Exception 
     */
    public void testSetSnmpInterfaceIdInIpInterfaceWithSnmpInterfaceIdNullIfIndex() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, snmpInterfaceId) VALUES ( 1, '1.2.3.4', 1 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT ifIndex from ipInterface");
            assertTrue("could not advance to read first row in results",
                       rs.next());

            assertEquals("ipInterface.ifIndex", 1, rs.getInt(1));
            assertFalse("ipInterface.ifIndex should not be null",
                        rs.wasNull());
            assertFalse("results contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    /**
     * Test adding an ipInterface entry with an snmpInterfaceId with ifIndex
     * set but null nodeId
     * @throws Exception 
     */
    public void testSetSnmpInterfaceIdInIpInterfaceWithSnmpInterfaceIdNullNodeId() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (ipAddr, ifIndex, snmpInterfaceId) VALUES ( '1.2.3.4', 1, 1 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT nodeId from ipInterface");
            assertTrue("could not advance to read first row in results",
                       rs.next());

            assertEquals("ipInterface.nodeId", 1, rs.getInt(1));
            assertFalse("ipInterface.nodeId should not be null",
                        rs.wasNull());
            assertFalse("results contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    /**
     * Test adding an ipInterface entry with an snmpInterfaceId with null nodeId
     * and ifIndex
     * @throws Exception 
     */
    public void testSetSnmpInterfaceIdInIpInterfaceWithSnmpInterfaceIdNullNodeIdAndIpAddr() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (ipAddr, snmpInterfaceId) VALUES ( '1.2.3.4', 1 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT nodeId, ifIndex from ipInterface");
            assertTrue("could not advance to read first row in results",
                       rs.next());

            assertEquals("ipInterface.nodeId", 1, rs.getInt(1));
            assertFalse("ipInterface.nodeId should not be null",
                        rs.wasNull());
            
            assertEquals("ipInterface.ifIndex", 1, rs.getInt(2));
            assertFalse("ipInterface.ifIndex should not be null",
                        rs.wasNull());
            assertFalse("results contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }
}
