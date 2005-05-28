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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.opennms.netmgt.collectd.SnmpObjId;
import org.opennms.netmgt.mock.MockUtil;


public class TestAgent {

    public TreeMap m_agentData;

    public Object parseMibValue(String mibVal) {
        return mibVal;
    }

    public Object getValueFor(SnmpObjId id) {
        return m_agentData.get(id);
    }

    public void setAgentData(Properties mibData) {
        m_agentData = new TreeMap();
        for (Iterator it = mibData.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            SnmpObjId objId = SnmpObjId.get((String)entry.getKey());
            
            m_agentData.put(objId, parseMibValue((String)entry.getValue()));
        }
    }

    public SnmpObjId getFollowingObjId(SnmpObjId id) {
        MockUtil.println("Retrieving next id for "+id);
        return (SnmpObjId)m_agentData.tailMap(SnmpObjId.get(id, "0")).firstKey();
    }

    public void loadSnmpTestData(Class clazz, String name) throws IOException {
        InputStream dataStream = clazz.getResourceAsStream(name);
        Properties mibData = new Properties();
        mibData.load(dataStream);
        dataStream.close();
    
        setAgentData(mibData);
    }
    
    protected TestVarBindList getNext(TestVarBindList request) {
        TestVarBindList response = new TestVarBindList(request.size());
        for (Iterator it = request.iterator(); it.hasNext();) {
            TestVarBind req = (TestVarBind) it.next();
            SnmpObjId respOid = getFollowingObjId(req.getObjId());
            response.addVarBind(respOid, getValueFor(respOid));
        }
        return response;
    }
    
    /**
     * This simulates send a packet and waiting for a response 
     * @param pdu
     * @return
     */
    public TestPdu send(TestPdu pdu) {
        switch(pdu.getType()) {
        case TestPdu.GET_REQ:
            return sendGet(pdu);
        case TestPdu.NEXT_REQ:
            return sendNext(pdu);
        case TestPdu.BULK_REQ:
            return sendBulk(pdu);
        default:
            return sendError(pdu);
        }
        
    }

    private TestPdu sendError(TestPdu pdu) {
        // TODO Auto-generated method stub
        return null;
    }

    private TestPdu sendBulk(TestPdu pdu) {
        TestPdu resp = TestPdu.getResponse();

        // first do non repeaters
        int nonRepeaters = Math.min(pdu.size(), pdu.getNonRepeaters());
        for(int i = 0; i < nonRepeaters; i++) {
            TestVarBind varBind = (TestVarBind) pdu.getVarBindAt(i);
            SnmpObjId objId = getFollowingObjId(varBind.getObjId());
            resp.addVarBind(objId, getValueFor(objId));
        }
        
        // now do the repeaters
        
        // make a list to track the repititions
        int repeaters = pdu.size() - nonRepeaters;
        List repeaterList = new ArrayList(repeaters);
        for(int i = nonRepeaters; i < pdu.size(); i++) {
            repeaterList.add(pdu.getVarBindAt(i).getObjId());
        }
        
        for(int count = 0; count < pdu.getMaxRepititions(); count++) {
            for(int i = 0; i < repeaterList.size(); i++) {
                SnmpObjId lastOid = (SnmpObjId)repeaterList.get(i);
                SnmpObjId objId = getFollowingObjId(lastOid);
                resp.addVarBind(objId, getValueFor(objId));
                repeaterList.set(i, objId);
            }
        }
        
        
        return resp;
    }

    private TestPdu sendNext(TestPdu pdu) {
        TestPdu resp = TestPdu.getResponse();
        
        for (Iterator it = pdu.getVarBinds().iterator(); it.hasNext();) {
            TestVarBind varBind = (TestVarBind) it.next();
            SnmpObjId objId = getFollowingObjId(varBind.getObjId());
            resp.addVarBind(objId, getValueFor(objId));
        }
        
        return resp;
    }

    private TestPdu sendGet(TestPdu pdu) {
        TestPdu resp = TestPdu.getResponse();
        
        for (Iterator it = pdu.getVarBinds().iterator(); it.hasNext();) {
            TestVarBind varBind = (TestVarBind) it.next();
            SnmpObjId objId = varBind.getObjId();
            resp.addVarBind(objId, getValueFor(objId));
        }
        
        return resp;
    }

    public void setBehaviorToV1() {
        // TODO Auto-generated method stub
        
    }
    



}
