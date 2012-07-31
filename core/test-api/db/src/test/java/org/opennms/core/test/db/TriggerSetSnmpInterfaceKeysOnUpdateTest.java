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

import junit.framework.AssertionFailedError;

import org.opennms.test.ThrowableAnticipator;

/**
 * <p>Tests for the setSnmpInterfaceKeysOnUpdate trigger.</p>
 * 
 * <p>Cases that aren't tested:
 * <ul>
 *   <li>Null snmp</li>
 * </ul>
 * </p>
 * @author djgregor
 *
 */
public class TriggerSetSnmpInterfaceKeysOnUpdateTest extends
        PopulatedTemporaryDatabaseTestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
    }
    
    public void testSameSnmpInterfaceIdDifferentNodeId() {
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex, snmpInterfaceId) VALUES ( 1, 1, '1.1.1.1', 1, 1 )");
        
        // Add new node, update snmpInterface, and verify
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 2, now() )");
        assertEquals("snmpInterface update count", 1, jdbcTemplate.update("UPDATE snmpInterface SET nodeId = 2 WHERE nodeID = 1 AND snmpIfIndex = 1"));
        assertEquals("snmpIfIndex after update", 1, jdbcTemplate.queryForInt("SELECT snmpIfIndex FROM snmpInterface"));
        assertEquals("snmpInterfaceId before update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
        
        assertEquals("ipInterface update count", 1, jdbcTemplate.update("UPDATE ipInterface SET nodeId = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'"));
        assertEquals("snmpInterfaceId after update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
    }
    
    public void testSameSnmpInterfaceIdDifferentIfIndex() {
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex, snmpInterfaceId) VALUES ( 1, 1, '1.1.1.1', 1, 1 )");
        
        // Update snmpInterface and verify
        assertEquals("snmpInterface update count", 1, jdbcTemplate.update("UPDATE snmpInterface SET snmpIfIndex = 2 WHERE nodeID = 1 AND snmpIfIndex = 1"));
        assertEquals("snmpIfIndex after update", 2, jdbcTemplate.queryForInt("SELECT snmpIfIndex FROM snmpInterface"));
        assertEquals("snmpInterfaceId before update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
        
        assertEquals("ipInterface update count", 1, jdbcTemplate.update("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'"));
        assertEquals("snmpInterfaceId after update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
    }
    
    
    
    public void testSameSnmpInterfaceIdDifferentIfIndexNull() {
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex, snmpInterfaceId) VALUES ( 1, '1.1.1.1', 1, 1 )");
        
        // Update snmpInterface and verify
        assertEquals("snmpInterface update count", 1, jdbcTemplate.update("UPDATE snmpInterface SET snmpIfIndex = 2 WHERE nodeID = 1 AND snmpIfIndex = 1"));
        assertEquals("snmpIfIndex after update", 2, jdbcTemplate.queryForInt("SELECT snmpIfIndex FROM snmpInterface"));
        assertEquals("snmpInterfaceId before update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
        
        assertEquals("ipInterface update count", 1, jdbcTemplate.update("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'"));
        assertEquals("snmpInterfaceId after update", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface"));
    }

    public void testSetIpInterfaceIfIndexLikeCapsdDoes() throws Exception {
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex) VALUES ( 1, 1, '1.1.1.1', null )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1)");
        
        assertEquals("ifIndex", null, jdbcTemplate.queryForObject("SELECT ifIndex FROM ipinterface", Integer.class));
        executeSQL("UPDATE ipInterface SET ifIndex = 1 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'");
        assertEquals("ifIndex", 1, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));

        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }
    
    public void testSetIpInterfaceIfIndexLikeCapsdDoesBadIfIndex() throws Exception {
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.1.1.1', null )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        
        assertEquals("ifIndex", null, jdbcTemplate.queryForObject("SELECT ifIndex FROM ipinterface", Integer.class));

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
                executeSQL("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'");
        } catch (Throwable t) {
                ta.throwableReceived(t);
        } finally {
                ta.verifyAnticipated();
        }
    }
    
    public void testSetIpInterfaceIfIndexLikeCapsdButOppositeOrder() throws Exception {
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.1.1.1', 1 )");
        
        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }

    public void testSetIpInterfaceIfIndexLikeCapsdButOppositeOrderUpdateWithBadIfIndex() throws Exception {
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.1.1.1', 1 )");
        
        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
                executeSQL("UPDATE ipInterface SET ifIndex = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'");
        } catch (Throwable t) {
                ta.throwableReceived(t);
        } finally {
                ta.verifyAnticipated();
        }
    }
    
    
    public void testSetIpInterfaceToNewNodeWithNodeSnmpInterfaces() throws Exception {
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr) VALUES ( 1, 1, '1.1.1.1')");
        
        // Add new node with no snmp interfaces
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 2, now() )");
        
        // reparent ipInterface to new node
        assertNull("snmpInterfaceId after update", jdbcTemplate.queryForObject("SELECT snmpInterfaceId FROM ipInterface", Object.class));
        
        assertEquals("ipInterface update count", 1, jdbcTemplate.update("UPDATE ipInterface SET nodeId = 2 WHERE nodeID = 1 AND ipAddr = '1.1.1.1'"));
        assertNull("snmpInterfaceId after update", jdbcTemplate.queryForObject("SELECT snmpInterfaceId FROM ipInterface", Object.class));
        

    }

    public void testBugNMS1881() {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 100, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 100, 100, 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex) VALUES ( 100, 100, '1.1.1.1', 1)");

        // The trigger will assign the value of snmpInterfaceId
        assertEquals("snmpInterfaceId after creation of new node", 100, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE id = 100"));

        assertEquals("deleted interface count", 1, jdbcTemplate.update("UPDATE ipInterface SET ismanaged = 'D' WHERE id = 100"));
        assertEquals("deleted node count", 1, jdbcTemplate.update("UPDATE node SET nodetype = 'D' WHERE nodeid = 100"));

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 101, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 101, 101, 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex) VALUES ( 101, 101, '1.1.1.1', 1)");

        assertEquals("snmpInterfaceId after creation of a second new node", 101, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE id = 101"));
    }
    
}
