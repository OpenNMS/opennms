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
// Modifications:
//
// 2007 Jun 23: Code formatting, deduplication (especially the send methods),
//              and more specific log messages. - dj@opennms.org
// 2007 Jun 22: Iterate over the proper array in the four-argument send method
//              and don't change the input values array. - dj@opennms.org
// 2007 Jun 22: Make the static sendTest(...) method non-static.
//              - dj@opennms.org
// 2007 Jun 22: Make the static send(...) method non-static and do some
//              various code cleanup. - dj@opennms.org
// 2007 Jun 21: Always use SnmpHelpers.createSnmpSession() to create SNMP
//              sessions, including eliminating static Snmp object used
//              for sending traps.  Improve error reporting. - dj@opennms.org
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
import java.net.UnknownHostException;
import java.util.HashMap;
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
import org.snmp4j.MessageDispatcher;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.PduHandle;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * <p>Snmp4JStrategy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Snmp4JStrategy implements SnmpStrategy {

    private static Map<TrapNotificationListener, RegistrationInfo> s_registrations = new HashMap<TrapNotificationListener, RegistrationInfo>();
    
    private static boolean s_initialized = false;
    
    private Snmp4JValueFactory m_valueFactory;

    /**
     * Initialize for v3 communications
     */
    private static void initialize() {
        if (s_initialized) {
            return;
        }

//      LogFactory.setLogFactory(new Log4jLogFactory());
            
        MPv3.setEnterpriseID(5813);
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        
        // Enable extensibility in SNMP4J so that we can subclass some SMI classes to work around
        // agent bugs
        if (System.getProperty("org.snmp4j.smisyntaxes", null) != null) {
        	SNMP4JSettings.setExtensibilityEnabled(true);
        }
        
        if (Boolean.getBoolean("org.opennms.snmp.snmp4j.forwardRuntimeExceptions")) {
        	SNMP4JSettings.setForwardRuntimeExceptions(true);
        }
        
        s_initialized = true;
    }
    
    /**
     * <p>Constructor for Snmp4JStrategy.</p>
     */
    public Snmp4JStrategy() {
        initialize();
    }
    
    /**
     * {@inheritDoc}
     *
     * SNMP4J createWalker implemenetation.
     */
    public SnmpWalker createWalker(SnmpAgentConfig snmpAgentConfig, String name, CollectionTracker tracker) {
        return new Snmp4JWalker(new Snmp4JAgentConfig(snmpAgentConfig), name, tracker);
    }
    
    /**
     * Not yet implemented.  Use a walker.
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oid) {
    	throw new UnsupportedOperationException("Snmp4JStrategy.getBulk not yet implemented.");
    }

    /** {@inheritDoc} */
    public SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value) {
        if (log().isDebugEnabled()) {
            log().debug("set: OID: " + oid + " value: " + value.toString() + " for Agent: " + agentConfig);
        }
        
        SnmpObjId[] oids = { oid };
        SnmpValue[] values = { value };
        SnmpValue[] retvalues = set(agentConfig, oids, values);
        
        return retvalues[0];
    }

    /** {@inheritDoc} */
    public SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId[] oids, SnmpValue[] values) {
        if (log().isDebugEnabled()) {
            log().debug("set: OIDs: " + oids + " values: " + values + " for Agent: " + agentConfig);
        }
        
        return buildAndSendPdu(agentConfig, PDU.SET, oids, values);
    }

    /**
     * {@inheritDoc}
     *
     * SNMP4J get helper that takes a single SnmpObjId
     * and calls get with an array.lenght =1 and returns
     * the first element of the returned array of SnmpValue.
     */
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled()) {
            log().debug("get: OID: "+oid+" for Agent:"+agentConfig);
        }
        
        SnmpObjId[] oids = { oid };
        SnmpValue[] retvalues = get(agentConfig, oids);
        
        return retvalues[0];
    }
    
    /**
     * {@inheritDoc}
     *
     * SnmpGet implementation.
     */
    public SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        if (log().isDebugEnabled()) {
            log().debug("get: OID: "+oids+" for Agent:"+agentConfig);
        }
        
        return buildAndSendPdu(agentConfig, PDU.GET, oids, null);
    }
    
    /**
     * SNMP4J getNext implementation
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled()) {
            log().debug("getNext: OID: "+oid+" for Agent:"+agentConfig);
        }
        
        SnmpObjId[] oids = { oid };
        SnmpValue[] retvalues = getNext(agentConfig, oids);
        return retvalues[0];
    }
    
    /**
     * SNMP GetNext implementation.
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    public SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        if (log().isDebugEnabled()) {
            log().debug("getNext: OID: "+oids+" for Agent:"+agentConfig);
        }
        
        return buildAndSendPdu(agentConfig, PDU.GETNEXT, oids, null);
    }

    private SnmpValue[] buildAndSendPdu(SnmpAgentConfig agentConfig, int type, SnmpObjId[] oids, SnmpValue[] values) {
        Snmp4JAgentConfig snmp4jAgentConfig = new Snmp4JAgentConfig(agentConfig);
        
        PDU pdu = buildPdu(snmp4jAgentConfig, type, oids, values);
        if (pdu == null) {
            return null;
        }
        
        return send(snmp4jAgentConfig, pdu, true);
    }

    /**
     * Sends and SNMP4J request pdu.  The attributes in SnmpAgentConfig should have been
     * adapted from default SnmpAgentConfig values to those compatible with the SNMP4J library.
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.snmp4j.Snmp4JAgentConfig} object.
     * @param pdu a {@link org.snmp4j.PDU} object.
     * @param expectResponse a boolean.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     */
    protected SnmpValue[] send(Snmp4JAgentConfig agentConfig, PDU pdu, boolean expectResponse) {
        Snmp session;

        try {
            session = agentConfig.createSnmpSession();
        } catch (IOException e) {
            log().error("send: Could not create SNMP session for agent " + agentConfig + ": " + e, e);
            return new SnmpValue[] { null };
        }

        try {
            if (expectResponse) {
                try {
                    session.listen();
                } catch (IOException e) {
                    log().error("send: error setting up listener for SNMP responses: " + e, e);
                    return new SnmpValue[] { null };
                }
            }
    
            try {
                ResponseEvent responseEvent = session.send(pdu, agentConfig.getTarget());

                if (expectResponse) {
                    return processResponse(agentConfig, responseEvent);
                } else {
                    return null;
                }
            } catch (IOException e) {
                log().error("send: error during SNMP operation: " + e, e);
                return new SnmpValue[] { null };
            }
        } finally {
            closeQuietly(session);
        }
    }

    /**
     * <p>buildPdu</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.snmp4j.Snmp4JAgentConfig} object.
     * @param pduType a int.
     * @param oids an array of {@link org.opennms.netmgt.snmp.SnmpObjId} objects.
     * @param values an array of {@link org.opennms.netmgt.snmp.SnmpValue} objects.
     * @return a {@link org.snmp4j.PDU} object.
     */
    protected PDU buildPdu(Snmp4JAgentConfig agentConfig, int pduType, SnmpObjId[] oids, SnmpValue[] values) {
        PDU pdu = agentConfig.createPdu(pduType);
        
        if (values == null) {
            for (SnmpObjId oid : oids) {
                pdu.add(new VariableBinding(new OID(oid.toString())));
            }
        } else {
            // TODO should this throw an exception?  This situation is fairly bogus and probably signifies a coding error.
            if (oids.length != values.length) {
                Exception e = new Exception("This is a bogus exception so we can get a stack backtrace");
                log().error("PDU to prepare has object values but not the same number as there are OIDs.  There are " + oids.length + " OIDs and " + values.length + " object values.", e);
                return null;
            }
        
            for (int i = 0; i < oids.length; i++) {
                pdu.add(new VariableBinding(new OID(oids[i].toString()), new Snmp4JValue(values[i].getType(), values[i].getBytes()).getVariable()));
            }
        }
        
        // TODO should this throw an exception?  This situation is fairly bogus.
        if (pdu.getVariableBindings().size() != oids.length) {
            Exception e = new Exception("This is a bogus exception so we can get a stack backtrace");
            log().error("Prepared PDU does not have as many variable bindings as there are OIDs.  There are " + oids.length + " OIDs and " + pdu.getVariableBindings() + " variable bindings.", e);
            return null;
        }
        
        return pdu;
    }
    
    private SnmpValue[] processResponse(Snmp4JAgentConfig agentConfig, ResponseEvent responseEvent) throws IOException {
        SnmpValue[] retvalues = { null };

        if (responseEvent.getResponse() == null) {
            log().warn("send: Timeout.  Agent: "+agentConfig);
        } else if (responseEvent.getResponse().get(0).getSyntax() == SMIConstants.SYNTAX_NULL) {
            retvalues[0] = null;
        } else if (responseEvent.getError() != null) {
            log().warn("send: Error during get operation.  Error: "+responseEvent.getError().getLocalizedMessage());
        } else if (responseEvent.getResponse().getType() == PDU.REPORT) {
            log().warn("send: Error during get operation.  Report returned with varbinds: "+responseEvent.getResponse().getVariableBindings());
        } else if (responseEvent.getResponse().getVariableBindings().size() < 1) {
            log().warn("send: Received PDU with 0 varbinds.");
        } else {
            retvalues = convertResponseToValues(responseEvent);

            if (log().isDebugEnabled()) {
                log().debug("send: Snmp operation successful. Value: "+retvalues);
            }
        }

        return retvalues;
    }

    private SnmpValue[] convertResponseToValues(ResponseEvent responseEvent) {
        SnmpValue[] retvalues = new Snmp4JValue[responseEvent.getResponse().getVariableBindings().size()];
        
        for (int i = 0; i < retvalues.length; i++) {
            retvalues[i] = new Snmp4JValue(responseEvent.getResponse().get(i).getVariable());
        }
        
        return retvalues;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * <p>getValueFactory</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValueFactory} object.
     */
    public SnmpValueFactory getValueFactory() {
        if (m_valueFactory == null) {
            m_valueFactory = new Snmp4JValueFactory();
        }
        return m_valueFactory;
    }

    public static class RegistrationInfo {
        public TrapNotificationListener m_listener;
        int m_trapPort;
        
        Snmp m_trapSession;
        Snmp4JTrapNotifier m_trapHandler;
        private TransportMapping m_transportMapping;
        
        RegistrationInfo(TrapNotificationListener listener, int trapPort) {
            if (listener == null) {
                throw new NullPointerException("listener is null");
            }
    
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



    /** {@inheritDoc} */
    public void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, int snmpTrapPort) throws IOException {
        RegistrationInfo info = new RegistrationInfo(listener, snmpTrapPort);
        
        Snmp4JTrapNotifier m_trapHandler = new Snmp4JTrapNotifier(listener, processorFactory);
        info.setHandler(m_trapHandler);

        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress(snmpTrapPort));
        info.setTransportMapping(transport);
        Snmp snmp = new Snmp(transport);
        snmp.addCommandResponder(m_trapHandler);
        info.setSession(snmp);
        
        s_registrations.put(listener, info);
        
        snmp.listen();
    }

    /** {@inheritDoc} */
    public void unregisterForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException {
        RegistrationInfo info = s_registrations.remove(listener);
        closeQuietly(info.getSession());
    }

    /**
     * <p>getV1TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpV1TrapBuilder} object.
     */
    public SnmpV1TrapBuilder getV1TrapBuilder() {
        return new Snmp4JV1TrapBuilder(this);
    }

    /**
     * <p>getV2TrapBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpTrapBuilder} object.
     */
    public SnmpTrapBuilder getV2TrapBuilder() {
        return new Snmp4JV2TrapBuilder(this);
    }
    
    /**
     * <p>buildAgentConfig</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param port a int.
     * @param community a {@link java.lang.String} object.
     * @param pdu a {@link org.snmp4j.PDU} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @throws java.net.UnknownHostException if any.
     */
    protected SnmpAgentConfig buildAgentConfig(String address, int port, String community, PDU pdu) throws UnknownHostException {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getByName(address));
        config.setPort(port);
        config.setVersion(pdu instanceof PDUv1 ? SnmpAgentConfig.VERSION1 : SnmpAgentConfig.VERSION2C);
        return config;
    }
    
    /**
     * <p>sendTest</p>
     *
     * @param agentAddress a {@link java.lang.String} object.
     * @param port a int.
     * @param community a {@link java.lang.String} object.
     * @param pdu a {@link org.snmp4j.PDU} object.
     */
    public void sendTest(String agentAddress, int port, String community, PDU pdu) {
        for (RegistrationInfo info : s_registrations.values()) {
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

    private void closeQuietly(Snmp session) {
        if (session == null) {
            return;
        }
        
        try {
            session.close();
        } catch (IOException e) {
            ThreadCategory.getInstance(Snmp4JStrategy.class).error("error closing SNMP connection: " + e, e);
        }
    }
}
