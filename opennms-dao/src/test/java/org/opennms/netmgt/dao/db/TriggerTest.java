package org.opennms.netmgt.dao.db;

import java.util.Date;

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

public class TriggerTest extends PopulatedTemporaryDatabaseTestCase {
   
    
    public void testSetIpInterfaceIfIndexLikeCapsdDoes() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        
        assertEquals("ifIndex", 0, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));
        executeSQL("UPDATE ipInterface SET ifIndex = 1 WHERE nodeID = 1 AND ipAddr = '1.2.3.4'");
        assertEquals("ifIndex", 1, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));

        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }
    
    public void testSetIpInterfaceIfIndexLikeCapsdDoesBadIfIndex() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        
        assertEquals("ifIndex", 0, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	executeSQL("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.2.3.4'");
        } catch (Throwable t) {
        	ta.throwableReceived(t);
        } finally {
        	ta.verifyAnticipated();
        }
    }
    
    public void testSetIpInterfaceIfIndexLikeCapsdButOppositeOrder() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        
        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }

    public void testSetIpInterfaceIfIndexLikeCapsdButOppositeOrderUpdateWithBadIfIndex() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        
        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	executeSQL("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.2.3.4'");
        } catch (Throwable t) {
        	ta.throwableReceived(t);
        } finally {
        	ta.verifyAnticipated();
        }
    }
}