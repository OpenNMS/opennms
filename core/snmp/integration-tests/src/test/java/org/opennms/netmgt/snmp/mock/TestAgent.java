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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class TestAgent {
    
    private static class Redirect {
        SnmpObjId m_targetObjId;
        public Redirect(SnmpObjId targetObjId) {
            m_targetObjId = targetObjId;
        }
        public SnmpObjId getTargetObjId() {
            return m_targetObjId;
        }

    }
    
    private SortedMap<SnmpObjId, Object> m_agentData = new TreeMap<SnmpObjId, Object>();
    private boolean isV1 = true;

    private int m_maxResponseSize = 100; // this is kind of close to reality
 
    public SnmpValue getValueFor(SnmpObjId id) {
        Object result = m_agentData.get(id);
        if (result == null) {
            generateException(id);
        } else if (result instanceof RuntimeException) {
            throw (RuntimeException) result;
        } 
        return (SnmpValue) result;
        
    }

    private void generateException(SnmpObjId id) {
        if (m_agentData.isEmpty()) {
            throw new AgentNoSuchObjectException();
        }
        
        SnmpObjId firstOid = m_agentData.firstKey();
        SnmpObjId lastOid = m_agentData.lastKey();
        if (id.compareTo(firstOid) < 0 || id.compareTo(lastOid) > 0)
            throw new AgentNoSuchObjectException();
        throw new AgentNoSuchInstanceException();
    }

    public void setAgentData(Properties mibData) {
    	MockSnmpValueFactory factory = new MockSnmpValueFactory();
        m_agentData = new TreeMap<SnmpObjId, Object>();
        for (Entry<Object, Object> entry : mibData.entrySet()) {
            SnmpObjId objId = SnmpObjId.get(entry.getKey().toString());
            
            setAgentValue(objId, factory.parseMibValue(entry.getValue().toString()));
        }
    }

    public SnmpObjId getFollowingObjId(SnmpObjId id) {
        try {
            SnmpObjId nextObjId = m_agentData.tailMap(SnmpObjId.get(id, SnmpInstId.INST_ZERO)).firstKey();
            Object value = m_agentData.get(nextObjId);
            if (value instanceof Redirect) {
                Redirect redirect = (Redirect) value;
                return redirect.getTargetObjId();
            }
            return nextObjId;
        } catch (NoSuchElementException e) {
            throw new AgentEndOfMibException();   
        }
    }

    public void loadSnmpTestData(Class<?> clazz, String name) throws IOException {
        InputStream dataStream = clazz.getResourceAsStream(name);
        Properties mibData = new Properties();
        mibData.load(dataStream);
        dataStream.close();
    
        setAgentData(mibData);
    }
    
    public void setAgentValue(SnmpObjId objId, SnmpValue value) {
        m_agentData.put(objId, value);
    }
    
    /**
     * This simulates send a packet and waiting for a response 
     * @param pdu
     * @return
     */
    public ResponsePdu send(RequestPdu pdu) {
        return pdu.send(this);
    }

    public void setBehaviorToV1() {
        isV1 = true;
    }

    public void setBehaviorToV2() {
        isV1 = false;
    }

    SnmpValue handleNoSuchObject(SnmpObjId reqObjId, int errIndex) {
        if (isVersion1()) {
            throw new AgentNoSuchNameException(errIndex);
        }
            
        return MockSnmpValue.NO_SUCH_OBJECT;
    }
    
    SnmpValue handleNoSuchInstance(SnmpObjId reqObjId, int errIndex) {
        if (isVersion1()) {
            throw new AgentNoSuchNameException(errIndex);
        }
        
        return MockSnmpValue.NO_SUCH_INSTANCE;
    }

    SnmpValue getVarBindValue(SnmpObjId objId, int errIndex) {
        try {
            return getValueFor(objId); 
        } catch (AgentNoSuchInstanceException e) {
            return handleNoSuchInstance(objId, errIndex);
        } catch (AgentNoSuchObjectException e) {
            return handleNoSuchObject(objId, errIndex);
        } catch (Exception e) {
            throw new AgentGenErrException(errIndex);
        }
    }

    TestVarBind getNextResponseVarBind(SnmpObjId lastOid, int errIndex) {
        try {
            SnmpObjId objId = getFollowingObjId(lastOid);
            SnmpValue value = getVarBindValue(objId, errIndex);
            return new TestVarBind(objId, value);
        } catch (AgentEndOfMibException e) {
            return handleEndOfMib(lastOid, errIndex);
        }
    }

    private TestVarBind handleEndOfMib(SnmpObjId lastOid, int errIndex) {
        if (isVersion1()) {
            throw new AgentNoSuchNameException(errIndex);
        }
        
        return new TestVarBind(lastOid, MockSnmpValue.END_OF_MIB);
    }

    TestVarBind getResponseVarBind(SnmpObjId objId, int errIndex) {
        SnmpValue value = getVarBindValue(objId, errIndex);
        return new TestVarBind(objId, value);
    }

    public boolean isVersion1() {
        return isV1;
    }

    public void setMaxResponseSize(int maxResponseSize) {
        m_maxResponseSize = maxResponseSize;
    }
    
    public int getMaxResponseSize() {
        return m_maxResponseSize;
    }

    public void introduceSequenceError(SnmpObjId objId, SnmpObjId followingObjId) {
        Redirect redirect = new Redirect(followingObjId);
        m_agentData.put(SnmpObjId.get(objId, "0"), redirect);
    }

    public RuntimeException introduceGenErr(SnmpObjId objId) {
        RuntimeException exception = new RuntimeException("Error occurred retrieving "+objId);
        m_agentData.put(objId, exception);
        return exception;
    }
}
