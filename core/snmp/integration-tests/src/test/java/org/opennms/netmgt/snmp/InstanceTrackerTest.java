/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    public void testSingleInstanceTrackerZeroInstance() throws Exception {
        testSingleInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "0"));
    }
    
    public void testSingleInstanceTrackerMultiIdInstance() throws Exception {
        testSingleInstanceTracker("1.2.3", SnmpObjId.get(m_sysNameOid, "1.2.3"));
    }
    
    public void testSingleInstanceTracker(String instance, SnmpObjId receivedOid) throws Exception {
        SnmpInstId inst = new SnmpInstId(instance);
        CollectionTracker it = new SingleInstanceTracker(m_sysNameOid, inst);
        
        testCollectionTrackerInnerLoop(it, SnmpObjId.get(m_sysNameOid, inst), receivedOid, 1);
        
        // ensure that it thinks we are finished
        assertTrue(it.isFinished());
    }
    
    private void testCollectionTrackerInnerLoop(CollectionTracker tracker, final SnmpObjId expectedOid, SnmpObjId receivedOid, final int nonRepeaters) throws Exception {
        testCollectionTrackerInnerLoop(tracker, new SnmpObjId[] { expectedOid }, new SnmpObjId[] { receivedOid }, nonRepeaters);
    }
    
    private void testCollectionTrackerInnerLoop(CollectionTracker tracker, final SnmpObjId[] expectedOids, SnmpObjId[] receivedOids, final int nonRepeaters) throws Exception {
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
        try {
            rp.processErrors(0, 0);
        } catch (SnmpException e) {
            throw new RuntimeException(e);
        }
        for (SnmpObjId receivedOid : receivedOids) {
            rp.processResponse(receivedOid, SnmpUtils.getValueFactory().getOctetString("Value".getBytes()));
        }
        
        
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() throws Exception {
        testSingleInstanceTracker("1", SnmpObjId.get(m_sysNameOid, "1"));

    }
    
    public void testSingleInstanceTrackerNoMatch() throws Exception {
        testSingleInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "1"));
    }
    
    public void testInstanceListTrackerWithAllResults() throws Exception {
        String[] instances = { "1", "3", "5" };
        CollectionTracker it = new InstanceListTracker(m_sysNameOid, toCommaSeparated(instances));
        
        SnmpObjId[] oids = new SnmpObjId[instances.length];
        for(int i = 0; i < instances.length; i++) {
            oids[i] = SnmpObjId.get(m_sysNameOid, instances[i]);
        }
        testCollectionTrackerInnerLoop(it, oids, oids, oids.length);

        assertTrue(it.isFinished());
    }
    
    public void testInstanceListTrackerWithNoResults() throws Exception {
        String[] instances = { "1", "3", "5" };
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

    
    public void testColumnTracker() throws Exception {
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
        final StringBuilder buf = new StringBuilder();
        for(int i = 0; i < instances.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(instances[i]);
        }
        return buf.toString();
    }


}
