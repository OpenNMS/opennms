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
package org.opennms.netmgt.collectd.mock;

import java.util.NoSuchElementException;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.collectd.SnmpObjId;
import org.opennms.netmgt.mock.MockUtil;

public class TestAgentTest extends TestCase {
    
    private static final int columnLength = 10;
    private static final String col3Base = ".1.3.5.1.2.3";
    private static final String col2Base = ".1.3.5.1.2.2";
    private static final String col1Base = ".1.3.5.1.2.1";
    private static final String zeroInst2Base = ".1.3.5.1.1.2";
    private static final String zeroInst1Base = ".1.3.5.1.1.1";
    private static final String invalid = ".1.5.5.5";
        
    private TestAgent m_agent;

    protected void setUp() throws Exception {
        m_agent = new TestAgent();
        Properties agentData = new Properties();
        addZeroInstance(agentData, zeroInst1Base);
        addZeroInstance(agentData, zeroInst2Base);
        addColumn(agentData, col1Base, 1, columnLength);
        addColumn(agentData, col2Base, 1, columnLength);
        addColumn(agentData, col3Base, 1, columnLength);
        m_agent.setAgentData(agentData);
        
        MockUtil.println("---------- Begin Test "+getName()+"---------------");
    }

    private void addColumn(Properties agentData, String base, int start, int end) {
        for(int inst = start; inst <= end; inst++) {
            addInstance(agentData, base, inst);
        }
    }

    private void addZeroInstance(Properties agentData, String oid) {
        addInstance(agentData, oid, 0);
    }
    
    private void addInstance(Properties agentData, String oid, int inst) {
        agentData.put(oid+"."+inst, getValueFor(oid+"."+inst));
    }

    private Object getValueFor(String oid) {
        return getValueFor(SnmpObjId.get(oid));
    }
    
    private Object getValueFor(SnmpObjId oid) {
        return oid+"-value";
    }
    
    public void testAgentValueFor() {
        SnmpObjId z1 = SnmpObjId.get(zeroInst1Base, "0");
        SnmpObjId z2 = SnmpObjId.get(zeroInst2Base, "0");
        
        assertEquals(getValueFor(z1), m_agent.getValueFor(z1));
        assertEquals(getValueFor(z2), m_agent.getValueFor(z2));
        // make sure they are not the same
        assertFalse(getValueFor(z2).equals(m_agent.getValueFor(z1)));
        
        // try a column
        for(int i = 1; i <= columnLength; i++) {
            SnmpObjId colOid = SnmpObjId.get(col2Base, ""+i);
            assertEquals(getValueFor(colOid), m_agent.getValueFor(colOid));
        }
        
        // what should it do if you ask for an invalid one - return null
        assertNull(m_agent.getValueFor(SnmpObjId.get(".1.1.1.1.1")));
        
    }
    
    public void testFollowingOid() {
        SnmpObjId z1 = SnmpObjId.get(zeroInst1Base);
        SnmpObjId z2 = SnmpObjId.get(zeroInst2Base);
        SnmpObjId z1dot0 = SnmpObjId.get(z1, "0");
        SnmpObjId z2dot0 = SnmpObjId.get(z2, "0");
        SnmpObjId col1 = SnmpObjId.get(col1Base);
        SnmpObjId col1dot1 = SnmpObjId.get(col1, "1");
        SnmpObjId col2 = SnmpObjId.get(col2Base);
        SnmpObjId col2dot1 = SnmpObjId.get(col2, "1");
        SnmpObjId col2dot10 = SnmpObjId.get(col2, "10");
        SnmpObjId col3 = SnmpObjId.get(col3Base);
        SnmpObjId col3dot1 = SnmpObjId.get(col3, "1");
        SnmpObjId col3dot10 = SnmpObjId.get(col3, "10");
        
        assertEquals(z1dot0, m_agent.getFollowingObjId(z1));
        assertEquals(z2dot0, m_agent.getFollowingObjId(z1dot0));
        assertEquals(z2dot0, m_agent.getFollowingObjId(z2));
        assertEquals(col1dot1, m_agent.getFollowingObjId(z2dot0));
        assertEquals(col1dot1, m_agent.getFollowingObjId(col1));
        
        assertEquals(col3dot1, m_agent.getFollowingObjId(col2dot10));

        try {
            m_agent.getFollowingObjId(col3dot10);
            fail("Expected exception");
        } catch (NoSuchElementException e) {
            
        }
    }
    
    // TODO generate tooBig error
    // TODO generate genErr
    // TODO generate end of mib, errorIndex for v1, endOfMibView of v2
    // TODO generate partial getBulk responses
    
    // TODO simulate bad agents by returning data out of order
    // TODO simulate bad agents by repeating same oid on getnext, bulk

    public void testGet() {
        TestPdu get = TestPdu.getGet();
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base, 0);
        get.addVarBind(col1Base, 1);
        
        validateGetResponse(get, m_agent.send(get));
    }
    
    // TODO make sure v1 agent fails to respond to getBulk
    
    public void xtestGetWithInvalidOidV1() {
        m_agent.setBehaviorToV1();
        
        TestPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(invalid, 0);
        get.addVarBind(zeroInst2Base, 0);
        
        TestPdu resp = m_agent.send(get);
        
        assertEquals(TestPdu.NO_SUCH_NAME, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
        
    }

    private void validateGetResponse(TestPdu get, TestPdu resp) {
        assertNotNull(resp);
        
        // determine if errors are expected
        
        
        assertEquals(get.size(), resp.size());
        for(int i = 0; i < resp.size(); i++) {
            assertEquals(get.getVarBindAt(i).getObjId(), resp.getVarBindAt(i).getObjId());
            verifyObjIdValue(resp.getVarBindAt(i));
        }
    }
    
    public void testNext() {
        TestPdu pdu = TestPdu.getNext();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        
        validateNextResponse(pdu, m_agent.send(pdu));
    }

    private void validateNextResponse(TestPdu pdu, TestPdu resp) {
        assertNotNull(resp);
        assertEquals(pdu.size(), resp.size());
        for(int i = 0; i < resp.size(); i++) {
            assertEquals(m_agent.getFollowingObjId(pdu.getVarBindAt(i).getObjId()), resp.getVarBindAt(i).getObjId());
            verifyObjIdValue(resp.getVarBindAt(i));
        }
    }
    
    public void testBulk() {
        TestPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        validateBulkResponse(pdu, m_agent.send(pdu));
    }

    private void validateBulkResponse(TestPdu pdu, TestPdu resp) {
        assertNotNull(resp);
        
        int nonRepeaters = pdu.getNonRepeaters();
        int repeaters = (pdu.size() - nonRepeaters);
        
        // validate the length
        assertEquals(nonRepeaters+(repeaters*pdu.getMaxRepititions()), resp.size());
        
        // validate the nonRepeaters
        for(int i = 0; i < nonRepeaters; i++) {
            assertEquals(m_agent.getFollowingObjId(pdu.getVarBindAt(i).getObjId()), resp.getVarBindAt(i).getObjId());
            verifyObjIdValue(resp.getVarBindAt(i));
        }
        
        // validate the repeaters
        for(int i = 0; i < repeaters; i++) {
            SnmpObjId oid = pdu.getVarBindAt(i+nonRepeaters).getObjId();
            for(int count = 0; count < pdu.getMaxRepititions(); count++) {
                SnmpObjId nextOid = m_agent.getFollowingObjId(oid);
                assertEquals(nextOid, resp.getVarBindAt(nonRepeaters+(count*repeaters)+i).getObjId());
                verifyObjIdValue(resp.getVarBindAt(i));
                oid = nextOid;
            } 
        }
    }

    private void verifyObjIdValue(TestVarBind varbind) {
        assertEquals(getValueFor(varbind.getObjId()), varbind.getValue());
        
    }

}
