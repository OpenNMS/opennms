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

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.ScopedPDU;
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
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4JStrategy implements SnmpStrategy {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(Snmp4JStrategy.class);

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

        SNMP4JSettings.setEnterpriseID(5813);
        //USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        USM usm = new USM();
        SecurityModels.getInstance().addSecurityModel(usm);
        
        // Enable extensibility in SNMP4J so that we can subclass some SMI classes to work around
        // agent bugs
        if (System.getProperty("org.snmp4j.smisyntaxes", null) != null) {
        	SNMP4JSettings.setExtensibilityEnabled(true);
        }
        
        if (Boolean.getBoolean("org.opennms.snmp.snmp4j.forwardRuntimeExceptions")) {
        	SNMP4JSettings.setForwardRuntimeExceptions(true);
        }
        
        SNMP4JSettings.setAllowSNMPv2InV1(Boolean.getBoolean("org.opennms.snmp.snmp4j.allowSNMPv2InV1"));
        SNMP4JSettings.setNoGetBulk(Boolean.getBoolean("org.opennms.snmp.snmp4j.noGetBulk"));

        // NMS-9223: This call can be expensive, and is synchronized
        // so we perform it only once during initialization
        SecurityProtocols.getInstance().addDefaultProtocols();

        s_initialized = true;
    }
    
    public Snmp4JStrategy() {
        initialize();
    }
    
    /**
     * SNMP4J createWalker implemenetation.
     * 
     * @param snmpAgentConfig
     * @param name
     * @param tracker
     */
        @Override
    public SnmpWalker createWalker(SnmpAgentConfig snmpAgentConfig, String name, CollectionTracker tracker) {
        return new Snmp4JWalker(new Snmp4JAgentConfig(snmpAgentConfig), name, tracker);
    }
    
    /**
     * Not yet implemented.  Use a walker.
     */
        @Override
    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oid) {
    	throw new UnsupportedOperationException("Snmp4JStrategy.getBulk not yet implemented.");
    }

        @Override
    public SnmpValue set(final SnmpAgentConfig agentConfig, final SnmpObjId oid, final SnmpValue value) {
    	LOG.debug("set: OID: {} value: {} for Agent: {}", oid, value,  agentConfig);
        
        final SnmpObjId[] oids = { oid };
        final SnmpValue[] values = { value };
        final SnmpValue[] retvalues = set(agentConfig, oids, values);
        
        return retvalues[0];
    }

        @Override
    public SnmpValue[] set(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids, final SnmpValue[] values) {
    	LOG.debug("set: OIDs: {} values: {} for Agent: {}", oids, values, agentConfig);
        
        return buildAndSendPdu(agentConfig, PDU.SET, oids, values);
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
        @Override
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
    	LOG.debug("get: OID: {} for Agent: {}", oid, agentConfig);
        
        SnmpObjId[] oids = { oid };
        SnmpValue[] retvalues = get(agentConfig, oids);
        
        return retvalues[0];
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
        @Override
    public SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
    	LOG.debug("get: OID: {} for Agent: {}", oids, agentConfig);
        
        return buildAndSendPdu(agentConfig, PDU.GET, oids, null);
    }
    
    /**
     * SNMP4J getNext implementation
     * 
     * @param agentConfig
     * @param oid
     * 
     */
        @Override
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
    	LOG.debug("getNext: OID: {} for Agent: {}", oid, agentConfig);
        
        return getNext(agentConfig, new SnmpObjId[] { oid })[0];
    }
    
    /**
     * SNMP GetNext implementation.
     * 
     * @param agentConfig
     * @param oids
     * @return
     *        Returns an array of Snmp4JValues.  If the
     *        getNext was unsuccessful, then the first element
     *        of the array will be null and length of 1. 
     */
        @Override
    public SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
    	LOG.debug("getNext: OID: {} for Agent: {}", oids, agentConfig);
        
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
     * Sends and SNMP4J request PDU.  The attributes in SnmpAgentConfig should have been
     * adapted from default SnmpAgentConfig values to those compatible with the SNMP4J library.
     */
    protected SnmpValue[] send(Snmp4JAgentConfig agentConfig, PDU pdu, boolean expectResponse) {
        Snmp session;

        try {
            session = agentConfig.createSnmpSession();
        } catch (IOException e) {
            LOG.error("send: Could not create SNMP session for agent {}", agentConfig, e);
            return new SnmpValue[] { null };
        }

        try {
            if (expectResponse) {
                try {
                    session.listen();
                } catch (IOException e) {
                    LOG.error("send: error setting up listener for SNMP responses", e);
                    return new SnmpValue[] { null };
                }
            }
    
            try {
                final ResponseEvent responseEvent = session.send(pdu, agentConfig.getTarget());

                if (expectResponse) {
                    return processResponse(agentConfig, responseEvent);
                } else {
                    return null;
                }
            } catch (final IOException e) {
                LOG.error("send: error during SNMP operation", e);
                return new SnmpValue[] { null };
            } catch (final RuntimeException e) {
                LOG.error("send: unexpected error during SNMP operation", e);
                return new SnmpValue[] { null };
            }
        } finally {
            closeQuietly(session);
        }
    }
    

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
                LOG.error("PDU to prepare has object values but not the same number as there are OIDs.  There are {} OIDs and {} object values.", oids.length, values.length, e);
                return null;
            }
        
            for (int i = 0; i < oids.length; i++) {
                pdu.add(new VariableBinding(new OID(oids[i].toString()), new Snmp4JValue(values[i].getType(), values[i].getBytes()).getVariable()));
            }
        }
        
        // TODO should this throw an exception?  This situation is fairly bogus.
        if (pdu.getVariableBindings().size() != oids.length) {
            Exception e = new Exception("This is a bogus exception so we can get a stack backtrace");
            LOG.error("Prepared PDU does not have as many variable bindings as there are OIDs.  There are {} OIDs and {} variable bindings.", oids.length,pdu.getVariableBindings(), e);
            return null;
        }
        
        return pdu;
    }

    /**
     * TODO: Merge this logic with {@link Snmp4JWalker.Snmp4JResponseListener} #processResponse(PDU response)
     */
    private static SnmpValue[] processResponse(Snmp4JAgentConfig agentConfig, ResponseEvent responseEvent) throws IOException {
        SnmpValue[] retvalues = { null };

        if (responseEvent.getResponse() == null) {
            LOG.warn("processResponse: Timeout.  Agent: {}, requestID={}", agentConfig, responseEvent.getRequest().getRequestID());
        } else if (responseEvent.getError() != null) {
            LOG.warn("processResponse: Error during get operation.  Error: {}, requestID={}", responseEvent.getError().getLocalizedMessage(), responseEvent.getError(), responseEvent.getRequest().getRequestID());
        } else if (responseEvent.getResponse().getType() == PDU.REPORT) {
            LOG.warn("processResponse: Error during get operation.  Report returned with varbinds: {}, requestID={}", responseEvent.getResponse().getVariableBindings(), responseEvent.getRequest().getRequestID());
        } else if (responseEvent.getResponse().getVariableBindings().size() < 1) {
            LOG.warn("processResponse: Received PDU with 0 varbinds. Agent: {}, requestID={}", agentConfig, responseEvent.getRequest().getRequestID());
        } else if (responseEvent.getResponse().get(0).getSyntax() == SMIConstants.SYNTAX_NULL) {
            LOG.info("processResponse: Null value returned in varbind: {}. Agent: {}, requestID={}", responseEvent.getResponse().get(0), agentConfig, responseEvent.getRequest().getRequestID());
        } else {
            retvalues = convertResponseToValues(responseEvent);

            LOG.debug("processResponse: SNMP operation successful, value: {}", (Object)retvalues);
        }

        return retvalues;
    }

    private static SnmpValue[] convertResponseToValues(ResponseEvent responseEvent) {
        SnmpValue[] retvalues = new Snmp4JValue[responseEvent.getResponse().getVariableBindings().size()];
        
        for (int i = 0; i < retvalues.length; i++) {
            retvalues[i] = new Snmp4JValue(responseEvent.getResponse().get(i).getVariable());
        }
        
        return retvalues;
    }

        @Override
    public SnmpValueFactory getValueFactory() {
        if (m_valueFactory == null) {
            m_valueFactory = new Snmp4JValueFactory();
        }
        return m_valueFactory;
    }
    
    public static class RegistrationInfo {
        private TrapNotificationListener m_listener;
        
        Snmp m_trapSession;
        Snmp4JTrapNotifier m_trapHandler;
        private TransportMapping<UdpAddress> m_transportMapping;
		private InetAddress m_address;
		private int m_port;
        
        RegistrationInfo(TrapNotificationListener listener, int trapPort) throws SocketException {
        	if (listener == null) throw new NullPointerException("You must specify a trap notification listener.");
        	LOG.debug("trapPort = {}", trapPort);
    
            m_listener = listener;
            m_port = trapPort;
        }
    
        public RegistrationInfo(final TrapNotificationListener listener, final InetAddress address, final int snmpTrapPort) {
        	if (listener == null) throw new NullPointerException("You must specify a trap notification listener.");

        	m_listener = listener;
        	m_address = address;
        	m_port = snmpTrapPort;
		}

        public void setSession(final Snmp trapSession) {
            m_trapSession = trapSession;
        }
        
        public Snmp getSession() {
            return m_trapSession;
        }
        
        public void setHandler(final Snmp4JTrapNotifier trapHandler) {
            m_trapHandler = trapHandler;
        }
        
        public Snmp4JTrapNotifier getHandler() {
            return m_trapHandler;
        }

        public InetAddress getAddress() {
        	return m_address;
        }
        
        public int getPort() {
            return m_port;
        }

        public void setTransportMapping(final TransportMapping<UdpAddress> transport) {
            m_transportMapping = transport;
        }
        
        public TransportMapping<UdpAddress> getTransportMapping() {
            return m_transportMapping;
        }
        
        @Override
        public int hashCode() {
            return (m_listener.hashCode() + m_address.hashCode() ^ m_port);
        }
        
        @Override
		public boolean equals(final Object obj) {
            if (obj instanceof RegistrationInfo) {
            	final RegistrationInfo info = (RegistrationInfo) obj;
                return (m_listener == info.m_listener) && Arrays.equals(m_address.getAddress(), info.getAddress().getAddress()) && m_port == info.getPort();
            }
            return false;
        }
        
    }

        @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, InetAddress address, int snmpTrapPort, List<SnmpV3User> snmpUsers) throws IOException {
    	final RegistrationInfo info = new RegistrationInfo(listener, address, snmpTrapPort);
        
    	final Snmp4JTrapNotifier m_trapHandler = new Snmp4JTrapNotifier(listener, processorFactory);
        info.setHandler(m_trapHandler);

        final UdpAddress udpAddress;
        if (address == null) {
        	udpAddress = new UdpAddress(snmpTrapPort);
        } else {
        	udpAddress = new UdpAddress(address, snmpTrapPort);
        }
        final TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(udpAddress);
        info.setTransportMapping(transport);
        Snmp snmp = new Snmp(transport);
        snmp.addCommandResponder(m_trapHandler);

        if (snmpUsers != null) {
            for (SnmpV3User user : snmpUsers) {
                SnmpAgentConfig config = new SnmpAgentConfig();
                config.setVersion(SnmpConfiguration.VERSION3);
                config.setSecurityName(user.getSecurityName());
                config.setAuthProtocol(user.getAuthProtocol());
                config.setAuthPassPhrase(user.getAuthPassPhrase());
                config.setPrivProtocol(user.getPrivProtocol());
                config.setPrivPassPhrase(user.getPrivPassPhrase());
                Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(config);
                UsmUser usmUser = new UsmUser(
                        agentConfig.getSecurityName(),
                        agentConfig.getAuthProtocol(),
                        agentConfig.getAuthPassPhrase(),
                        agentConfig.getPrivProtocol(),
                        agentConfig.getPrivPassPhrase()
                );
                /* This doesn't work as expected. Basically SNMP4J is ignoring the engineId
                if (user.getEngineId() == null) {
                    snmp.getUSM().addUser(agentConfig.getSecurityName(), usmUser);
                } else {
                    snmp.getUSM().addUser(agentConfig.getSecurityName(), new OctetString(user.getEngineId()), usmUser);
                }
                */
                snmp.getUSM().addUser(agentConfig.getSecurityName(), usmUser);
            }
        }

        info.setSession(snmp);
        
        s_registrations.put(listener, info);
        
        snmp.listen();
    }
    
        @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, InetAddress address, int snmpTrapPort) throws IOException {
        registerForTraps(listener, processorFactory, address, snmpTrapPort, null);
    }

        @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final int snmpTrapPort) throws IOException {
    	registerForTraps(listener, processorFactory, null, snmpTrapPort);
    }

        @Override
    public void unregisterForTraps(final TrapNotificationListener listener, InetAddress address, int snmpTrapPort) throws IOException {
        RegistrationInfo info = s_registrations.remove(listener);
        closeQuietly(info.getSession());
    }

        @Override
    public void unregisterForTraps(final TrapNotificationListener listener, final int snmpTrapPort) throws IOException {
        RegistrationInfo info = s_registrations.remove(listener);
        closeQuietly(info.getSession());
    }

        @Override
    public SnmpV1TrapBuilder getV1TrapBuilder() {
        return new Snmp4JV1TrapBuilder(this);
    }

        @Override
    public SnmpTrapBuilder getV2TrapBuilder() {
        return new Snmp4JV2TrapBuilder(this);
    }

        @Override
    public SnmpV3TrapBuilder getV3TrapBuilder() {
        return new Snmp4JV3TrapBuilder(this);
    }

        @Override
    public SnmpV2TrapBuilder getV2InformBuilder() {
        return new Snmp4JV2InformBuilder(this);
    }

        @Override
    public SnmpV3TrapBuilder getV3InformBuilder() {
        return new Snmp4JV3InformBuilder(this);
    }

    protected SnmpAgentConfig buildAgentConfig(String address, int port, String community, PDU pdu) throws UnknownHostException {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getByName(address));
        config.setPort(port);
        config.setVersion(pdu instanceof PDUv1 ? SnmpAgentConfig.VERSION1 : SnmpAgentConfig.VERSION2C);
        return config;
    }

    protected SnmpAgentConfig buildAgentConfig(String address, int port, int timeout, int retries, String community, PDU pdu) throws UnknownHostException {
        SnmpAgentConfig config = buildAgentConfig(address, port, community, pdu);
        config.setTimeout(timeout);
        config.setRetries(retries);
        return config;
    }

	protected SnmpAgentConfig buildAgentConfig(String address, int port, int securityLevel,
			String securityName, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privProtocol, PDU pdu) throws UnknownHostException, Exception {
			
		if (! (pdu instanceof ScopedPDU)) 
				throw new Exception();

			SnmpAgentConfig config = new SnmpAgentConfig();
	        config.setAddress(InetAddress.getByName(address));
	        config.setPort(port);
	        config.setVersion(SnmpAgentConfig.VERSION3);
	        config.setSecurityLevel(securityLevel);
	        config.setSecurityName(securityName);
	        config.setAuthPassPhrase(authPassPhrase);
	        config.setAuthProtocol(authProtocol);
	        config.setPrivPassPhrase(privPassPhrase);
	        config.setPrivProtocol(privProtocol);
	        return config;

	}

	protected SnmpAgentConfig buildAgentConfig(String address, int port, int timeout, int retries, int securityLevel,
			String securityName, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privProtocol, PDU pdu) throws UnknownHostException, Exception {
			
			SnmpAgentConfig config = buildAgentConfig(address, port, securityLevel, securityName, authPassPhrase, authProtocol, privPassPhrase, privProtocol, pdu);
	        config.setTimeout(timeout);
	        config.setRetries(retries);
	        return config;

	}

	
    public void sendTest(String agentAddress, int port, String community, PDU pdu) {
        for (RegistrationInfo info : s_registrations.values()) {
            if (port == info.getPort()) {
                Snmp snmp = info.getSession();
                MessageDispatcher dispatcher = snmp.getMessageDispatcher();
                TransportMapping<UdpAddress> transport = info.getTransportMapping();
                
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
            LOG.error("error closing SNMP connection", e);
        }
    }

        @Override
	public byte[] getLocalEngineID() {
		return MPv3.createLocalEngineID();
	}

}
