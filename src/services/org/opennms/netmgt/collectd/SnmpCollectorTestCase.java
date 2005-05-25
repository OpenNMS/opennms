//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpTimeTicks;

public class SnmpCollectorTestCase extends OpenNMSTestCase {

    protected BarrierSignaler m_signaler;
    protected List m_objList;
    protected SnmpPeer m_peer;

    protected void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();
        m_peer = new SnmpPeer(InetAddress.getLocalHost());
        m_signaler = new BarrierSignaler(1);
        m_objList = new ArrayList();
    }

    protected void tearDown() throws Exception {
        getSession().close();
        super.tearDown();
    }

    protected void waitForSignal() throws InterruptedException {
        m_signaler.waitFor();
    }

    protected void assertMibObjectsPresent(SNMPCollectorEntry entry, List objList) {
        assertNotNull(entry);
        assertEquals(objList.size(), getEntrySize(entry));
        for (Iterator it = objList.iterator(); it.hasNext();) {
            MibObject mibObject = (MibObject) it.next();
            assertMibObjectPresent(entry, mibObject);
        }
    }

    private int getEntrySize(SNMPCollectorEntry entry) {
        return entry.size() - (entry.get(SNMPCollectorEntry.IF_INDEX) == null ? 0 : 1);
    }

    private void assertMibObjectPresent(SNMPCollectorEntry entry, MibObject mibObject) {
        String inst = getObjectInstance(entry, mibObject);
        Object value = entry.get(mibObject.getOid()+"."+inst);
        assertNotNull(value);
        assertEquals(getClassForType(mibObject.getType()), value.getClass());
    }

    private String getObjectInstance(SNMPCollectorEntry entry, MibObject mibObject) {
        return (mibObject.getInstance() == "ifIndex" ? (String)entry.get(SNMPCollectorEntry.IF_INDEX) : mibObject.getInstance());
    }

    private Class getClassForType(String type) {
        if ("string".equals(type)) {
            return SnmpOctetString.class;
        } else if ("timeTicks".equals(type)) {
            return SnmpTimeTicks.class;
        } else if ("objectid".equals(type)) {
            return SnmpObjectId.class;
        } else if ("integer".equals(type)) {
            return SnmpInt32.class;
        } else if ("counter".equals(type)) {
            return SnmpCounter32.class;
        } else if ("gauge".equals(type)) {
            return SnmpGauge32.class;
        }
        return Void.class;
    }

    protected MibObject createMibObject(String alias, String oid, String instance, String type) {
        MibObject sysName = new MibObject();
        sysName.setAlias(alias);
        sysName.setOid(oid);
        sysName.setType(type);
        sysName.setInstance(instance);
        return sysName;
    }

    protected SnmpSession getSession() throws Exception {
        return new SnmpSession(m_peer);
    }

}
