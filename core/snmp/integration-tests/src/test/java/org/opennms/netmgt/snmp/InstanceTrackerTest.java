/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import junit.framework.TestCase;

public class InstanceTrackerTest extends TestCase {
	
	private SnmpObjId m_sysNameOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");

    static private class MyColumnTracker extends ColumnTracker {

        private boolean m_expectsStorageCall;
        private boolean m_storageCalled;

        @Override
        protected void storeResult(SnmpResult res) {
            m_storageCalled = true;
            assertTrue(m_expectsStorageCall);
        }
        
        protected void assertStoreResultsCalled() {
            if (m_expectsStorageCall) {
                assertTrue(m_storageCalled);
            }
        }
        
        void setExpectsStorageCall(boolean expectsStorageCall) {
            m_expectsStorageCall = expectsStorageCall;
            m_storageCalled = false;
        }

        public MyColumnTracker(SnmpObjId base) {
            super(base);
        }

    }

    public void testSingleInstanceTrackerZeroInstance() {
        testSingleInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "0"));
    }
    
    public void testSingleInstanceTrackerMultiIdInstance() {
        testSingleInstanceTracker("1.2.3", SnmpObjId.get(m_sysNameOid, "1.2.3"));
    }
    
    public void testSingleInstanceTracker(String instance, SnmpObjId receivedOid) {
        SnmpInstId inst = new SnmpInstId(instance);
        CollectionTracker it = new SingleInstanceTracker(m_sysNameOid, inst);
        
        testCollectionTrackerInnerLoop(it, SnmpObjId.get(m_sysNameOid, inst), receivedOid, 1);
        
        // ensure that it thinks we are finished
        assertTrue(it.isFinished());
    }
    
    private void testCollectionTrackerInnerLoop(CollectionTracker tracker, final SnmpObjId expectedOid, SnmpObjId receivedOid, final int nonRepeaters) {
        testCollectionTrackerInnerLoop(tracker, new SnmpObjId[] { expectedOid }, new SnmpObjId[] { receivedOid }, nonRepeaters);
    }
    
    private void testCollectionTrackerInnerLoop(CollectionTracker tracker, final SnmpObjId[] expectedOids, SnmpObjId[] receivedOids, final int nonRepeaters) {
        class OidCheckedPduBuilder extends PduBuilder {
            int count = 0;

            @Override
            public void addOid(SnmpObjId snmpObjId) {
                assertEquals(expectedOids[count].decrement(), snmpObjId);
                count++;
            }

            @Override
            public void setNonRepeaters(int numNonRepeaters) {
                assertEquals(nonRepeaters, numNonRepeaters);
            }

            @Override
            public void setMaxRepetitions(int maxRepititions) {
                assertTrue("MaxRepititions must be positive", maxRepititions > 0);
            }
            
            public int getCount() {
                return count;
            }
            
        }

        // ensure it needs to receive something - object id for the instance
        assertFalse(tracker.isFinished());
        // ensure that is asks for the OID preceding
        OidCheckedPduBuilder builder = new OidCheckedPduBuilder();
        ResponseProcessor rp = tracker.buildNextPdu(builder);
        assertNotNull(rp);
        assertEquals(expectedOids.length, builder.getCount());
        rp.processErrors(0, 0);
        for (SnmpObjId receivedOid : receivedOids) {
            rp.processResponse(receivedOid, SnmpUtils.getValueFactory().getOctetString("Value".getBytes()));
        }
        
        
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() {
        testSingleInstanceTracker("1", SnmpObjId.get(m_sysNameOid, "1"));

    }
    
    public void testSingleInstanceTrackerNoMatch() {
        testSingleInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "1"));
    }
    
    public void testInstanceListTrackerWithAllResults() {
        String instances[] = { "1", "3", "5" };
        CollectionTracker it = new InstanceListTracker(m_sysNameOid, toCommaSeparated(instances));
        
        SnmpObjId[] oids = new SnmpObjId[instances.length];
        for(int i = 0; i < instances.length; i++) {
            oids[i] = SnmpObjId.get(m_sysNameOid, instances[i]);
        }
        testCollectionTrackerInnerLoop(it, oids, oids, oids.length);

        assertTrue(it.isFinished());
    }
    
    public void testInstanceListTrackerWithNoResults() {
        String instances[] = { "1", "3", "5" };
        CollectionTracker it = new InstanceListTracker(m_sysNameOid, toCommaSeparated(instances));
        
        SnmpObjId[] expectedOids = new SnmpObjId[instances.length];
        SnmpObjId[] receivedOids = new SnmpObjId[instances.length];
        for(int i = 0; i < instances.length; i++) {
            expectedOids[i] = SnmpObjId.get(m_sysNameOid, instances[i]);
            receivedOids[i] = expectedOids[i].append("0");
        }
        testCollectionTrackerInnerLoop(it, expectedOids, receivedOids, expectedOids.length);

        assertTrue(it.isFinished());
    }

    
    public void testColumnTracker() {
        SnmpObjId colOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        SnmpObjId nextColOid = SnmpObjId.get(".1.3.6.1.2.1.1.6.2");
        MyColumnTracker tracker = new MyColumnTracker(colOid);
        
        int colLength = 5;
        
        for(int i = 0; i < colLength; i++) {
            String instance = Integer.toString(i);
            tracker.setExpectsStorageCall(true);
            testCollectionTrackerInnerLoop(tracker, SnmpObjId.get(colOid, instance), colOid.append(instance), 0);
            tracker.assertStoreResultsCalled();
        }

        tracker.setExpectsStorageCall(false);
        testCollectionTrackerInnerLoop(tracker, SnmpObjId.get(colOid, ""+colLength), nextColOid, 0);
        tracker.assertStoreResultsCalled();
        
        // now it should be done
        assertTrue(tracker.isFinished());
        
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


}
