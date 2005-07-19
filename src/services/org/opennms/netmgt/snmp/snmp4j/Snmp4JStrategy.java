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
import java.net.InetAddress;
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
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.PduHandle;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
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
     * @param agentConfig
     * @param name
     * @param tracker
     */
    public SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return new Snmp4JWalker(agentConfig, name, tracker);
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
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GET);
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
    public SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oids+" for Agent:"+agentConfig);
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GET);
        return send(agentConfig, oids);
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
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GETNEXT);
        SnmpObjId[] oids = { oid };
        SnmpValue[] values = send(agentConfig, oids);
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
    public SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oids+" for Agent:"+agentConfig);
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GETNEXT);
        return send(agentConfig, oids);
    }

    
    /**
     * Sends and SNMP4J request pdu.  The attributes in SnmpAgentConfig should have been
     * adapted from default SnmpAgentConfig values to those compatible with the SNMP4J library.
     * 
     * @param agentConfig
     * @param oids
     * @return
     */
    private SnmpValue[] send(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        
        SnmpValue[] values = { null };
        Snmp session = null;
        
        try {
            session = SnmpHelpers.createSnmpSession(agentConfig);
            session = new Snmp(new DefaultUdpTransportMapping());
            session.listen();
            
            session.getUSM().addUser((createOctetString(agentConfig.getSecurityName())),
                    new UsmUser(createOctetString(agentConfig.getSecurityName()),
                            convertAuthProtocol(agentConfig.getAuthProtocol()),
                            createOctetString(agentConfig.getAuthPassPhrase()),
                            convertPrivProtocol(agentConfig.getPrivProtocol()),
                            createOctetString(agentConfig.getPrivPassPhrase())));
            
            Target target = getTarget(agentConfig);
            PDU pdu = SnmpHelpers.createPDU(agentConfig);
            
            //TODO:log this
            if (!buildPdu(pdu, oids))
                return null;
            
            ResponseEvent responseEvent = session.send(pdu, target);
            
            if (responseEvent.getResponse() == null) {
                log().warn("send: Timeout.  Agent: "+agentConfig);
            } else if (responseEvent.getResponse().get(0).getSyntax() == SMIConstants.SYNTAX_NULL) {
                values[0] = null;
            } else if (responseEvent.getError() != null) {
                log().warn("send: Error during getNext operation.  Error: "+responseEvent.getError().getLocalizedMessage());
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

    static OID convertAuthProtocol(String authProtocol) {
        
        //Returning null here is okay because the SNMP4J library supports
        //this value as null when creating the Snmp session.
        if (authProtocol == null)
            return null;
        
        if (authProtocol.equals("MD5")) {
            return AuthMD5.ID;
        } else if (authProtocol.equals("SHA")) {
            return AuthSHA.ID;
        } else {
            throw new IllegalArgumentException("Authentication protocol unsupported: " + authProtocol);
        }            
    }

    static OID convertPrivProtocol(String privProtocol) {

        //Returning null here is okay because the SNMP4J library supports
        //this value as null when creating the Snmp session.
        if (privProtocol == null)
            return null;
        
        if (privProtocol.equals("DES")) {
            return PrivDES.ID;
        } else if ((privProtocol.equals("AES128")) || (privProtocol.equals("AES"))) {
            return PrivAES128.ID;
        } else if (privProtocol.equals("AES192")) {
            return PrivAES192.ID;
        } else if (privProtocol.equals("AES256")) {
            return PrivAES256.ID;
        } else {
            throw new IllegalArgumentException("Privacy protocol " + privProtocol + " not supported");
        }

    }

    /**
     * Adapts the agent's values defined by SnmpAgentConfig's constants
     * to SNMP4J compatible constants.
     * @param agentConfig
     */
    public static void adaptConfig(SnmpAgentConfig agentConfig) {
        
        //fail-safe for those not checking prior to this call
        if(agentConfig.isAdapted())
            return;
        
        agentConfig.setPduType(convertPduType(agentConfig.getPduType()));
        agentConfig.setSecurityLevel(convertSecurityLevel(agentConfig.getSecurityLevel()));
        agentConfig.setVersion(convertVersion(agentConfig.getVersion()));
        agentConfig.setAdapted(true);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * Adapts the OpenNMS SNMPv3 community name to an SNMP4J compatible
     * community name (String -> OctetString)
     * 
     * @param agentConfig
     * @return
     */
    public static OctetString convertCommunity(String community) {
        return new OctetString(community);
    }

    /**
     * This method adapts the OpenNMS SNMP version constants
     * to SNMP4J defined constants.
     * 
     * @param version
     * @return
     */
    public static int convertVersion(int version) {

        switch (version) {
        case SnmpAgentConfig.VERSION3 :
            return SnmpConstants.version3;
        case SnmpAgentConfig.VERSION2C :
            return SnmpConstants.version2c;
        default :
            return SnmpConstants.version1;
        }
    }

    /**
     * Adapts the OpenNMS SNMPv3 security name to an SNMP4J compatible
     * security name (String -> OctetString)
     * 
     * @param securityName
     * @return
     */
    public static OctetString convertSecurityName(String securityName) {
        return new OctetString(securityName);
    }

    /**
     * This method adapts the OpenNMS SNMPv3 security level constants
     * to SNMP4J defined constants.
     * 
     * @param securityLevel
     * @return
     */
    public static int convertSecurityLevel(int securityLevel) {
        
    
        switch (securityLevel) {
        case SnmpAgentConfig.AUTH_NOPRIV :
            securityLevel = SecurityLevel.AUTH_NOPRIV;
            break;
        case SnmpAgentConfig.AUTH_PRIV :
            securityLevel = SecurityLevel.AUTH_PRIV;
            break;
        case SnmpAgentConfig.NOAUTH_NOPRIV :
            securityLevel = SecurityLevel.NOAUTH_NOPRIV;
            break;
        default :
           securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        }
        
        return securityLevel;
    }

    /**
     * This method converts an InetAddress to an implementation of an SNMP4J Address
     * (UdpAddress or TcpAddress)
     * 
     * TODO: This needs to be updated when the protocol flag is added to the SNMP Config
     * so that UDP or TCP can be used in v3 operations.
     */
    public static Address convertAddress(InetAddress address, int port) {
        String transportAddress = address.getHostAddress();
        transportAddress += "/" + port;
        Address targetAddress = new UdpAddress(transportAddress);
        return targetAddress;
    }
    
    /**
     * Converts OpenNMS PDU type constant to that value used in the SNMP4J library.
     * 
     * @param agentConfig
     * @return
     */
    public static int convertPduType(int pduType) {
        
        switch (pduType) {
        case SnmpAgentConfig.GET_PDU :
            return PDU.GET;
        case SnmpAgentConfig.GETNEXT_PDU :
            return PDU.GETNEXT;
        case SnmpAgentConfig.GETBULK_PDU :
            return PDU.GETBULK;
        case SnmpAgentConfig.SET_PDU :
            return PDU.SET;
        default :
            ThreadCategory.getInstance(Snmp4JStrategy.class).warn("convertPduType: Unknown SnmpAgentConfig PDU type requested...Defaulting to GET. Type requested: "+pduType);
            return PDU.SET;
        }
    }

    protected static Target getTarget(SnmpAgentConfig agentConfig) {
    
        Target target = null;
        
        //TODO: Need to do something better than this.
        if (!agentConfig.isAdapted()) {
            return target;
        }
        
        if (agentConfig.getVersion() == SnmpConstants.version3) {
            target = new UserTarget();
            ((UserTarget)target).setSecurityLevel(agentConfig.getSecurityLevel());
            ((UserTarget)target).setSecurityName(convertSecurityName(agentConfig.getSecurityName()));
        } else {
            target = new CommunityTarget();
            ((CommunityTarget)target).setCommunity(convertCommunity(agentConfig.getReadCommunity()));
        }
    
        target.setVersion((agentConfig.getVersion()));
        target.setRetries(agentConfig.getRetries());
        target.setTimeout(agentConfig.getTimeout());
        target.setAddress(convertAddress(agentConfig.getAddress(), agentConfig.getPort()));
        target.setMaxSizeRequestPDU(agentConfig.getMaxRequestSize());
            
        return target;
    }
    
    static OctetString createOctetString(String s) {
        
        if (s == null) {
            return null;
        }
        
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
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
