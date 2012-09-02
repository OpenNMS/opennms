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

public class TriggerSetIfServiceKeysOnInsertTest extends
        PopulatedTemporaryDatabaseTestCase {


    public void testSetIfServiceIdInOutage() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), 1 )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT outageId, ifServiceId from outages");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            assertEquals("expected outages outageId", 1, rs.getInt(1));
            assertEquals("expected outages ifServiceId", 3, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }

    public void testSetIfServiceIdInOutageNullNodeId()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) VALUES ( nextval('outageNxtId'), null, '1.2.3.4', now(), 1 )': ERROR: Outages Trigger Exception, Condition 1: No service found for... nodeid: <NULL>  ipaddr: 1.2.3.4  serviceid: 1"));
        try {
            executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                    + "VALUES ( nextval('outageNxtId'), null, '1.2.3.4', now(), 1 )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testSetIfServiceIdInOutageNullServiceId()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), null )': ERROR: Outages Trigger Exception, Condition 1: No service found for... nodeid: 1  ipaddr: 1.2.3.4  serviceid: <NULL>"));
        try {
            executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                    + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), null )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

}
