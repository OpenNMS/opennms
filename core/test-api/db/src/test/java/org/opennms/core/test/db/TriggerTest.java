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

public class TriggerTest extends PopulatedTemporaryDatabaseTestCase {
   
    
    public void testSetIpInterfaceIfIndexLikeCapsdDoes() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        
        assertEquals("ipinterface.id", 1, jdbcTemplate.queryForInt("SELECT id FROM ipinterface"));
        assertEquals("snmpinterface.id", 2, jdbcTemplate.queryForInt("SELECT id FROM snmpinterface"));
        
        assertEquals("ifIndex", 0, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));
        executeSQL("UPDATE ipInterface SET ifIndex = 1 WHERE nodeID = 1 AND ipAddr = '1.2.3.4'");
        assertEquals("ifIndex", 1, jdbcTemplate.queryForInt("SELECT ifIndex FROM ipinterface"));

        assertEquals("snmpInterfaceId", 2, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }
    
    public void testSetIpInterfaceIfIndexLikeCapsdDoesBadIfIndex() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        
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
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        
        assertEquals("snmpInterfaceId", 1, jdbcTemplate.queryForInt("SELECT snmpInterfaceId FROM ipInterface WHERE nodeID = ?", 1));
    }

    public void testSetIpInterfaceIfIndexLikeCapsdButOppositeOrderUpdateWithBadIfIndex() throws Exception {
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
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