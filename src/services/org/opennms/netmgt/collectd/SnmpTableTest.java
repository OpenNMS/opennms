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

import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpVarBind;

public class SnmpTableTest extends SnmpCollectorTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSingleInstanceTrackerZeroInstance() {
        String sysNameOid = ".1.3.6.1.2.1.1.5";
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, "0");
        assertTrue(it.hasOidForNext());
        String oidForNext = it.getOidForNext();
        assertEquals(sysNameOid, oidForNext);
        assertTrue(it.receivedOid(sysNameOid+".0")) ;
        assertFalse(it.hasOidForNext());
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() {
        String sysNameOid = ".1.3.6.1.2.1.1.5";
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, "1");
        assertTrue(it.hasOidForNext());
        String oidForNext = it.getOidForNext();
        assertEquals(sysNameOid+".0", oidForNext);
        assertTrue(it.receivedOid(sysNameOid+".1")) ;
        assertFalse(it.hasOidForNext());
    }
    
    public void testSingleInstanceTrackerNoMatch() {
        String sysNameOid = ".1.3.6.1.2.1.1.5";
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, "0");
        assertTrue(it.hasOidForNext());
        String oidForNext = it.getOidForNext();
        assertEquals(sysNameOid, oidForNext);
        assertFalse(it.receivedOid(sysNameOid+".1")) ;
        assertFalse(it.hasOidForNext());
    }
    
    public void testListInstanceTrackerWithAllResults() {
        String sysNameOid = ".1.3.6.1.2.1.1.5";
        int instances[] = { 1, 3, 5 };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toString(instances));
        
        for(int i = 0; i < instances.length; i++) {
            assertTrue(it.hasOidForNext());
            String oidForNext = it.getOidForNext();
            assertEquals(sysNameOid+"."+(instances[i]-1), oidForNext);
            assertTrue(it.receivedOid(sysNameOid+"."+instances[i]));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testListInstanceTrackerWithNoResults() {
        String sysNameOid = ".1.3.6.1.2.1.1.5";
        int instances[] = { 1, 3, 5 };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toString(instances));
        
        for(int i = 0; i < instances.length; i++) {
            assertTrue(it.hasOidForNext());
            String oidForNext = it.getOidForNext();
            assertEquals(sysNameOid+"."+(instances[i]-1), oidForNext);
            assertFalse(it.receivedOid(sysNameOid+"."+(instances[i]+1)));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testColumnInstanceTracker() {
        String colOid = ".1.3.6.1.2.1.1.5";
        String nextColOid = ".1.3.6.1.2.1.1.6.2";
        InstanceTracker it = new ColumnInstanceTracker(colOid);
        
        int colLength = 5;
        for(int i = 0; i < colLength; i++) {
            assertTrue(it.hasOidForNext());
            String oidForNext = it.getOidForNext();
            assertEquals((i == 0 ? colOid : colOid+"."+(i-1)), oidForNext);
            assertTrue(it.receivedOid(colOid+"."+i));
        }
        
        assertTrue(it.hasOidForNext());
        String oidForNext = it.getOidForNext();
        assertEquals((colOid+"."+(colLength-1)), oidForNext);
        assertFalse(it.receivedOid(nextColOid));
        assertFalse(it.hasOidForNext());
        
        
        
    }
    
    private String toString(int[] instances) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < instances.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(instances[i]);
        }
        return buf.toString();
    }

    // TODO: add hint columns
    // TODO: add extra required columns like ifType for interface collection
     
    
    public void testColumnGetNextZeroInstance() {
        MibObject sysName = createMibObject("sysName", ".1.3.6.1.2.1.1.5", "0", "string");
        
        SnmpColumn col = new SnmpColumn(sysName.getOid());
        assertTrue(col.hasOidForNext());
        String nextOid = col.getOidForNext();
        assertEquals(sysName.getOid(), nextOid);
        
        col.addResult(nextOid+".0", new SnmpOctetString("sysName".getBytes()));
        
        assertFalse(col.hasOidForNext());
    }

    public void testGetNextVarBinds() {
        addSysDescr();
        addSysName();
        SnmpTable table = new SnmpTable(m_objList);
        
        SnmpVarBind[] varbinds = table.getNextVarBinds();

        // process the varbinds
        assertNotNull(varbinds);
        assertEquals(m_objList.size(), varbinds.length);
        for (int i = 0; i < varbinds.length; i++) {
            SnmpVarBind varBind = varbinds[i];
            MibObject mibObject = (MibObject)m_objList.get(i);
            assertNotNull(varBind);
            assertEquals(mibObject.getOid(), varBind.getName().toString());
        }
        
        // create responses
        for (int i = 0; i < varbinds.length; i++) {
            varbinds[i].setValue(new SnmpOctetString(("result"+i).getBytes()));
        } 
        
        table.processResults(varbinds);
        
        // to do make sure it processed the results correctly
        
        
        
    }

    
    

}
