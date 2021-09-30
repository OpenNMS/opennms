/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.mock;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.SnmpObjId;

abstract public class RequestPdu extends TestPdu {

    protected int getNonRepeaters() {
        return size();
    }

    protected int getMaxRepititions() {
        return 0;
    }

    /*
     * This simulates send a packet and waiting for a response. 
     * 
     * This is a template method based on te getBulk algorithm. We use the getBulk
     * algorithm for get and nexts as well.  nonRepeaters for gets and nexts is always
     * equals to pdu size so there are no repeaters. maxRepitions is also always zero
     * for gets and nexts.
     * 
     * The method getRespObjIdFromReqObjId which by default goes 'next' is overridden
     * and does 'get' in the GetPdu.
     */
    public ResponsePdu send(TestAgent agent) {
        ResponsePdu resp = TestPdu.getResponse();

        try {
            // first do non repeaters
            int nonRepeaters = Math.min(size(), getNonRepeaters());
            for(int i = 0; i < nonRepeaters; i++) {
                int errIndex = i+1;
                TestVarBind varBind = (TestVarBind) getVarBindAt(i);
                SnmpObjId lastOid = varBind.getObjId();
                TestVarBind newVarBind = getResponseVarBind(agent, lastOid, errIndex);
                resp.addVarBind(newVarBind);
                
                // make sure we haven't exceeded response size
                validateResponseSize(resp, agent);
            }
            
            // make a list to track the repititions
            int repeaters = size() - nonRepeaters;
            List<SnmpObjId> repeaterList = new ArrayList<SnmpObjId>(repeaters);
            for(int i = nonRepeaters; i < size(); i++) {
                repeaterList.add(getVarBindAt(i).getObjId());
            }
            
            // now generate varbinds for the repeaters
            for(int count = 0; count < getMaxRepititions(); count++) {
                for(int i = 0; i < repeaterList.size(); i++) {
                    int errIndex = nonRepeaters+i+1;
                    SnmpObjId lastOid = (SnmpObjId)repeaterList.get(i);
                    TestVarBind newVarBind = getResponseVarBind(agent, lastOid, errIndex);
                    resp.addVarBind(newVarBind);
                    repeaterList.set(i, newVarBind.getObjId());
                    
                    // make sure we haven't exceeded response size
                    validateResponseSize(resp, agent);
                }
            }
            return resp;
        } catch (AgentIndexException e) {
            // this happens for GenErr and NoSuchName errs
            resp.setVarBinds(getVarBinds());
            resp.setErrorStatus(e.getErrorStatus());
            resp.setErrorIndex(e.getErrorIndex()); // errorIndex uses indices starting at 1
            return resp;
        } catch (AgentTooBigException e) {
            // when we exceed response size we'll get here
            return handleTooBig(agent, resp);
        }
    }

    protected ResponsePdu handleTooBig(TestAgent agent, ResponsePdu resp) {
        resp.setVarBinds(new TestVarBindList());
        resp.setErrorStatus(ErrorStatus.TOO_BIG.ordinal());
        resp.setErrorIndex(0); // errorIndex uses indices starting at 1
        return resp;
    }

    private void validateResponseSize(ResponsePdu resp, TestAgent agent) {
        if (resp.size() > agent.getMaxResponseSize())
            throw new AgentTooBigException();
    }

    protected TestVarBind getResponseVarBind(TestAgent agent, SnmpObjId lastOid, int errIndex) {
        return agent.getNextResponseVarBind(lastOid, errIndex);
    }

}