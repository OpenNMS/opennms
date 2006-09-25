package org.opennms.install;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

public class TriggerTest extends PopulatedTemporaryDatabaseTestCase {
    public void testSetSnmpInterfaceIdInIpInterface() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
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
    public void XXXtestSetSnmpInterfaceIdInIpInterfaceNoSnmpInterfaceEntry()
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
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        Connection connection = getConnection();
        try {
            Statement st = getConnection().createStatement();
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
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 0)");
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

    public void testSetIpInterfaceIdInIfService() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");

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


    public void testSetIpInterfaceIdInIfServiceAllZeroesIpAddress() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '0.0.0.0', 1, 1)': ERROR: IfServices Trigger Exception, Condition 0: ipAddr of 0.0.0.0 is not allowed in ifServices table"));
        try {
            executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '0.0.0.0', 1, 1)");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testSetIpInterfaceIdInIfServiceNullIfIndexBoth()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

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

    public void testSetIpInterfaceIdInIfServiceNullIfIndexInIfServices()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

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

    public void testSetIpInterfaceIdInIfServiceNullIfIndexInIpInterface()
            throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");

        Connection connection = getConnection();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id from ipInterface");
            assertTrue("could not advance to read first results row from ipInterface",
                       rs.next());
            int ipInterfaceId = rs.getInt(1);

            executeSQL("INSERT INTO ifServices (ipInterfaceId, serviceID) VALUES ( "
                       + ipInterfaceId + ", 1)");

            st = connection.createStatement();
            rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
            assertTrue("could not advance to read first row in ResultSet",
                       rs.next());
            rs.getInt(1);
            assertFalse("ifServices.id should be non-null", rs.wasNull());
            assertEquals("ifServices.ipInterfaceId", ipInterfaceId, rs.getInt(2));
            assertFalse("ResultSet contains more than one row", rs.next());
        } finally {
            connection.close();
        }
    }
    
    public void testSetIpInterfaceKeysOnUpdate() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
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
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
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
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
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


    public void testSetIfServiceIdInOutage() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
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
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
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