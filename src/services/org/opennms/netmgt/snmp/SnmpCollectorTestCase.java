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
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.opennms.netmgt.collectd.MibObject;
import org.opennms.netmgt.collectd.SNMPCollectorEntry;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.mock.TestAgent;
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpSMI;

public class SnmpCollectorTestCase extends OpenNMSTestCase {

    protected BarrierSignaler m_signaler;
    protected List m_objList;
    private TreeMap m_mibObjectMap;
    public TestAgent m_agent = new TestAgent();
    
    private static final String initalMibObjects[][] = {
        {
            "sysLocation", ".1.3.6.1.2.1.1.6", "0", "string"
        },

        {
            "sysName",     ".1.3.6.1.2.1.1.5", "0", "string"
        },

        {
            "sysContact",  ".1.3.6.1.2.1.1.4", "0", "string"
        },

        {
            "sysUptime",   ".1.3.6.1.2.1.1.3", "0", "timeTicks"
        },

        {
            "sysOid",      ".1.3.6.1.2.1.1.2", "0", "objectid"
        },

        {
            "sysDescr", ".1.3.6.1.2.1.1.1", "0", "string"
        },
        { 
            "ifNumber",    ".1.3.6.1.2.1.2.1", "0", "integer" 
        },
        
        {
            "ifInDiscards", ".1.3.6.1.2.1.2.2.1.13", "ifIndex", "counter"
        },

        {
            "ifOutErrors", ".1.3.6.1.2.1.2.2.1.20", "ifIndex", "counter"
        },

        {
            "ifInErrors", ".1.3.6.1.2.1.2.2.1.14", "ifIndex", "counter"
        },

        {
            "ifOutOctets", ".1.3.6.1.2.1.2.2.1.16", "ifIndex", "counter"
        },

        {
            "ifInOctets", ".1.3.6.1.2.1.2.2.1.10", "ifIndex", "counter"
        },

        {
            "ifSpeed", ".1.3.6.1.2.1.2.2.1.5", "ifIndex", "gauge"
        },
        

    };

    protected SnmpObjId m_sysNameOid;
    protected SnmpObjId m_ifDescr;
    protected SnmpObjId m_ifOutOctets;
    protected SnmpObjId m_invalid;
    
    private int m_version = SnmpSMI.SNMPV1;
    
    public void setVersion(int version) {
        super.setVersion(version);
        m_version = version;
    }

    protected void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();
        m_signaler = new BarrierSignaler(1);
        m_objList = new ArrayList();
        m_mibObjectMap = new TreeMap();
        
        for (int i = 0; i < initalMibObjects.length; i++) {
            String[] mibData = initalMibObjects[i];
            defineMibObject(mibData[0], mibData[1], mibData[2], mibData[3]);
            
        }
        
        m_sysNameOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        m_ifOutOctets = SnmpObjId.get("..1.3.6.1.2.1.2.2.1.16");
        m_invalid = SnmpObjId.get(".1.5.6.1.2.1.1.5");
        m_ifDescr = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");

    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected void assertMibObjectsPresent(SNMPCollectorEntry entry, List objList) {
        assertNotNull(entry);
        assertEquals("Unexpected size for "+entry, objList.size(), getEntrySize(entry));
        for (Iterator it = objList.iterator(); it.hasNext();) {
            MibObject mibObject = (MibObject) it.next();
            assertMibObjectPresent(entry, mibObject);
        }
    }

    private int getEntrySize(SNMPCollectorEntry entry) {
        return entry.size() - (entry.getIfIndex() == null ? 0 : 1);
    }

    private void assertMibObjectPresent(SNMPCollectorEntry entry, MibObject mibObject) {
        String inst = getObjectInstance(entry, mibObject);
        SnmpValue value = entry.getValue(mibObject.getOid()+"."+inst);
        assertNotNull(value);
        assertExpectedType(mibObject, value);
    }

    private void assertExpectedType(MibObject mibObject, SnmpValue value) {
        assertEquals(expectNumeric(mibObject.getType()), value.isNumeric());
    }

    private String getObjectInstance(SNMPCollectorEntry entry, MibObject mibObject) {
        return (mibObject.getInstance() == "ifIndex" ? entry.getIfIndex().toString() : mibObject.getInstance());
    }

    private boolean expectNumeric(String type) {
        if ("string".equals(type)) {
            return false;
        } else if ("timeTicks".equals(type)) {
            return true;
        } else if ("objectid".equals(type)) {
            return false;
        } else if ("integer".equals(type)) {
            return true;
        } else if ("counter".equals(type)) {
            return true;
        } else if ("gauge".equals(type)) {
            return true;
        }
        return false;
    }

    protected MibObject defineMibObject(String alias, String oid, String instance, String type) {
        MibObject mibObj = createMibObject(alias, oid, instance, type);
        m_mibObjectMap.put(mibObj.getAlias(), mibObj);
        m_mibObjectMap.put(mibObj.getOid(), mibObj);
        return mibObj;
    }

    protected MibObject createMibObject(String alias, String oid, String instance, String type) {
        MibObject mibObj = new MibObject();
        mibObj.setAlias(alias);
        mibObj.setOid(oid);
        mibObj.setType(type);
        mibObj.setInstance(instance);
        return mibObj;
    }

    protected void addIfNumber() {
        addMibObject("ifNumber",    ".1.3.6.1.2.1.2.1", "0", "integer");
    }

    protected void addSystemGroup() {
        addSysDescr();
        addSysOid();
//        addSysContact();
        addSysName();
        addSysLocation();
    }

    protected void addSysLocation() {
        addMibObject("sysLocation", ".1.3.6.1.2.1.1.6", "0", "string");
    }

    protected void addSysName() {
        addMibObject("sysName",     ".1.3.6.1.2.1.1.5", "0", "string");
    }

    protected void addSysContact() {
        addMibObject("sysContact",  ".1.3.6.1.2.1.1.4", "0", "string");
    }

    protected void addSysUptime() {
        addMibObject("sysUptime",   ".1.3.6.1.2.1.1.3", "0", "timeTicks");
    }

    protected void addSysOid() {
        addMibObject("sysOid",      ".1.3.6.1.2.1.1.2", "0", "objectid");
    }

    protected void addSysDescr() {
        addMibObject("sysDescr", ".1.3.6.1.2.1.1.1", "0", "string");
    }

    protected void addMibObject(String alias, String oid, String inst, String type) {
        m_objList.add(getMibObject(alias,    oid, inst, type));
    }

    protected MibObject getMibObject(String alias, String oid, String inst, String type) {
        MibObject mibObj = getMibObject(alias);
        if (mibObj != null) return mibObj;
        return defineMibObject(alias, oid, inst, type);
        
    }

    protected MibObject getMibObject(String aliasOrOid) {
        return (MibObject)m_mibObjectMap.get(aliasOrOid);
    }

    protected void addIfTable() {
        addIfSpeed();
        addIfInOctets();
        addIfOutOctets();
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
    }
    
    protected void addIpAddrTable() {
        addIpAdEntAddr();
        addIpAdEntIfIndex();
        addIpAdEntNetMask();
        addIpAdEntBcastAddr();
    }
    
    protected void addInvalid() {
        addMibObject("invalid", ".1.5.6.1.2.1.4.20.1.4", "ifIndex", "counter");
        
    }
    
    

    protected void addIpAdEntBcastAddr() {
        // .1.3.6.1.2.1.4.20.1.4
        // FIXME: be better about non specific instances.. They are not all ifIndex but we are using that to mean a column
        addMibObject("addIpAdEntBcastAddr", ".1.3.6.1.2.1.4.20.1.4", "ifIndex", "ipAddress");
    }

    protected void addIpAdEntNetMask() {
        // .1.3.6.1.2.1.4.20.1.3
        addMibObject("addIpAdEntNetMask", ".1.3.6.1.2.1.4.20.1.3", "ifIndex", "ipAddress");
        
    }

    protected void addIpAdEntIfIndex() {
        // .1.3.6.1.2.1.4.20.1.2
        addMibObject("addIpAdEntIfIndex", ".1.3.6.1.2.1.4.20.1.2", "ifIndex", "integer");
        
    }

    protected void addIpAdEntAddr() {
        // .1.3.6.1.2.1.4.20.1.1
        addMibObject("addIpAdEntAddr", ".1.3.6.1.2.1.4.20.1.1", "ifIndex", "ipAddress");
        
    }

    protected void addIfInDiscards() {
        addMibObject("ifInDiscards", ".1.3.6.1.2.1.2.2.1.13", "ifIndex", "counter");
    }

    protected void addIfOutErrors() {
        addMibObject("ifOutErrors", ".1.3.6.1.2.1.2.2.1.20", "ifIndex", "counter");
    }

    protected void addIfInErrors() {
        addMibObject("ifInErrors", ".1.3.6.1.2.1.2.2.1.14", "ifIndex", "counter");
    }

    protected void addIfOutOctets() {
        addMibObject("ifOutOctets", ".1.3.6.1.2.1.2.2.1.16", "ifIndex", "counter");
    }

    protected void addIfInOctets() {
        addMibObject("ifInOctets", ".1.3.6.1.2.1.2.2.1.10", "ifIndex", "counter");
    }

    protected void addIfSpeed() {
        addMibObject("ifSpeed", ".1.3.6.1.2.1.2.2.1.5", "ifIndex", "gauge");
    }
    
    public void testDoNothing() {}
    
    

}
