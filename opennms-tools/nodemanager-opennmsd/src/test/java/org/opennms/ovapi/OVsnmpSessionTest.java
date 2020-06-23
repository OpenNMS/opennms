/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.ovapi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.nnm.SnmpObjId;
import org.opennms.nnm.swig.NNM;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.nnm.swig.OVsnmpVarBind;
import org.opennms.nnm.swig.SnmpCallback;
import org.opennms.nnm.swig.fd_set;
import org.opennms.nnm.swig.timeval;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class OVsnmpSessionTest extends TestCase {
    
    MockSnmpAgent m_agent;
    String m_host;
    
    public void setUp() throws Exception {
        Resource snmpData = new ClassPathResource("snmpTestData1.properties");
    
        m_host = InetAddress.getLocalHost().getHostAddress();
        
        m_agent = MockSnmpAgent.createAgentAndRun(snmpData, m_host+"/9161");
    }
    
    protected void tearDown() throws Exception {
        Thread.sleep(1000);
        m_agent.shutDownAndWait();
        
    }

    public void testOpenClose() throws Exception {

        OVsnmpSession sess = open("localhost", 9161);
        assertNotNull(sess);
        close(sess);
    }
    
    OVsnmpSession open(String peername, int remotePort) {
        return OVsnmpSession.open(peername, remotePort, null);
    }
    
    void close(OVsnmpSession session) {
        session.close();
    }
    
    public void testCreatePdu() throws Exception {
        SnmpObjId sysName = SnmpObjId.get(".1.3.6.1.2.1.1.5.0");
        
        
        OVsnmpPdu request = OVsnmpPdu.create(NNM.GET_REQ_MSG);
        
        assertNull(request.getVarBinds());
        
        request.addNullVarBind(sysName.getIds());
        
        OVsnmpVarBind varBind = request.getVarBinds();
        assertNotNull(varBind);
        
        assertEquals(sysName.toString(), varBind.getObjectId());
        
        assertNull(varBind.getNextVarBind());
        
        request.free();
    }
    
    public void testBlockingSend() {
        SnmpObjId sysName = SnmpObjId.get(".1.3.6.1.2.1.1.5.0");
        
        
        OVsnmpPdu request = OVsnmpPdu.create(NNM.GET_REQ_MSG);
        request.addNullVarBind(sysName.getIds());
        
        OVsnmpSession session = open(m_host, 9161);
        
                
        OVsnmpPdu reply = session.blockingSend(request);
        assertNotNull(reply);
        
        OVsnmpVarBind varbind = reply.getVarBinds();
        
        assertNotNull(varbind);
        assertNull(varbind.getNextVarBind());
        
        assertEquals(NNM.ASN_OCTET_STR, varbind.getType());
        
        byte[] octets = new byte[varbind.getValLength()];
        
        assertTrue(varbind.getValue().getOctetString(octets));
        
        assertEquals("brozow.local", new String(octets));
        
        reply.free();
        
        close(session);
        
    }
    
    
    private static class Walker extends SnmpCallback {
        
        boolean m_finished = false;
        String m_peername;
        int m_port;
        SnmpObjId m_base;
        
        OVsnmpSession m_session;
        
        public Walker(String peername, int port, SnmpObjId base) {
            m_peername = peername;
            m_port = port;
            m_base = base;
        }
        
        public void start() {
            m_session = OVsnmpSession.open(m_peername, m_port, this);
            m_finished = false;
            sendNext(m_base);
        }
        
        public void callback(int reason, OVsnmpSession session, OVsnmpPdu reply) {
            try {
                if (reason == NNM.SNMP_ERR_NO_RESPONSE) {
                    System.err.println("NO_RESPONSE");
                    timedOut();
                    return;
                }

                SnmpObjId recvdOid = processVarBinds(reply.getVarBinds());

                if (m_base.isPrefixOf(recvdOid)) {
                    sendNext(recvdOid);
                } else {
                    finished();
                }
            
            } finally {
                if (reply != null) {
                    reply.free();
                }
            }
            
        }
        
        private SnmpObjId processVarBinds(OVsnmpVarBind varBind) {
            SnmpObjId oid =  SnmpObjId.get(varBind.getObjectId());

            System.err.println("Received: "+oid+ " type: "+Integer.toHexString(varBind.getType())+" "+getValue(varBind));
            
            return oid;
        }
        
        private String getValue(OVsnmpVarBind varBind) {
            int type = varBind.getType();
            if (type == NNM.ASN_BOOLEAN) {
                return (varBind.getValue().getIntValue() == 0 ? "false" : "true");
            } else if (type == NNM.ASN_INTEGER) {
                return Integer.toString(varBind.getValue().getIntValue());
            } else if (type == NNM.ASN_OCTET_STR) {
                byte[] bytes = new byte[varBind.getValLength()];
                varBind.getValue().getOctetString(bytes);
                return new String(bytes);
            } else if (type == NNM.ASN_U_INTEGER) {
                return ""+varBind.getValue().getUnsigned32Value();
            } else if (type == NNM.ASN_OBJECT_ID) {
                return varBind.getValue().getObjectId(varBind.getValLength());
            } else if (type == NNM.ASN_TIMETICKS) {
                int centis = varBind.getValue().getIntValue();
                return ""+centis/100+"."+centis%100+" s";
            } else if (type == NNM.ASN_COUNTER32) {
                return ""+varBind.getValue().getUnsigned32Value();
            } else if (type == NNM.ASN_COUNTER64) {
                return ""+varBind.getValue().getCounter64Value();
            } else if (type == NNM.ASN_GAUGE) {
                return ""+varBind.getValue().getUnsigned32Value();
            } else if (type == NNM.ASN_IPADDRESS) {
                byte[] bytes = new byte[4];
                varBind.getValue().getOctetString(bytes);
                try {
                    return InetAddress.getByAddress(bytes).getHostAddress();
                } catch (UnknownHostException e) {
                    return "UnknownHost that can't happen";
                }
            } else {
                return "UNKNOWN TYPE: "+type;
            }
/*
            
#define ASN_BOOLEAN         (0x01)
#define ASN_INTEGER         (0x02)
#define ASN_BIT_STR         (0x03)
#define ASN_U_INTEGER       (0x07) 
#define ASN_OCTET_STR       (0x04)
#define ASN_NULL            (0x05)
#define ASN_OBJECT_ID       (0x06)
#define ASN_SEQUENCE        (0x10)
#define ASN_SET             (0x11)


#define ASN_IPADDRESS       (ASN_APPLICATION | 0)
#define ASN_COUNTER         (ASN_APPLICATION | 1)
#define ASN_GAUGE           (ASN_APPLICATION | 2)
#define ASN_TIMETICKS       (ASN_APPLICATION | 3)
#define ASN_OPAQUE          (ASN_APPLICATION | 4)

#define ASN_COUNTER64       (ASN_APPLICATION | 6)

#define ASN_UNSIGNED32      ASN_GAUGE
#define ASN_GAUGE32         ASN_GAUGE
#define ASN_COUNTER32       ASN_COUNTER

#define ASN_NOSUCHOBJECT    (ASN_CONTEXT | ASN_PRIMITIVE | 0x0)
#define ASN_NOSUCHINSTANCE  (ASN_CONTEXT | ASN_PRIMITIVE | 0x1)
#define ASN_ENDOFMIBVIEW    (ASN_CONTEXT | ASN_PRIMITIVE | 0x2)

 */
        }

        private void timedOut() {
            m_finished = true;
            System.err.println("Timed Out");
            close();
            
        }
        
        private void finished() {
            m_finished = true;
            System.err.println("Finished");
            close();
        }
        
        private void sendNext(SnmpObjId oid) {
            
            OVsnmpPdu next = OVsnmpPdu.create(NNM.GETNEXT_REQ_MSG);
            
            next.addNullVarBind(oid.getIds());
            
            m_session.send(next);
        }
        
        private void close() {
            m_session.close();
        }

        public boolean isFinished() {
            return m_finished;
        }
        
    }
    
    public void testAsynchronousCallbacks() throws Exception {
        
        Thread.sleep(20000);

        Walker system = new Walker(m_host, 9161, SnmpObjId.get(".1.3.6.1.2.1.1"));
        Walker ifTable = new Walker(m_host, 9161, SnmpObjId.get(".1.3.6.1.2.1.2"));
        Walker ipAddrTable = new Walker(m_host, 9161, SnmpObjId.get(".1.3.6.1.2.1.4.20.1"));
        
        List walkers = new LinkedList();
        walkers.add(system);
        walkers.add(ifTable);
        walkers.add(ipAddrTable);
        
        fd_set fdset = new fd_set();
        timeval timeout = new timeval();
        
        for(Iterator it = walkers.iterator(); it.hasNext(); ) {
            Walker walker = (Walker)it.next();
            walker.start();
            System.err.println("New Walker");
            while(!walker.isFinished()) {
        
                int maxFDs = OVsnmpSession.getRetryInfo(fdset, timeout);

                int count = NNM.select(maxFDs, fdset, null, null, timeout);

                if (count > 0) {
                    OVsnmpSession.read(fdset);
                }
        
                OVsnmpSession.doRetry();
            }
        }
        

    }
    


}
