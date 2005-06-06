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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestSuite;

import org.opennms.netmgt.collectd.mock.BulkPdu;
import org.opennms.netmgt.collectd.mock.NextPdu;
import org.opennms.netmgt.collectd.mock.RequestPdu;
import org.opennms.netmgt.collectd.mock.ResponsePdu;
import org.opennms.netmgt.collectd.mock.TestPdu;
import org.opennms.netmgt.collectd.mock.TestVarBind;


public class SnmpCollectionTrackerTest extends SnmpCollectorTestCase {
    
    public static TestSuite suite() {
        Class testClass = SnmpCollectionTrackerTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", V1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", V2));
        return suite;
    }

    
    private static interface TestPduBuilder extends PduBuilder {
        public RequestPdu getPdu();
    }
    private static class GetNextBuilder implements TestPduBuilder {
        private final NextPdu m_nextPdu;

        private GetNextBuilder() {
            m_nextPdu = TestPdu.getNext();
        }

        public RequestPdu getPdu() {
            return m_nextPdu;
        }
        public void addOid(SnmpObjId snmpObjId) {
            m_nextPdu.addVarBind(snmpObjId);
        }

        public void setNonRepeaters(int numNonRepeaters) {
        }

        public void setMaxRepititions(int maxRepititions) {
        }

    }
    
    private static class GetBulkBuilder implements TestPduBuilder {
        
        private BulkPdu m_bulkPdu;

        public GetBulkBuilder() {
            m_bulkPdu = TestPdu.getBulk();
        }

        public RequestPdu getPdu() {
            return m_bulkPdu;
        }

        public void addOid(SnmpObjId snmpObjId) {
            m_bulkPdu.addVarBind(snmpObjId);
        }

        public void setNonRepeaters(int numNonRepeaters) {
            m_bulkPdu.setNonRepeaters(numNonRepeaters);
        }

        public void setMaxRepititions(int maxRepititions) {
            m_bulkPdu.setMaxRepititions(maxRepititions);
        }
        
    }

    public static final int V1 = 1;
    public static final int V2 = 2;
    
    public int m_version = V1;
    
    public void setVersion(int version) {
        m_version = version;
    }

    protected void setUp() throws Exception {
        super.setUp();
        switch (m_version) {
        case V1:
            m_agent.setBehaviorToV1();
            break;
        case V2:
            m_agent.setBehaviorToV2();
            break;
        default:
            throw new IllegalStateException("Don't understand version number "+m_version);
        }
        
        m_agent.loadSnmpTestData(getClass(), "snmpTestData1.properties");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetNextVarbindsForZeroInstanceVars() {
        addSysDescr();
        addSysName();
        SnmpCollectionTracker tracker = new SnmpCollectionTracker(m_objList);

        TestPduBuilder pduBuilder = getPduBuilder();
        ResponseProcessor rp = tracker.buildNextPdu(pduBuilder, 2);
        RequestPdu next = pduBuilder.getPdu();
        
        assertNotNull(rp);
        
        assertEquals(2, next.size());
        
        ResponsePdu resp = m_agent.send(next);
        
        assertNotNull(resp);
        assertEquals(2, resp.size());
        
        // create responses
        for (int i = 0; i < resp.size(); i++) {
            TestVarBind varBind = (TestVarBind)resp.getVarBindAt(i);
            rp.processResponse(varBind.getObjId(), varBind.getValue());
        } 
        
        for(int i = 0; i < m_objList.size(); i++) {
            MibObject mibObj = (MibObject)m_objList.get(i);
            verifyColumnData(tracker, SnmpObjId.get(mibObj.getOid()));
        }

    }
    
    public void testCollectSystemGroup() {
        addSystemGroup();
        verifyCollection(5);
    }
    
    public void testCollectIfTable() {
        addIfTable();
        verifyCollection(5);
    }
    
    public void testCollectIpAddrTable() {
        addIpAddrTable();
        verifyCollection(5);
        
    }
    
    public void testSpecificPlusColumn() {
        addSysName();
        addIfSpeed();
        verifyCollection(5);
    }
    
    public void testAllTables() {
        addSystemGroup();
        addIfTable();
        addIpAddrTable();
        verifyCollection(50);
    }
    
    private void verifyCollection(int maxVarsPerPdu) {
        SnmpCollectionTracker tracker = new SnmpCollectionTracker(m_objList);
        
        while(!tracker.isFinished()) {
            
            // build a pdu for reqeusting them
            TestPduBuilder pduBuilder = getPduBuilder();
            ResponseProcessor rp = tracker.buildNextPdu(pduBuilder, maxVarsPerPdu);
            
            RequestPdu request = pduBuilder.getPdu();
            assertTrue(request.size() <= maxVarsPerPdu);
            
            ResponsePdu response = m_agent.send(request);
            System.err.println("Get a response with "+response.size()+" vars");
            for (Iterator it = response.getVarBinds().iterator(); it.hasNext();) {
                TestVarBind varBind = (TestVarBind) it.next();
                rp.processResponse(varBind.getObjId(), varBind.getValue());
            }

        }

        verifyCollectedData(tracker);
        

    }

    private TestPduBuilder getPduBuilder() {
        switch (m_version) {
        case V1:
            return new GetNextBuilder();
        case V2:
            return new GetBulkBuilder();
        default:
            throw new IllegalStateException("Don't understand version "+m_version);
        }
    }

    private void verifyCollectedData(SnmpCollectionTracker tracker) {
        for (Iterator it = m_objList.iterator(); it.hasNext();) {
            MibObject mibObj = (MibObject) it.next();
            SnmpObjId base = SnmpObjId.get(mibObj.getOid());
            verifyColumnData(tracker, base);
        }
        Set instances = tracker.getInstances();
        for (Iterator iter = instances.iterator(); iter.hasNext();) {
            SnmpInstId inst = (SnmpInstId) iter.next();
            Map store = tracker.getDataForInstance(inst);
            
            for (Iterator it = store.keySet().iterator(); it.hasNext();) {
                SnmpObjId base = (SnmpObjId) it.next();
                verifyBaseRequested(base);
                SnmpObjId reqId = SnmpObjId.get(base, inst);
                Object val = m_agent.getValueFor(reqId);
                assertEquals(store.get(base), val);
            }
            
        }
    }

    private void verifyBaseRequested(SnmpObjId base) {
        for (Iterator it = m_objList.iterator(); it.hasNext();) {
            MibObject mibObj = (MibObject) it.next();
            SnmpObjId mibOid = SnmpObjId.get(mibObj.getOid());
            if (base.equals(mibOid))
                return;
        }
        
        fail("Expected "+base+" to be in the m_objList");
    }

    private void verifyColumnData(SnmpCollectionTracker tracker, SnmpObjId column) {
        
        for(SnmpObjId mib = m_agent.getFollowingObjId(column); column.isPrefixOf(mib); mib = m_agent.getFollowingObjId(mib)) {
            SnmpInstId inst = mib.getInstance(column);
            Object result = m_agent.getValueFor(mib);
        
            Map store = tracker.getDataForInstance(inst);
            assertNotNull("No Instance data for "+mib, store);
            assertEquals("Unexpected value collected for oid "+mib, result, store.get(column));
            
        }
        
        
        
    }
    

}
