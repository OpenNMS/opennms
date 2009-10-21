/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 May 04: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import junit.framework.TestCase;

import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.mock.TestSnmpValue;

public class RescanProcessorTest extends TestCase {
    /**
     * Test for bug #2448.
     */
    public void testBadSnmpIfSpeed() {
        int nodeId = 1;
        int ifIndex = 10;

        RescanProcessor.setQueuedRescansTracker(new HashSet<Integer>());
        RescanProcessor processor = new RescanProcessor(nodeId, false, null, null);
        
        IfTableEntry ifTableEntry = new IfTableEntry();
        ifTableEntry.storeResult(new SnmpResult(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.5"), new SnmpInstId("0"), new TestSnmpValue.StringSnmpValue("")));
        DbSnmpInterfaceEntry dbSnmpInterfaceEntry = DbSnmpInterfaceEntry.create(nodeId, ifIndex);
        processor.updateSpeed(ifIndex, ifTableEntry, null, dbSnmpInterfaceEntry);
        
        assertEquals("DbSnmpInterfaceEntry ifSpeed value", 0, dbSnmpInterfaceEntry.getSpeed());
    }
    
    public void testScannableInterface() throws UnknownHostException {
        InetAddress ifaddr1 = InetAddress.getByName("127.0.0.1");
        DbIpInterfaceEntry[] dbInterfaces = new DbIpInterfaceEntry[1];
        DbIpInterfaceEntry entry1 = DbIpInterfaceEntry.create(1, ifaddr1, 1);
        dbInterfaces[0] = entry1;
        assertTrue("Should scan 127.0.0.1 if it is the only interface in the list", RescanProcessor.scannableInterface(dbInterfaces, ifaddr1));
        dbInterfaces = new DbIpInterfaceEntry[2];
        InetAddress ifaddr2 = InetAddress.getByName("10.0.0.1");
        DbIpInterfaceEntry entry2 = DbIpInterfaceEntry.create(2, ifaddr2, 1);
        dbInterfaces[0] = entry1;
        dbInterfaces[1] = entry2;
        assertFalse("Do not rescan 127.0.0.1 when there are multiple interfaces in the list.",RescanProcessor.scannableInterface(dbInterfaces, ifaddr1));
        assertTrue(RescanProcessor.scannableInterface(dbInterfaces, ifaddr2));
    }
}
