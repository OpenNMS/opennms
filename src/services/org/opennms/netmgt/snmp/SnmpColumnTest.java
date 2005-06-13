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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.snmp;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class SnmpColumnTest extends SnmpCollectorTestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
        m_agent.loadSnmpTestData(getClass(), "snmpTestData1.properties");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    /*
     * Test the SnmpColumn class
     */
    public void testColumnGetNextZeroInstance() {

        // TODO: add hint columns
        // TODO: add extra required columns like ifType for interface collection

        SnmpColumn col = new SnmpColumn(m_sysNameOid, "0");
        SnmpCollectionTrackerTest.assertTrue(col.hasOidForNext());
        SnmpObjId nextOid = col.getOidForNext();
        assertEquals(m_sysNameOid, nextOid);

        Object result = "sysName";
        col.addResult(nextOid.append("0"), result);
        assertEquals(result, col.getResultForInstance("0"));

        assertFalse(col.hasOidForNext());
    }

    public void testGetColumnWithMultiInstances() {
        String[] instances = { "1", "2", "3" };
        SnmpColumn col = new SnmpColumn(m_sysNameOid, "1,2,3");

        for (int i = 0; i < instances.length; i++) {
            assertTrue(col.hasOidForNext());
            SnmpObjId expected = SnmpObjId.get(m_sysNameOid, instances[i]);
            assertEquals(expected.decrement(), col.getOidForNext());

            Object result = "sysName" + i;
            col.addResult(expected, result);
        }

        assertFalse(col.hasOidForNext());

        for (int i = 0; i < instances.length; i++) {
            Object result = "sysName" + i;
            assertEquals(result, col.getResultForInstance(instances[i]));
        }

    }

    public void testGetColumnForTableColumn() {
        SnmpColumn col = new SnmpColumn(m_ifDescr, "ifIndex");

        LinkedHashMap linkedMap = new LinkedHashMap();

        SnmpObjId next = m_ifDescr;
        while (col.hasOidForNext()) {
            assertEquals(next, col.getOidForNext());
            next = m_agent.getFollowingObjId(next);
            SnmpInstId inst = next.getInstance(m_ifDescr);
            Object result = m_agent.getValueFor(next);
            assertEquals(inst, col.addResult(next, result));
            if (inst != null)
                linkedMap.put(inst, result);
        }

        for (Iterator it = linkedMap.keySet().iterator(); it.hasNext();) {
            SnmpInstId inst = (SnmpInstId) it.next();
            Object val = linkedMap.get(inst);
            assertEquals(val, col.getResultForInstance(inst));
        }

    }

}