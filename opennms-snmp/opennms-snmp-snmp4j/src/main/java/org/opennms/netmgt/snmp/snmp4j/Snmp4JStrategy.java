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
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.PduHandle;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4JStrategy implements SnmpStrategy {

    public static Map m_registrations = new HashMap();
    private Snmp4JValueFactory m_valueFactory;
    
    static private boolean m_initialized = false;
    private static Snmp sm_session;

    //Initialize for v3 communications
    private static void initialize() {
        if (!m_initialized) {
//            LogFactory.setLogFactory(new Log4jLogFactory());
            
            MPv3.setEnterpriseID(5813);
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            m_initialized = true;
        }
    }
    
    public Snmp4JStrategy() {
        Snmp4JStrategy.initialize();
    }
    
    /**
     * SNMP4J createWalker implemenetation.
     * 
     * @param snmpAgentConfig
     * @param name
     * @param tracker
     */
    public SnmpWalker createWalker(SnmpAgentConfig snmpAgentConfig, String name, CollectionTracker tracker) {
        return new Snmp4JWalker(new Snmp4JAgentConfig(snmpAgentConfig), name, tracker);
    }
    
    /**
     * Not yet implemented.  Use a walker.
     */
    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oid) {
        return null;
    }

    /**
     * SNMP4J get helper that takes a single SnmpObjId
     * and calls get with an array.lenght =1 and returns
     * the first element of the returned array of SnmpValue.
     * 
     * @param agentConfig
     * @param oid
     *
     */
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oid+" for Agent:"+agentConfig);
        
        SnmpObjId[] oids = {oid};
        SnmpValue[] values = get(agentConfig, oids);
        
        return values[0];
    }
    
    /**
     * SnmpGet implementation.
     * 
     * @param agentConfig
     * @param oids
     * @return
     *        Returns an array of Snmp4JValues.  If the
     *        get was unsuccessful, then the first elment
     *        of the array will be null and lenth of 1. 
     */
    public SnmpValue[] get(SnmpAgentConfig snmpAgentConfig, SnmpObjId[] oids) {
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oids+" for Agent:"+agentConfig);
        
        return send(agentConfig, PDU.GET, oids);
    }
    
    /**
     * SNMP4J getNext implementation
     * 
     * @param agentConfig
     * @param oid
     * 
     */
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled())
            log().debug("getNext: OID: "+oid+" for Agent:"+agentConfig);
        
        SnmpObjId[] oids = { oid };
        SnmpValue[] values = getNext(agentConfig, oids);
        return values[0];
    }
    
    /**
     * SNMP GetNext implementation.
     * 
     * @param agentConfig
     * @param oids
     * @return
     *        Returns an array of Snmp4JValues.  If the
     *        getNext was unsuccessful, then the first elment
     *        of the array will be null and lenth of 1. 
     */
    public SnmpValue[] getNext(SnmpAgentConfig snmpAgentConfig, SnmpObjId[] oids) {
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oids+" for Agent:"+agentConfig);
        
        return send(agentConfig, PDU.GETNEXT, oids);
    }

    
    /**
     * Sends and SNMP4J request pdu.  The attributes in SnmpAgentConfig should have been
     * adapted from default SnmpAgentConfig values to those compatible with the SNMP4J library.
     * 
     * @param agentConfig
     * @param pduType TODO
     * @param oids
     * @return
     */
    private SnmpValue[] send(Snmp4JAgentConfig agentConfig, int pduType, SnmpObjId[] oids) {
        
        SnmpValue[] values = { null };
        Snmp session = null;
        
        try {
            session = SnmpHelpers.createSnmpSession(agentConfig);
            session = new Snmp(new DefaultUdpTransportMapping());
            session.listen();
            
            session.getUSM().addUser((agentConfig.getSecurityName()),
                    new UsmUser(agentConfig.getSecurityName(),
                            agentConfig.getAuthProtocol(),
                            agentConfig.getAuthPassPhrase(),
                            agentConfig.getPrivProtocol(),
                            agentConfig.getPrivPassPhrase()));
            
            Target target = agentConfig.getTarget();
            PDU pdu = SnmpHelpers.createPDU(agentConfig.getVersion());
            pdu.setType(pduType);
            
            //TODO:log this
            if (!buildPdu(pdu, oids))
                return null;
            
            ResponseEvent responseEvent = session.send(pdu, target);
            
            if (responseEvent.getResponse() == null) {
                log().warn("send: Timeout.  Agent: "+agentConfig);
            } else if (responseEvent.getResponse().get(0).getSyntax() == SMIConstants.SYNTAX_NULL) {
                values[0] = null;
            } else if (responseEvent.getError() != null) {
                log().warn("send: Error during get operation.  Error: "+responseEvent.getError().getLocalizedMessage());
            } else if (responseEvent.getResponse().getType() == PDU.REPORT) {
                log().warn("send: Error during get operation.  Report returned with varbinds: "+responseEvent.getResponse().getVariableBindings());
            } else if (responseEvent.getResponse().getVariableBindings().size() < 1) {
                log().warn("send: Received PDU with 0 varbinds.");
            } else {
                
                values = new Snmp4JValue[responseEvent.getResponse().getVariableBindings().size()];
                
                for (int i=0; i<values.length; i++) {
                    values[i] = new Snmp4JValue(responseEvent.getResponse().get(0).getVariable());
                }
                
                if (log().isDebugEnabled())
                    log().debug("send: Snmp operation successful. Value: "+values);
            }
            
        } catch (IOException e) {
            log().error("getNext: Could not create Snmp session for Agent: "+agentConfig+". "+e);
        } finally {
            try {
                session.close();
            } catch (IOException e) {
                log().error("send: Error closinging SNMP connection: "+e);
            }
        }
        return values;
        
    }

    private boolean buildPdu(PDU pdu, SnmpObjId[] oids) {
        
        VariableBinding vb;
        
        for (int i=0; i<oids.length; i++) {
            vb = new VariableBinding(new OID(oids[i].toString()));
            pdu.add(vb);
        }
        
        if (pdu.getVariableBindings().size() == oids.length) {
            return true;
        } else {
            return false;
        }
        
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public SnmpValueFactory getValueFactory() {
        if (m_valueFactory == null)
            m_valueFactory = new Snmp4JValueFactory();
        return m_valueFactory;
    }

    public static class RegistrationInfo {
        public TrapNotificationListener m_listener;
        int m_trapPort;
        
        Snmp m_trapSession;
        Snmp4JTrapNotifier m_trapHandler;
        private TransportMapping m_transportMapping;
        
        RegistrationInfo(TrapNotificationListener listener, int trapPort) {
            if (listener == null) throw new NullPointerException("listener is null");
    
            m_listener = listener;
            m_trapPort = trapPort;
        }
    
        public boolean equals(Object obj) {
            if (obj instanceof RegistrationInfo) {
                RegistrationInfo info = (RegistrationInfo) obj;
                return (m_listener == info.m_listener) && (m_trapPort == info.m_trapPort);
            }
            return false;
        }
    
        public int hashCode() {
            return (m_listener.hashCode() ^ m_trapPort);
        }
        
        public void setSession(Snmp trapSession) {
            m_trapSession = trapSession;
        }
        
        public Snmp getSession() {
            return m_trapSession;
        }
        
        public void setHandler(Snmp4JTrapNotifier trapHandler) {
            m_trapHandler = trapHandler;
        }
        
        public Snmp4JTrapNotifier getHandler() {
            return m_trapHandler;
        }

        public int getPort() {
            return m_trapPort;
        }

        public void setTransportMapping(TransportMapping transport) {
            m_transportMapping = transport;
        }
        
        public TransportMapping getTransportMapping() {
            return m_transportMapping;
        }
        
        
    }



    public void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws IOException {
        RegistrationInfo info = new RegistrationInfo(listener, snmpTrapPort);
        
        Snmp4JTrapNotifier m_trapHandler = new Snmp4JTrapNotifier(listener, processorFactory);
        info.setHandler(m_trapHandler);

        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress(snmpTrapPort));
        info.setTransportMapping(transport);
        Snmp snmp = new Snmp(transport);
        snmp.addCommandResponder(m_trapHandler);
        info.setSession(snmp);
        
        m_registrations.put(listener, info);
        
        snmp.listen();
    }

    public void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException {
        RegistrationInfo info = (RegistrationInfo)m_registrations.remove(listener);
        info.getSession().close();
    }

    public SnmpV1TrapBuilder getV1TrapBuilder() {
        return new Snmp4JV1TrapBuilder();
    }

    public SnmpTrapBuilder getV2TrapBuilder() {
        return new Snmp4JV2TrapBuilder();
    }
    
    public static void send(String agentAddress, int port, String community, PDU pdu) throws Exception {
        
        Snmp snmp = getSession();
        
        Address targetAddress = GenericAddress.parse("udp:"+agentAddress+"/"+port);
        CommunityTarget target = new CommunityTarget(targetAddress, new OctetString(community.getBytes()));
        target.setVersion(pdu instanceof PDUv1 ? SnmpConstants.version1 : SnmpConstants.version2c);
        
        snmp.send(pdu, target);
    }

    private static Snmp getSession() throws IOException {
        if (sm_session == null) {
            sm_session = new Snmp(new DefaultUdpTransportMapping());
        }
        return sm_session;
    }

    public static void sendTest(String agentAddress, int port, String community, PDU pdu) {
        for (Iterator it = m_registrations.values().iterator(); it.hasNext();) {
            RegistrationInfo info = (RegistrationInfo) it.next();
            if (port == info.getPort()) {
                Snmp snmp = info.getSession();
                MessageDispatcher dispatcher = snmp.getMessageDispatcher();
                TransportMapping transport = info.getTransportMapping();
                
                int securityModel = (pdu instanceof PDUv1 ? SecurityModel.SECURITY_MODEL_SNMPv1 :SecurityModel.SECURITY_MODEL_SNMPv2c);
                int messageModel = (pdu instanceof PDUv1 ? MessageProcessingModel.MPv1 : MessageProcessingModel.MPv2c);
                CommandResponderEvent e = new CommandResponderEvent(dispatcher, transport, new IpAddress(agentAddress), messageModel, 
                                                                    securityModel, community.getBytes(), 
                                                                    SecurityLevel.NOAUTH_NOPRIV, new PduHandle(), pdu, 1000, null);

                info.getHandler().processPdu(e);
            }
        }

    }

}
