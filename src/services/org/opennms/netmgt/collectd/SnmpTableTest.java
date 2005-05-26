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

    private SnmpObjId sysNameOid;

    protected void setUp() throws Exception {
        super.setUp();
        sysNameOid = new SnmpObjId(".1.3.6.1.2.1.1.5");

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    //
    // Object Id test code
    //
    
    public void testSnmpOidCompare() {
        SnmpObjId oid1 = new SnmpObjId("1.3.5.7");
        SnmpObjId oid1b = new SnmpObjId(".1.3.5.7");
        SnmpObjId oid2 = new SnmpObjId(new int[] {1, 3, 5, 7});
        SnmpObjId oid3 = new SnmpObjId(".1.3.5.8");
        SnmpObjId oid4 = new SnmpObjId(".1.3.5.7.0");
        
        assertArrayEquals(oid1.getIds(), oid2.getIds());
        
        assertEquals(oid1, oid1b);
        
        assertEquals(".1.3.5.7", oid1.toString());
        
        assertEquals(oid1, oid2);
        
        assertFalse(oid1.equals(oid3));
        assertFalse(oid1.equals(oid4));
        
        assertTrue(oid1.compareTo(oid3) < 0);
        assertTrue(oid3.compareTo(oid1) > 0);
        assertTrue(oid1.compareTo(oid4) < 0);
        assertTrue(oid4.compareTo(oid1) > 0);
        
    }
    
    public void testOidAppendPrefixInstance() {
        SnmpObjId base = new SnmpObjId(".1.3.5.7");
        SnmpObjId result = new SnmpObjId(".1.3.5.7.9.8.7.6");

        assertEquals(result, base.append(new int[] {9,8,7,6}));
        assertEquals(result, base.append("9.8.7.6"));
        
        assertTrue(base.isPrefixOf(base));
        assertTrue(base.isPrefixOf(result));
        assertFalse(result.isPrefixOf(base));
        
        SnmpInstId instance = result.getInstance(base);
        assertEquals(new SnmpObjId(".9.8.7.6"), instance);
        assertEquals("9.8.7.6", instance.toString());
    }
    
    public void testDecrement() {
        SnmpObjId oid = new SnmpObjId(".1.3.5.7");
        assertEquals(new SnmpObjId(".1.3.5.6"), oid.decrement());
    }
    
    
    private void assertArrayEquals(int[] a, int[] b) {
        if (a == null) {
            assertNull("expected value is null but actual value is "+b, b);
        } else {
            if (b == null) fail("Expected value is "+a+" but actual value is null"); 
            assertEquals("arrays have different length", a.length, b.length);
            for(int i = 0; i < a.length; i++) {
                assertEquals("array differ at index "+i+" expected: "+a+", actual: "+b, a[i], b[i]);
            }
        }
    }
    
    
    //
    // Test Trackers
    //
    
    public void testSingleInstanceTrackerZeroInstance() {
        testSpecificInstanceTracker("1.2.3", new SnmpObjId(sysNameOid, "0"));
    }
    
    public void testSingleInstanceTrackerMultiIdInstance() {
        testSpecificInstanceTracker("1.2.3", new SnmpObjId(sysNameOid, "1.2.3"));
    }
    
    public void testSpecificInstanceTracker(String instance, SnmpObjId receivedOid) {
        SnmpInstId inst = new SnmpInstId(instance);
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, instance);
        
        testInstanceTrackerInnerLoop(it, inst, receivedOid);
        
        // ensure that it thinks we are finished
        assertFalse(it.hasOidForNext());
    }
    
    private void testInstanceTrackerInnerLoop(InstanceTracker it, SnmpInstId inst, SnmpObjId receivedOid) {
        // ensure it needs to receive something - object id for the instance
        assertTrue(it.hasOidForNext());
        // ensure that is asks for the oid preceeding
        assertEquals(new SnmpObjId(sysNameOid, inst).decrement(), it.getOidForNext());
        // tell it received the expected one and ensure that it agrees
        if (receivedOid.equals(new SnmpObjId(sysNameOid, inst)))
            assertEquals(inst, it.receivedOid(receivedOid));
        else
            assertNull(it.receivedOid(receivedOid));
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() {
        testSpecificInstanceTracker("1.2.3", new SnmpObjId(sysNameOid, "1.2.3"));
    }
    
    public void testSingleInstanceTrackerNoMatch() {
        testSpecificInstanceTracker("0", new SnmpObjId(sysNameOid, "1"));
    }
    
    public void testListInstanceTrackerWithAllResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), new SnmpObjId(sysNameOid, instances[i]));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testListInstanceTrackerWithNoResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), new SnmpObjId(sysNameOid, instances[i]+".0"));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testColumnInstanceTracker() {
        SnmpObjId colOid = new SnmpObjId(".1.3.6.1.2.1.1.5");
        SnmpObjId nextColOid = new SnmpObjId(".1.3.6.1.2.1.1.6.2");
        InstanceTracker it = new ColumnInstanceTracker(colOid);
        
        int colLength = 5;
        
        for(int i = 0; i < colLength; i++) {
            String instance = Integer.toString(i);
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instance), colOid.append(instance));
        }

        // it needs another non matching receipt before it can know its done
        assertTrue(it.hasOidForNext());
        SnmpObjId oidForNext = it.getOidForNext();
        assertEquals(colOid.append(""+(colLength-1)), oidForNext); 
        assertNull(it.receivedOid(nextColOid));

        // now it should be done
        assertFalse(it.hasOidForNext());
        
        
        
    }
    
    private String toCommaSeparated(String[] instances) {
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
        
        SnmpColumn col = new SnmpColumn(sysNameOid, "0");
        assertTrue(col.hasOidForNext());
        SnmpObjId nextOid = col.getOidForNext();
        assertEquals(sysNameOid, nextOid);
        
        Object result = "sysName";
        col.addResult(nextOid.append("0"), result);
        assertEquals(result, col.getResultForInstance("0"));
        
        
        assertFalse(col.hasOidForNext());
    }
    
    public void xtestGetColumnWithMultiInstances() {
        String[] instances = { "1", "2", "3" };
        SnmpColumn col = new SnmpColumn(sysNameOid, "1,2,3");
        
        for(int i = 0; i < instances.length; i++) {
            assertTrue(col.hasOidForNext());
            SnmpObjId expected = new SnmpObjId(sysNameOid, instances[i]);
            assertEquals(expected.decrement(), col.getOidForNext());
        
            Object result = "sysName"+i;
            col.addResult(expected, result);
        }
            
        assertFalse(col.hasOidForNext());

        for(int i = 0; i < instances.length; i++) {
            Object result = "sysName"+i;
            assertEquals(result, col.getResultForInstance(instances[i]));
        }
        
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
