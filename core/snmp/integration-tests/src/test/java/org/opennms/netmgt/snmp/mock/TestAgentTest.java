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

package org.opennms.netmgt.snmp.mock;

import java.util.NoSuchElementException;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class TestAgentTest extends TestCase {
    
    private static final int columnLength = 10;
    private static final String col3Base = ".1.3.5.1.2.3";
    private static final String col2Base = ".1.3.5.1.2.2";
    private static final String col1Base = ".1.3.5.1.2.1";
    private static final String zeroInst2Base = ".1.3.5.1.1.2";
    private static final String zeroInst1Base = ".1.3.5.1.1.1";
    private static final String invalid = ".1.5.5.5";
        
    private TestAgent m_agent;

    @Override
    protected void setUp() throws Exception {
        m_agent = new TestAgent();
        Properties agentData = new Properties();
        addZeroInstance(agentData, zeroInst1Base);
        addZeroInstance(agentData, zeroInst2Base);
        addColumn(agentData, col1Base, 1, columnLength);
        addColumn(agentData, col2Base, 1, columnLength);
        addColumn(agentData, col3Base, 1, columnLength);
        m_agent.setAgentData(agentData);
        
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
        agentData.put(oid+"."+inst, "STRING: "+getValueFor(oid+"."+inst).toString());
    }

    private Object getValueFor(String oid) {
        return getValueFor(SnmpObjId.get(oid));
    }
    
    private Object getValueFor(SnmpObjId oid) {
        return new MockSnmpValue(SnmpValue.SNMP_OCTET_STRING, oid+"-value");
    }
    
    public void testConstantObjects() {
        assertEquals("noSuchObject", MockSnmpValue.NO_SUCH_OBJECT.toString());
        assertEquals("noSuchInstance", MockSnmpValue.NO_SUCH_INSTANCE.toString());
        assertEquals("endOfMibView", MockSnmpValue.END_OF_MIB.toString());
    }
    
    public void testEmptyAgent() {
        TestAgent agent = new TestAgent();
        SnmpObjId z1 = SnmpObjId.get(zeroInst1Base, "0");

        try {
            agent.getValueFor(z1);
            fail("Expected exception");
        } catch (AgentNoSuchObjectException e) {
            
        }
        
    }
    
    public void testLoadSnmpData() throws Exception {
        TestAgent agent = new TestAgent();
        agent.loadSnmpTestData(getClass(), "/loadSnmpDataTest.properties");
        SnmpObjId z1 = SnmpObjId.get(zeroInst1Base, "0");
        assertEquals("TestData", agent.getValueFor(z1).toString());
        
    }
    
    public void testAgentValueFor() {
        SnmpObjId z1 = SnmpObjId.get(zeroInst1Base, "0");
        SnmpObjId z2 = SnmpObjId.get(zeroInst2Base, "0");
        
        assertEquals(getValueFor(z1).toString(), m_agent.getValueFor(z1).toString());
        assertEquals(getValueFor(z2).toString(), m_agent.getValueFor(z2).toString());
        // make sure they are not the same
        assertFalse(getValueFor(z2).equals(m_agent.getValueFor(z1)));
        
        // try a column
        for(int i = 1; i <= columnLength; i++) {
            SnmpObjId colOid = SnmpObjId.get(col2Base, ""+i);
            assertEquals(getValueFor(colOid).toString(), m_agent.getValueFor(colOid).toString());
        }
        
        // what should it do if you ask for an invalid one - return null
        try {
            m_agent.getValueFor(SnmpObjId.get(".1.1.1.1.1"));
            fail("expected an exception");
        } catch (AgentNoSuchObjectException e) {
            
        }
        
        SnmpObjId objId = SnmpObjId.get(zeroInst1Base, "1");
        try {
            m_agent.getValueFor(objId);
            fail("Expected no such instance exception");
        } catch (AgentNoSuchInstanceException e) {
            
        }
        
        RuntimeException exception = m_agent.introduceGenErr(objId);
        try {
            m_agent.getValueFor(objId);
        } catch (RuntimeException e) {
            assertSame(exception, e);
        }
        
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
        
        m_agent.introduceSequenceError(col2dot10, col2dot1);
        assertEquals(col2dot1, m_agent.getFollowingObjId(col2dot10));
    }
    
    public void testGet() {
        GetPdu get = TestPdu.getGet();
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base, 0);
        get.addVarBind(col1Base, 1);
        
        validateGetResponse(get, m_agent.send(get));
    }

    public void testGetTooBig() {
        m_agent.setBehaviorToV1();
        m_agent.setMaxResponseSize(5);
        GetPdu get = TestPdu.getGet();
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base, 0);
        get.addVarBind(col1Base, 1);
        get.addVarBind(col1Base, 2);
        get.addVarBind(col1Base, 3);
        get.addVarBind(col2Base, 1);
        get.addVarBind(col2Base, 2);
        get.addVarBind(col2Base, 3);
        get.addVarBind(col3Base, 1);
        get.addVarBind(col3Base, 2);
        get.addVarBind(col3Base, 3);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.TOO_BIG_ERR, resp.getErrorStatus());
        assertEquals(0, resp.getErrorIndex());
    }

    
    public void testGetWithInvalidOidV1() {
        m_agent.setBehaviorToV1();
        
        GetPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(invalid, 0);
        get.addVarBind(zeroInst2Base, 0);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.NO_SUCH_NAME_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
        
    }
    
    public void testGetWithNoInstanceV1() {
        m_agent.setBehaviorToV1();
        
        GetPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst1Base, 1);
        get.addVarBind(zeroInst2Base, 0);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.NO_SUCH_NAME_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }

    public void testGetWithInvalidOidV2() {
        m_agent.setBehaviorToV2();
        
        GetPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(invalid, 0);
        get.addVarBind(zeroInst2Base, 0);
        
        validateGetResponse(get, m_agent.send(get));
        
    }
    
    public void testGetWithNoInstanceV2() {
        m_agent.setBehaviorToV2();
        
        GetPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst1Base, 1);
        get.addVarBind(zeroInst2Base, 0);
        
        validateGetResponse(get, m_agent.send(get));
    }

    public void testGetWithGenErrV1() {
        m_agent.setBehaviorToV1();
        
        //m_agent.setAgentValue(SnmpObjId.get(zeroInst1Base, "1"), new RuntimeException());
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));

        GetPdu get = TestPdu.getGet();
        
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst1Base, 1);
        get.addVarBind(zeroInst2Base, 0);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }
    
    public void testGetNextWithGenErrV1() {
        m_agent.setBehaviorToV1();
        
        //m_agent.setAgentValue(SnmpObjId.get(zeroInst1Base, "1"), new RuntimeException());
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));

        NextPdu get = TestPdu.getNext();
        
        get.addVarBind(zeroInst1Base);
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }

    public void testBulkWithGenErr() {
        m_agent.setBehaviorToV2();
        
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));

        BulkPdu get = TestPdu.getBulk();
        
        get.addVarBind(zeroInst1Base);
        get.setNonRepeaters(0);
        get.setMaxRepititions(5);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(1, resp.getErrorIndex());
    }
    
    

    private void validateGetResponse(GetPdu get, ResponsePdu resp) {
        assertNotNull(resp);
        assertEquals(ResponsePdu.NO_ERR, resp.getErrorStatus());
        // determine if errors are expected
        
        
        assertEquals(get.size(), resp.size());
        for(int i = 0; i < resp.size(); i++) {
            assertEquals(get.getVarBindAt(i).getObjId(), resp.getVarBindAt(i).getObjId());
            verifyObjIdValue(resp.getVarBindAt(i));
        }
    }
    
    public void testNext() {
        NextPdu pdu = TestPdu.getNext();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        
        validateNextResponse(pdu, m_agent.send(pdu));
    }

    public void testNextInvalidOidV1() {
        m_agent.setBehaviorToV1();
        
        NextPdu pdu = TestPdu.getNext();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(invalid);
        pdu.addVarBind(col1Base);
        
        ResponsePdu resp = m_agent.send(pdu);
        
        assertEquals(ResponsePdu.NO_SUCH_NAME_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }

    public void testNextInvalidOidV2() {
        m_agent.setBehaviorToV2();
        
        NextPdu pdu = TestPdu.getNext();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(invalid);
        pdu.addVarBind(col1Base);
        
        validateNextResponse(pdu, m_agent.send(pdu));
    }

    public void testNextWithGenErrV1() {
        m_agent.setBehaviorToV1();
        
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));


        NextPdu get = TestPdu.getNext();
        
        get.addVarBind(zeroInst1Base);
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }

    public void testNextWithGenErrV2() {
        m_agent.setBehaviorToV2();
        
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));


        NextPdu get = TestPdu.getNext();
        
        get.addVarBind(zeroInst1Base);
        get.addVarBind(zeroInst1Base, 0);
        get.addVarBind(zeroInst2Base);
        
        ResponsePdu resp = m_agent.send(get);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }
   private void validateNextResponse(NextPdu pdu, ResponsePdu resp) {
        assertNotNull(resp);
        assertEquals(ResponsePdu.NO_ERR, resp.getErrorStatus());
        assertEquals(pdu.size(), resp.size());
        for(int i = 0; i < resp.size(); i++) {
            verifyNextVarBind(pdu.getVarBindAt(i).getObjId(), resp.getVarBindAt(i));
        }
    }

    private SnmpObjId verifyNextVarBind(SnmpObjId reqObjId, TestVarBind respVarBind) {
        try {
            SnmpObjId nextOid = m_agent.getFollowingObjId(reqObjId);
            assertEquals(nextOid, respVarBind.getObjId());
            verifyObjIdValue(respVarBind);
            return nextOid;
        } catch (AgentEndOfMibException e) {
            assertEquals(reqObjId, respVarBind.getObjId());
            assertEquals(MockSnmpValue.END_OF_MIB, respVarBind.getValue());
            return reqObjId;
        }
    }

    public void testBulk() {
        m_agent.setBehaviorToV2();

        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        validateBulkResponse(pdu, m_agent.send(pdu));
    }
    
    public void testBulkInV1() {
        try {
            m_agent.setBehaviorToV1();
            BulkPdu pdu = TestPdu.getBulk();
            pdu.addVarBind(zeroInst1Base);
            pdu.addVarBind(zeroInst2Base);
            pdu.addVarBind(col1Base);
            pdu.addVarBind(col2Base);
            pdu.addVarBind(col3Base);
            pdu.setNonRepeaters(2);
            pdu.setMaxRepititions(3);
            m_agent.send(pdu);
            fail("Cannot send Bulk Pdus to V1 agent");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void testBulkTooBig() {
        m_agent.setBehaviorToV2();
        m_agent.setMaxResponseSize(4);
        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        validateBulkResponse(pdu, m_agent.send(pdu));
    }
    
    public void testBulkInvalidOidInNonRepeaterV2() {
        m_agent.setBehaviorToV2();
        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(invalid);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        validateBulkResponse(pdu, m_agent.send(pdu));
    }

    public void testBulkInvalidOidInRepeaterV2() {
        m_agent.setBehaviorToV2();
        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(invalid);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        validateBulkResponse(pdu, m_agent.send(pdu));
    }
    
    public void testBulkWithGenErrInNonRepeater() {
        m_agent.setBehaviorToV2();
        
        m_agent.introduceGenErr(SnmpObjId.get(zeroInst1Base, "1"));

        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst1Base, 0);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(3);
        pdu.setMaxRepititions(3);
        
        ResponsePdu resp = m_agent.send(pdu);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(2, resp.getErrorIndex());
    }

    public void testBulkWithGenErrInRepeater() {
        m_agent.setBehaviorToV2();
        
        m_agent.introduceGenErr(SnmpObjId.get(col2Base, "2"));


        BulkPdu pdu = TestPdu.getBulk();
        pdu.addVarBind(zeroInst1Base);
        pdu.addVarBind(zeroInst2Base);
        pdu.addVarBind(col1Base);
        pdu.addVarBind(col2Base);
        pdu.addVarBind(col3Base);
        pdu.setNonRepeaters(2);
        pdu.setMaxRepititions(3);
        
        ResponsePdu resp = m_agent.send(pdu);
        
        assertEquals(ResponsePdu.GEN_ERR, resp.getErrorStatus());
        assertEquals(4, resp.getErrorIndex());
    }


    private void validateBulkResponse(BulkPdu pdu, ResponsePdu resp) {
        assertNotNull(resp);
        assertEquals(ResponsePdu.NO_ERR, resp.getErrorStatus());
        
        int nonRepeaters = pdu.getNonRepeaters();
        int repeaters = (pdu.size() - nonRepeaters);
        
        // validate the length
        int expectedSize = Math.min(nonRepeaters+(repeaters*pdu.getMaxRepititions()), m_agent.getMaxResponseSize());
        assertEquals(expectedSize, resp.size());
        
        // validate the nonRepeaters
        for(int i = 0; i < nonRepeaters; i++) {
            verifyBulkVarBind(pdu.getVarBindAt(i).getObjId(), resp, i);
        }
        
        // validate the repeaters
        for(int i = 0; i < repeaters; i++) {
            SnmpObjId oid = pdu.getVarBindAt(i+nonRepeaters).getObjId();
            for(int count = 0; count < pdu.getMaxRepititions(); count++) {
                oid = verifyBulkVarBind(oid, resp, nonRepeaters+(count*repeaters)+i);
            } 
        }
    }

    private SnmpObjId verifyBulkVarBind(SnmpObjId oid, ResponsePdu resp, int index) {
        if (index < resp.size()) {
            return verifyNextVarBind(oid, resp.getVarBindAt(index));
        } else {
            return oid;
        }
    }
    
    public Object getAgentValueFor(SnmpObjId objId) {
        try {
            return m_agent.getValueFor(objId);
        } catch (AgentNoSuchInstanceException e) {
            return MockSnmpValue.NO_SUCH_INSTANCE;
        } catch (AgentNoSuchObjectException e) {
            return MockSnmpValue.NO_SUCH_OBJECT;
        }
    }

    private void verifyObjIdValue(TestVarBind varbind) {
        assertEquals(getAgentValueFor(varbind.getObjId()), varbind.getValue());
        
    }

}
