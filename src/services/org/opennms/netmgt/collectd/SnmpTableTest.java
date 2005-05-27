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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SnmpTableTest extends SnmpCollectorTestCase {

    private SnmpObjId sysNameOid;
    private SnmpObjId ifDescr;

    protected void setUp() throws Exception {
        super.setUp();
        loadSnmpTestData("snmpTestData1.properties");
        sysNameOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        ifDescr = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");
        

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    //
    // Object Id test code
    //
    
    public void testSnmpOidCompare() {
        SnmpObjId oid1 = SnmpObjId.get("1.3.5.7");
        SnmpObjId oid1b = SnmpObjId.get(".1.3.5.7");
        SnmpObjId oid2 = SnmpObjId.get(new int[] {1, 3, 5, 7});
        SnmpObjId oid3 = SnmpObjId.get(".1.3.5.8");
        SnmpObjId oid4 = SnmpObjId.get(".1.3.5.7.0");
        
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
        SnmpObjId base = SnmpObjId.get(".1.3.5.7");
        SnmpObjId result = SnmpObjId.get(".1.3.5.7.9.8.7.6");

        assertEquals(result, base.append(new int[] {9,8,7,6}));
        assertEquals(result, base.append("9.8.7.6"));
        
        assertTrue(base.isPrefixOf(base));
        assertTrue(base.isPrefixOf(result));
        assertFalse(result.isPrefixOf(base));
        
        SnmpInstId instance = result.getInstance(base);
        assertEquals(SnmpObjId.get(".9.8.7.6"), instance);
        assertEquals("9.8.7.6", instance.toString());
    }
    
    public void testDecrement() {
        SnmpObjId oid = SnmpObjId.get(".1.3.5.7");
        assertEquals(SnmpObjId.get(".1.3.5.6"), oid.decrement());
        
        SnmpObjId oid2 = SnmpObjId.get(".1.3.5.7.0");
        assertEquals(oid, oid2.decrement());
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
        testSpecificInstanceTracker("0", SnmpObjId.get(sysNameOid, "0"));
    }
    
    public void testSingleInstanceTrackerMultiIdInstance() {
        testSpecificInstanceTracker("1.2.3", SnmpObjId.get(sysNameOid, "1.2.3"));
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
        assertEquals(SnmpObjId.get(sysNameOid, inst).decrement(), it.getOidForNext());
        // tell it received the expected one and ensure that it agrees
        
        //FIXME: take this if out an make it explicit as an arg or something (can lead to hidden failures)
        if (receivedOid.equals(SnmpObjId.get(sysNameOid, inst)))
            assertEquals(inst, it.receivedOid(receivedOid));
        else
            assertNull(it.receivedOid(receivedOid));
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() {
        testSpecificInstanceTracker("1", SnmpObjId.get(sysNameOid, "1"));
    }
    
    public void testSingleInstanceTrackerNoMatch() {
        testSpecificInstanceTracker("0", SnmpObjId.get(sysNameOid, "1"));
    }
    
    public void testListInstanceTrackerWithAllResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), SnmpObjId.get(sysNameOid, instances[i]));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testListInstanceTrackerWithNoResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), SnmpObjId.get(sysNameOid, instances[i]+".0"));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testColumnInstanceTracker() {
        SnmpObjId colOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        SnmpObjId nextColOid = SnmpObjId.get(".1.3.6.1.2.1.1.6.2");
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
     
    /*
     * Test the SnmpColumn class
     */
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
    
    public void testGetColumnWithMultiInstances() {
        String[] instances = { "1", "2", "3" };
        SnmpColumn col = new SnmpColumn(sysNameOid, "1,2,3");
        
        for(int i = 0; i < instances.length; i++) {
            assertTrue(col.hasOidForNext());
            SnmpObjId expected = SnmpObjId.get(sysNameOid, instances[i]);
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
    
    public void testGetColumnForTableColumn() {
        SnmpColumn col = new SnmpColumn(ifDescr, "ifIndex");
        
        LinkedHashMap linkedMap = new LinkedHashMap();
        
        SnmpObjId next = ifDescr;
        while(col.hasOidForNext()) {
            assertEquals(next, col.getOidForNext());
            next = getNextSnmpId(next);
            SnmpInstId inst = next.getInstance(ifDescr);
            Object result = getSnmpData(next);
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
    
    // TODO: make sure columns can handle getting multiple responses with out a getNext call
    // TODO: add maxRepititions to columns
    

    /*
     * Test SnmpCollectionTracker
     */
    
    public void testGetNextVarBindsForZeroInstanceVars() {
        addSysDescr();
        addSysName();
        SnmpCollectionTracker tracker = new SnmpCollectionTracker(m_objList);

        final int[] callCount = new int[3];
        final Iterator it = m_objList.iterator();
        ResponseProcessor rp = tracker.buildNextPdu(new PduBuilder() {
            public void addOid(SnmpObjId snmpObjId) {
                callCount[0]++;
                MibObject mibObj = (MibObject)it.next();
                assertEquals(SnmpObjId.get(mibObj.getOid()), snmpObjId);
            }
            public void setNonRepeaters(int numNonRepeaters) {
                callCount[1]++;
            }
            public void setMaxRepititions(int maxRepititions) {
                callCount[2]++;
            }
        });
        assertNotNull(rp);
        
        assertEquals(m_objList.size(), callCount[0]);
        assertEquals(1, callCount[1]);
        assertEquals(1, callCount[2]);
        
        // create responses
        for (int i = 0; i < m_objList.size(); i++) {
            MibObject mibObj = (MibObject)m_objList.get(i);
            rp.processResponse(SnmpObjId.get(mibObj.getOid(), "0"), "response"+i);
        } 
        
        // TODO make sure it processed the results correctly
        Map store = tracker.getDataForInstance(new SnmpInstId("0"));
        assertNotNull(store);
        
        for (int i = 0; i < m_objList.size(); i++) {
            MibObject mibObj = (MibObject)m_objList.get(i);
            assertEquals("response"+i, store.get(SnmpObjId.get(mibObj.getOid())));
        } 
        
    }
    
    public void testCollectSystemGroup() {
        addSystemGroup();
        verifyCollection();
    }
    
    public void testCollectIfTable() {
        addIfTable();
        verifyCollection();
    }
    
    public void testCollectIpAddrTable() {
        addIpAddrTable();
        verifyCollection();
        
    }
    
    public void testSpecificPlusColumn() {
        addSysName();
        addIfSpeed();
        verifyCollection();
    }
    
    public void testAllTables() {
        addSystemGroup();
        addIfTable();
        addIpAddrTable();
        verifyCollection();
    }
    
    // TODO: handle maxVarsPerPdu
    public void xtestMaxVarsPerPdu() {
        fail("Unimplemented!");
    }

    private void verifyCollection() {
        SnmpCollectionTracker tracker = new SnmpCollectionTracker(m_objList);
        final List colTrackers = new ArrayList(m_objList.size());
        for (Iterator it = m_objList.iterator(); it.hasNext();) {
            MibObject mibObj = (MibObject) it.next();
            SnmpObjId base = SnmpObjId.get(mibObj.getOid());
            InstanceTracker colTracker = InstanceTracker.get(base, mibObj.getInstance());
            colTrackers.add(colTracker);
        }
        
        while(!tracker.isFinished()) {
            
            // compute the list of oids we need to request
            final List expectedOids = new ArrayList(colTrackers.size());
            for (Iterator it = colTrackers.iterator(); it.hasNext();) {
                InstanceTracker colTracker = (InstanceTracker) it.next();
                if (colTracker.hasOidForNext())
                    expectedOids.add(colTracker.getOidForNext());
            }
            
            
            // build a pdu for reqeusting them
            final int[] callCount = new int[3];
            ResponseProcessor rp = tracker.buildNextPdu(new PduBuilder() {
                
                int currIndex = 0;

                public void addOid(SnmpObjId snmpObjId) {
                    assertEquals(expectedOids.get(currIndex), snmpObjId);
                    currIndex++;
                    callCount[0]++;
                }

                public void setNonRepeaters(int numNonRepeaters) {
                    assertTrue(numNonRepeaters <= expectedOids.size());
                    callCount[1]++;
                }

                public void setMaxRepititions(int maxRepititions) {
                    assertTrue(maxRepititions > 0);
                    callCount[2]++;
                }
                
            });
            
            // make sure we properly built the pdu
            assertEquals(callCount[0], expectedOids.size());
            assertEquals(1, callCount[1]);
            assertEquals(1, callCount[2]);

            // generate responses and update the tracking
            for (Iterator it = colTrackers.iterator(); it.hasNext();) {
                InstanceTracker colTracker = (InstanceTracker) it.next();
                if (colTracker.hasOidForNext()) {
                    // then we requested it earlier so it is in expectedOids
                    // and the below is the value it has
                    SnmpObjId req = colTracker.getOidForNext();
                    
                    // these are the response for this pdu
                    SnmpObjId resp = getNextSnmpId(req);
                    Object val = getSnmpData(resp);
                    
                    // notify the response processor
                    rp.processResponse(resp, val);
                    
                    // update the tracker
                    colTracker.receivedOid(resp);
                }
            }
            
        }

        for (Iterator it = colTrackers.iterator(); it.hasNext();) {
            InstanceTracker colTracker = (InstanceTracker) it.next();
            SnmpObjId base = colTracker.getBaseOid();

            for(SnmpObjId mib = getNextSnmpId(base); base.isPrefixOf(mib); mib = getNextSnmpId(mib)) {
                SnmpInstId inst = mib.getInstance(base);
                Object result = getSnmpData(mib);
                Map store = tracker.getDataForInstance(inst);
                assertNotNull(store);

                assertEquals(result, store.get(base));
                
            }
        }
    }
    

}
