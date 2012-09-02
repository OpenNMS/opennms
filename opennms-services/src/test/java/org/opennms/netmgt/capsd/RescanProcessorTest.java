/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.model.capsd.DbSnmpInterfaceEntry;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValueFactory;

public class RescanProcessorTest extends TestCase {
    
    @Override
    protected void setUp(){
        MockLogAppender.setupLogging();
    }
    
    /**
     * Test for bug #2448.
     */
    public void testBadSnmpIfSpeed() {
        int nodeId = 1;
        int ifIndex = 10;
        
        SnmpValueFactory valFactory = SnmpUtils.getValueFactory();
        
        RescanProcessor.setQueuedRescansTracker(new HashSet<Integer>());
        
        IfTableEntry ifTableEntry = new IfTableEntry();
        ifTableEntry.storeResult(new SnmpResult(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.5"), new SnmpInstId("0"), valFactory.getOctetString("".getBytes())));
        DbSnmpInterfaceEntry dbSnmpInterfaceEntry = DbSnmpInterfaceEntry.create(nodeId, ifIndex);
        RescanProcessor.updateSpeed(ifIndex, ifTableEntry, null, dbSnmpInterfaceEntry);
        
        assertEquals("DbSnmpInterfaceEntry ifSpeed value", 0, dbSnmpInterfaceEntry.getSpeed());
    }
    
    public void testScannableInterface() throws UnknownHostException {
        InetAddress ifaddr1 = InetAddressUtils.addr("127.0.0.1");
        DbIpInterfaceEntry[] dbInterfaces = new DbIpInterfaceEntry[1];
        DbIpInterfaceEntry entry1 = DbIpInterfaceEntry.create(1, ifaddr1, 1);
        dbInterfaces[0] = entry1;
        assertTrue("Should scan 127.0.0.1 if it is the only interface in the list", RescanProcessor.scannableInterface(dbInterfaces, ifaddr1));
        dbInterfaces = new DbIpInterfaceEntry[2];
        InetAddress ifaddr2 = InetAddressUtils.addr("10.0.0.1");
        DbIpInterfaceEntry entry2 = DbIpInterfaceEntry.create(2, ifaddr2, 1);
        dbInterfaces[0] = entry1;
        dbInterfaces[1] = entry2;
        assertFalse("Do not rescan 127.0.0.1 when there are multiple interfaces in the list.",RescanProcessor.scannableInterface(dbInterfaces, ifaddr1));
        assertTrue(RescanProcessor.scannableInterface(dbInterfaces, ifaddr2));
    }
}
