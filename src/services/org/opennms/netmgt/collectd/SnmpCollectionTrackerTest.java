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

public class SnmpCollectionTrackerTest extends SnmpCollectorTestCase {
    
    private InstanceTrackerTest m_test;

    protected void setUp() throws Exception {
        super.setUp();
        m_agent.loadSnmpTestData(getClass(), "snmpTestData1.properties");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // TODO: add hint columns
    // TODO: add extra required columns like ifType for interface collection
     
    /*
     * Test the SnmpColumn class
     */
    public void testColumnGetNextZeroInstance() {
        
        SnmpColumn col = new SnmpColumn(m_sysNameOid, "0");
        assertTrue(col.hasOidForNext());
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
        
        for(int i = 0; i < instances.length; i++) {
            assertTrue(col.hasOidForNext());
            SnmpObjId expected = SnmpObjId.get(m_sysNameOid, instances[i]);
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
        SnmpColumn col = new SnmpColumn(m_ifDescr, "ifIndex");
        
        LinkedHashMap linkedMap = new LinkedHashMap();
        
        SnmpObjId next = m_ifDescr;
        while(col.hasOidForNext()) {
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
    public void testMaxVarsPerPdu() {
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
                    SnmpObjId resp = m_agent.getFollowingObjId(req);
                    Object val = m_agent.getValueFor(resp);
                    
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

            for(SnmpObjId mib = m_agent.getFollowingObjId(base); base.isPrefixOf(mib); mib = m_agent.getFollowingObjId(mib)) {
                SnmpInstId inst = mib.getInstance(base);
                Object result = m_agent.getValueFor(mib);
                Map store = tracker.getDataForInstance(inst);
                assertNotNull(store);

                assertEquals(result, store.get(base));
                
            }
        }
    }
    

}
