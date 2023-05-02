/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpAgentTimeoutException;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MutablePDU;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;
import org.snmp4j.event.AuthenticationFailureEvent;
import org.snmp4j.event.AuthenticationFailureListener;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.MutableStateReference;
import org.snmp4j.mp.PduHandle;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.security.UsmUserEntry;
import org.snmp4j.security.UsmUserTable;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class Snmp4JStrategy implements SnmpStrategy {
    private static final transient Logger LOG = LoggerFactory.getLogger(Snmp4JStrategy.class);

    private static final ExecutorService REAPER_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SNMP4J-Session-Reaper");
        }
    });

    private static Map<TrapNotificationListener, RegistrationInfo> s_registrations = new HashMap<>();

    private static boolean s_initialized = false;

    private static USM m_usm;

    private Snmp4JValueFactory m_valueFactory;

    private static ScheduledExecutorService s_sessionStatsExecutor;
    private static ConcurrentHashMap<Snmp, SessionInfo> s_sessions;
    private static boolean s_trackSessions = Boolean.getBoolean("org.opennms.core.snmp.trackSessions");
    private static long s_trackSummaryDelay = SystemProperties.getLong("org.opennms.core.snmp.trackSummaryDelay", 60);
    private static long s_trackSummaryLimit = SystemProperties.getLong("org.opennms.core.snmp.trackSummaryLimit", 10);

    /**
     * Initialize for v3 communications
     */
    private static void initialize() {
        if (s_initialized) {
            return;
        }

        SNMP4JSettings.setEnterpriseID(5813);
        
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
        // NMS-15637: AES-256 gets automatically registered, but with an ID of "" instead of "1.3.6.1.4.1.4976.2.2.1.1.2"
        // Adding it manually here results in it being registered with the correct ID
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256());

        s_initialized = true;
    }
    
    public Snmp4JStrategy() {
        initialize();
    }
    
    public void clearUsers() {
        if (m_usm != null) {
            m_usm.removeAllUsers();
        }
    }
    
    /**
     * SNMP4J createWalker implementation.
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

    @Override
    public CompletableFuture<SnmpValue[]> getAsync(SnmpAgentConfig agentConfig, SnmpObjId[] oids) {
        final CompletableFuture<SnmpValue[]> future = new CompletableFuture<>();
        final Snmp4JAgentConfig snmp4jAgentConfig = new Snmp4JAgentConfig(agentConfig);
        final PDU pdu = buildPdu(snmp4jAgentConfig, PDU.GET, oids, null);
        if (pdu == null) {
            future.completeExceptionally(new Exception("Invalid PDU for OIDs: " + Arrays.toString(oids)));
        }
        send(snmp4jAgentConfig, pdu, true, future);
        return future;
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
        final CompletableFuture<SnmpValue[]> future = new CompletableFuture<>();
        send(agentConfig, pdu, expectResponse, future);
        try {
            return future.get();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return new SnmpValue[] { null };
        }
    }

    private void send(Snmp4JAgentConfig agentConfig, PDU pdu, boolean expectResponse, CompletableFuture<SnmpValue[]> future) {
        Snmp session;

        try {
            session = agentConfig.createSnmpSession();
            Snmp4JStrategy.trackSession(session);
        } catch (final Exception e) {
            LOG.error("send: Could not create SNMP session for agent {}", agentConfig, e);
            future.completeExceptionally(new SnmpException("Could not create SNMP session for agent", e));
            return;
        }

        if (expectResponse) {
            try {
                session.listen();
            } catch (final Exception e) {
                closeQuietly(session);
                LOG.error("send: error setting up listener for SNMP responses", e);
                future.completeExceptionally(new SnmpException("error setting up listener for SNMP responses", e));
                return;
            }

            try {
                final Snmp mySession = session;
                mySession.send(pdu, agentConfig.getTarget(), null, new ResponseListener() {
                    @Override
                    public void onResponse(final ResponseEvent responseEvent) {
                        try {
                            future.complete(processResponse(agentConfig, responseEvent, pdu));
                        } catch (final Exception e) {
                            future.completeExceptionally(new SnmpException(e));
                        } finally {
                            // Close the tracker using a separate thread
                            // This allows the SnmpWalker to clean up properly instead
                            // of interrupting execution as it's executing the callback
                            REAPER_EXECUTOR.submit(new Runnable() {
                                @Override
                                public void run() {
                                    closeQuietly(mySession);
                                }
                            });
                        }
                    }
                });
            } catch (final Exception e) {
                // The ResponseListener will not be called since an exception occurred in the send,
                // so we make sure to close the session here
                closeQuietly(session);
                LOG.error("send: error during SNMP operation", e);
                future.completeExceptionally(e);
            }
        } else { // we're not expecting a response
            try {
                session.send(pdu, agentConfig.getTarget());
                future.complete(null);
            } catch (final Exception e) {
                LOG.error("send: error during SNMP operation", e);
                future.completeExceptionally(new SnmpException(e));
            } finally {
                closeQuietly(session);
                Snmp4JStrategy.reapSession(session);
            }
        }
    }

    protected static PDU buildPdu(Snmp4JAgentConfig agentConfig, int pduType, SnmpObjId[] oids, SnmpValue[] values) {
        PDU pdu = agentConfig.createPdu(pduType);
        
        if (values == null) {
            for (SnmpObjId oid : oids) {
                pdu.add(new VariableBinding(new OID(oid.toString())));
            }
        } else {
            // TODO should this throw an exception?  This situation is fairly bogus and probably signifies a coding error.
            if (oids.length != values.length) {
                Exception e = new SnmpException("PDU values do not match OIDs");
                LOG.error("PDU to prepare has object values but not the same number as there are OIDs.  There are {} OIDs and {} object values.", oids.length, values.length, e);
                return null;
            }
        
            for (int i = 0; i < oids.length; i++) {
                pdu.add(new VariableBinding(new OID(oids[i].toString()), new Snmp4JValue(values[i].getType(), values[i].getBytes()).getVariable()));
            }
        }
        
        // TODO should this throw an exception?  This situation is fairly bogus.
        if (pdu.getVariableBindings().size() != oids.length) {
            Exception e = new SnmpException("PDU bindings do not match OIDs");
            LOG.error("Prepared PDU does not have as many variable bindings as there are OIDs.  There are {} OIDs and {} variable bindings.", oids.length,pdu.getVariableBindings(), e);
            return null;
        }
        
        return pdu;
    }

    /**
     * TODO: Merge this logic with {@link Snmp4JWalker.Snmp4JResponseListener} #processResponse(PDU response)
     */
    static SnmpValue[] processResponse(Snmp4JAgentConfig agentConfig, ResponseEvent responseEvent, PDU requestPdu) throws IOException, SnmpAgentTimeoutException, SnmpException {
        SnmpValue[] retvalues = { null };

        if (responseEvent.getResponse() == null) {
            LOG.warn("processResponse: Timeout.  Agent: {}, requestID={}", agentConfig, responseEvent.getRequest().getRequestID());
            throw new SnmpAgentTimeoutException(agentConfig.getInetAddress());
        } else if (responseEvent.getError() != null) {
            LOG.warn("processResponse: Error during get operation.  Error: {}, requestID={}", responseEvent.getError().getLocalizedMessage(), responseEvent.getError(), responseEvent.getRequest().getRequestID());
            String errorMsg = "SNMP Internal error for: " + agentConfig.getInetAddress() + " Error : " + responseEvent.getError();
            throw new SnmpException(errorMsg);
        } else if (responseEvent.getResponse().getType() == PDU.REPORT) {
            LOG.warn("processResponse: Error during get operation.  Report returned with varbinds: {}, requestID={}", responseEvent.getResponse().getVariableBindings(), responseEvent.getRequest().getRequestID());
            String errorMsg = "Error during get operation.  Report returned with varbinds: " + responseEvent.getResponse().getVariableBindings() + " , requestID=" + responseEvent.getRequest().getRequestID();
            throw new SnmpException(errorMsg);
        } else if (responseEvent.getResponse().getVariableBindings().size() < 1) {
            LOG.warn("processResponse: Received PDU with 0 varbinds. Agent: {}, requestID={}", agentConfig, responseEvent.getRequest().getRequestID());
            String errorMsg =  "Received PDU with 0 varbinds. Agent: " + agentConfig + ", requestID= " + responseEvent.getRequest().getRequestID();
            throw new SnmpException(errorMsg);
        } else if (responseEvent.getResponse().get(0).getSyntax() == SMIConstants.SYNTAX_NULL) {
            LOG.info("processResponse: Null value returned in varbind: {}. Agent: {}, requestID={}", responseEvent.getResponse().get(0), agentConfig, responseEvent.getRequest().getRequestID());
            String errorMsg = "Null value returned in varbind: " + responseEvent.getResponse().get(0) + " Agent: " + agentConfig + " requestID = " + responseEvent.getRequest().getRequestID();
            throw new SnmpException(errorMsg);
        } else {
            retvalues = convertResponseToValues(agentConfig, responseEvent, requestPdu);

            LOG.debug("processResponse: SNMP operation successful, value: {}", (Object)retvalues);
        }

        return retvalues;
    }

    private static SnmpValue[] convertResponseToValues(Snmp4JAgentConfig agentConfig, ResponseEvent responseEvent, PDU requestPdu) {
        SnmpValue[] retvalues = new Snmp4JValue[responseEvent.getResponse().getVariableBindings().size()];

        if (requestPdu.size() != retvalues.length) {
            LOG.warn("Unexpected results, Request oids length doesn't match response values length for the " +
                            "Agent : {}, requestID= {}", agentConfig, responseEvent.getRequest().getRequestID());
            retvalues = new Snmp4JValue[requestPdu.size()];
            int resultSize = responseEvent.getResponse().getVariableBindings().size();
            for (int i = 0; i < requestPdu.size(); i++) {
                VariableBinding requestVb = requestPdu.getVariableBindings().get(i);
                // Try matching in order
                if (i < resultSize && requestVb.getOid().equals(responseEvent.getResponse().get(i).getOid())) {
                    retvalues[i] = new Snmp4JValue(responseEvent.getResponse().get(i).getVariable());
                } else {
                    // Find matching Variable binding which matches request OID
                    Optional<? extends VariableBinding> matchingVb = responseEvent.getResponse().getVariableBindings().stream()
                            .filter(vb -> vb.getOid().equals(requestVb.getOid())).findFirst();
                    if (matchingVb.isPresent()) {
                        retvalues[i] = new Snmp4JValue(matchingVb.get().getVariable());
                    } else {
                        retvalues[i] = new Snmp4JValue(SnmpValue.SNMP_NULL, null);
                    }
                }
            }
            return retvalues;
        }
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
            if (m_trapSession != null && m_trapSession != trapSession) {
                LOG.warn("replacing existing session {} with {}", m_trapSession, trapSession);
            }
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
    public void registerForTraps(final TrapNotificationListener listener, InetAddress address, int snmpTrapPort, List<SnmpV3User> snmpUsers) throws IOException {
    	final RegistrationInfo info = new RegistrationInfo(listener, address, snmpTrapPort);
        
    	final Snmp4JTrapNotifier trapNotifier = new Snmp4JTrapNotifier(listener);
        info.setHandler(trapNotifier);

        final UdpAddress udpAddress;
        if (address == null) {
        	udpAddress = new UdpAddress(snmpTrapPort);
        } else {
        	udpAddress = new UdpAddress(address, snmpTrapPort);
        }

        // Set socket option SO_REUSEADDR so that we can bind to the port even if it
        // has recently been closed by passing 'true' as the second argument here.
        final DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(udpAddress, true);
        // Increase the receive buffer for the socket
        LOG.debug("Attempting to set receive buffer size to {}", Integer.MAX_VALUE);
        transport.setReceiveBufferSize(Integer.MAX_VALUE);
        LOG.debug("Actual receive buffer size is {}", transport.getReceiveBufferSize());

        info.setTransportMapping(transport);

        MessageDispatcher dispatcher = new MessageDispatcherImpl();
        // add message processing models
        dispatcher.addMessageProcessingModel(new MPv1());
        dispatcher.addMessageProcessingModel(new MPv2c());
        dispatcher.addMessageProcessingModel(new MPv3(getLocalEngineID()));

        Snmp snmp = new Snmp(dispatcher, transport);
        m_usm = new USM(SecurityProtocols.getInstance(), new OctetString(getLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(m_usm);

        if (snmpUsers != null) {
            // Split users up based on security name
            Map<UsmUserTable.UsmUserKey, List<UsmUser>> usmUsersByKey = snmpUsers.stream()
                    .map(user -> {
                        SnmpAgentConfig config = new SnmpAgentConfig();
                        config.setVersion(SnmpConfiguration.VERSION3);
                        config.setSecurityName(user.getSecurityName());
                        config.setAuthProtocol(user.getAuthProtocol());
                        config.setAuthPassPhrase(user.getAuthPassPhrase());
                        config.setPrivProtocol(user.getPrivProtocol());
                        config.setPrivPassPhrase(user.getPrivPassPhrase());
                        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(config);
                        return new UsmUser(
                                agentConfig.getSecurityName(),
                                agentConfig.getAuthProtocol(),
                                agentConfig.getAuthPassPhrase(),
                                agentConfig.getPrivProtocol(),
                                agentConfig.getPrivPassPhrase()
                        );
                        // Use the same key as SNMP4J uses in the UserTable
                    }).collect(Collectors.groupingBy(user ->new UsmUserTable.UsmUserKey(new UsmUserEntry(user.getSecurityName(), user))));

            // The map may contain a list with multiple entries for a given key
            // Process the first entries by adding all users to the default USM context
            usmUsersByKey.values().stream()
                    .filter(usmUsers -> usmUsers.size() > 0)
                    .map(usmUsers -> usmUsers.get(0))
                    .forEach(u -> snmp.getUSM().addUser(u.getSecurityName(), u));

            // Determine the maximum number of entries across all lists
            int maxNumUniqueDefs = usmUsersByKey.values().stream()
                    .mapToInt(List::size).max().orElse(0);

            // For every additional index (meaning there are duplicate entries for the same key)
            // gather the entries across all the other lists and:
            //   1) Add these to a new USM context
            //   2) Create a new SNMPv3 processor for this context that is run *after* the previous processors
            // All SNMPv3 packets are expected to be processed by all processors, but only 1 is expected to respond
            for (int k = 1; k < maxNumUniqueDefs; k++) {
                // New USM context with same engine ID
                USM usm = new USM();
                usm.setLocalEngine(new OctetString(getLocalEngineID()), 0, 0);

                // Add all the corresponding users to the USM context
                final int index = k;
                usmUsersByKey.values().stream()
                        .filter(usmUsers -> usmUsers.size() > index)
                        .map(usmUsers -> usmUsers.get(index))
                        .forEach(u -> usm.addUser(u.getSecurityName(), u));

                // Create another dispatcher for this context
                final MessageDispatcherImpl nextDispatcher = new BufferRewindingMessageDispatcher();

                AuthenticationFailureLogger authenticationFailureLogger = new AuthenticationFailureLogger(k, maxNumUniqueDefs - 1);
                nextDispatcher.addAuthenticationFailureListener(authenticationFailureLogger);
                nextDispatcher.addCommandResponder(authenticationFailureLogger);

                nextDispatcher.addMessageProcessingModel(new MPv3(usm));
                // Give the dispatcher NoOp message processors for other SNMP versions to avoid
                // logs complaining about unsupported versions
                nextDispatcher.addMessageProcessingModel(new NoOpMessageProcessor(MessageProcessingModel.MPv1));
                nextDispatcher.addMessageProcessingModel(new NoOpMessageProcessor(MessageProcessingModel.MPv2c));
                // Use the same trap notifier
                nextDispatcher.addCommandResponder(trapNotifier);
                transport.addTransportListener(nextDispatcher);
            }
        }

        Snmp4JStrategy.trackSession(snmp);
        snmp.addCommandResponder(trapNotifier);
        info.setSession(snmp);
        
        s_registrations.put(listener, info);
        
        snmp.listen();
    }

    private static class AuthenticationFailureLogger implements AuthenticationFailureListener, CommandResponder {
        private int currentDispatcher, maxDispatcher;

        private AuthenticationFailureLogger(final int currentDispatcher, final int maxDispatchers) {
            this.currentDispatcher = currentDispatcher;
            this.maxDispatcher = maxDispatchers;
        }

        @Override
        public void authenticationFailure(final AuthenticationFailureEvent authenticationFailureEvent) {
            final MessageDispatcher messageDispatcher = (MessageDispatcher) authenticationFailureEvent.getSource();
            // the dispatchers for different credentials have only the v3 processing model assigned, so we can check whether the v2 processing model is null
            if (messageDispatcher.getMessageProcessingModel(MPv2c.ID) == null) {
                LOG.debug("Error authenticating SNMP v3 trap PDU received from {} (error status code {}). Tried #{} of {} different dispatchers.", authenticationFailureEvent.getAddress(), authenticationFailureEvent.getError(), currentDispatcher, maxDispatcher);
            }
        }

        @Override
        public void processPdu(final CommandResponderEvent event) {
            if (event.getMessageProcessingModel() == MPv3.ID) {
                LOG.debug("Authenticated SNMP v3 trap PDU received from {}. Tried #{} of {} different dispatchers.", event.getPeerAddress(), currentDispatcher, maxDispatcher);
            }
        }
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, InetAddress address, int snmpTrapPort) throws IOException {
        registerForTraps(listener, address, snmpTrapPort, null);
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, final int snmpTrapPort) throws IOException {
    	registerForTraps(listener, null, snmpTrapPort);
    }

    @Override
    public void unregisterForTraps(final TrapNotificationListener listener) throws IOException {
        final RegistrationInfo info = s_registrations.remove(listener);
        final Snmp session = info.getSession();
        try {
            session.close();
        } catch (final IOException e) {
            LOG.error("session error unregistering for traps", e);
            throw e;
        } finally {
            Snmp4JStrategy.reapSession(session);
        }
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
        config.setReadCommunity(community);
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
			
		if (! (pdu instanceof ScopedPDU)) {
		    throw new SnmpException("PDU is not a ScopedPDU (this should not happen)");
		}

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
                final Snmp snmp = info.getSession();
                Snmp4JStrategy.trackSession(snmp);
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

    private static void closeQuietly(Snmp session) {
        if (session == null) {
            return;
        }
        
        try {
            session.close();
        } catch (IOException e) {
            LOG.error("error closing SNMP connection", e);
        } finally {
            Snmp4JStrategy.reapSession(session);
        }
    }

    public static OctetString createPersistentInstanceId() {
        String instanceId = SystemInfoUtils.getInstanceId();
        // Limit this instance to 23 bytes.
        if (instanceId.length() > 24) {
            instanceId = instanceId.substring(0, 23);
        }
        return new OctetString(instanceId);
    }

    public static OctetString createLocalEngineId() {
        return new OctetString(MPv3.createLocalEngineID(createPersistentInstanceId()));
    }

    @Override
    public byte[] getLocalEngineID() {
        return createLocalEngineId().getValue();
    }

        private static void assertTrackingInitialized() {
            if (s_sessions == null) {
                s_sessions = new ConcurrentHashMap<>();
                s_sessionStatsExecutor = Executors.newSingleThreadScheduledExecutor();
                s_sessionStatsExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override public void run() {
                        logSessionStats();
                    }
                }, s_trackSummaryDelay, s_trackSummaryDelay, TimeUnit.SECONDS);
            }
        }

        public static void trackSession(final Snmp session) {
            if (!s_trackSessions || session == null) return;
            Logging.withPrefix("snmp", () -> {
                assertTrackingInitialized();
                if (s_sessions.containsKey(session)) {
                    LOG.warn("track: session {} is already tracked -- overwriting", s_sessions.get(session));
                }
                final SessionInfo ts = new SessionInfo(session);
                LOG.debug("track: tracking session {}", ts);
                s_sessions.put(session, ts);
            });
        }

        public static void reapSession(final Snmp session) {
            if (!s_trackSessions || session == null) return;
            Logging.withPrefix("snmp", () -> {
                assertTrackingInitialized();
                if (!s_sessions.containsKey(session)) {
                    LOG.warn("reap: session {} is not being tracked", session, new Exception());
                } else {
                    LOG.debug("reap: reaping session {}", s_sessions.get(session));
                }
                s_sessions.remove(session);
            });
        }

        private static void logSessionStats() {
            LOG.debug("SNMP session tracker: {} sessions being tracked on {} unique threads", s_sessions.size(), s_sessions.values().stream().map(si -> {
                return si.getThread();
            }).distinct().count());
            s_sessions.values().stream().sorted(new Comparator<SessionInfo>() {
                @Override
                public int compare(final SessionInfo o1, final SessionInfo o2) {
                    return o1.getStart().compareTo(o2.getStart());
                }
            }).limit(s_trackSummaryLimit).forEach((si) -> {
                LOG.debug("SNMP session tracker: active session: {}", si);
            });
        }

        private static class SessionInfo implements Comparable<SessionInfo> {
            private final Snmp m_session;
            private final StackTraceElement[] m_stackTrace;
            private final Thread m_thread;
            private final LocalDateTime m_start;

            public SessionInfo(final Snmp session) {
                m_session = session;
                m_stackTrace = new Exception().getStackTrace();
                m_thread = Thread.currentThread();
                m_start = LocalDateTime.now();
            }

            public Thread getThread() {
                return m_thread;
            }

            public LocalDateTime getStart() {
                return m_start;
            }

            public String getOutsideCaller() {
                for (final StackTraceElement ste : m_stackTrace) {
                    final String name = ste.getClassName();
                    if (!name.startsWith("org.opennms.netmgt.snmp.snmp4j") && !name.startsWith("org.opennms.core.logging") && !name.startsWith("org.opennms.netmgt.snmp.SnmpUtils")) {
                        return ste.toString();
                    }
                }
                LOG.warn("unable to determine non-snmp4j caller from stack trace: {}", Arrays.asList(m_stackTrace));
                return m_stackTrace[0].toString();
            }
            @Override
            public int hashCode() {
                return Objects.hash(m_session, m_thread);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final SessionInfo that = (SessionInfo) obj;
                return Objects.equals(this.m_session, that.m_session) &&
                        Objects.equals(this.m_stackTrace, that.m_stackTrace) &&
                        Objects.equals(this.m_thread, that.m_thread);
            }

            @Override
            public int compareTo(final SessionInfo that) {
                if (this.m_session.hashCode() < that.m_session.hashCode()) {
                    return -1;
                } else if (this.m_session.hashCode() > that.m_session.hashCode()) {
                    return 1;
                }
                if (this.m_stackTrace.hashCode() < that.m_stackTrace.hashCode()) {
                    return -1;
                } else if (this.m_stackTrace.hashCode() > that.m_stackTrace.hashCode()) {
                    return 1;
                }
                if (this.m_thread.hashCode() < that.m_thread.hashCode()) {
                    return -1;
                } else if (this.m_thread.hashCode() > that.m_thread.hashCode()) {
                    return 1;
                }
                return 0;
            }

            @Override
            public String toString() {
                return "SessionInfo[session=" + m_session + ", caller=" + getOutsideCaller() + ", thread=" + m_thread.getName() + ", age=" + Duration.between(m_start, LocalDateTime.now()).getSeconds() + "s]";
            }
        }

    /**
     * A message processor that does nothing but return OK statuses
     */
    private static class NoOpMessageProcessor implements MessageProcessingModel {

            private int id;

            private NoOpMessageProcessor(int id) {
                this.id = id;
            }

            @Override
            public int getID() {
                return this.id;
            }

            @Override
            public int prepareOutgoingMessage(Address transportAddress,
                                       int maxMsgSize,
                                       int messageProcessingModel,
                                       int securityModel,
                                       byte[] securityName,
                                       int securityLevel,
                                       PDU pdu,
                                       boolean expectResponse,
                                       PduHandle sendPduHandle,
                                       Address destTransportAddress,
                                       BEROutputStream outgoingMessage,
                                       TransportStateReference tmStateReference)
                    throws IOException {
                return SnmpConstants.SNMP_MP_OK;
            }

            @Override
            public int prepareResponseMessage(int messageProcessingModel,
                                       int maxMsgSize,
                                       int securityModel,
                                       byte[] securityName,
                                       int securityLevel,
                                       PDU pdu,
                                       int maxSizeResponseScopedPDU,
                                       StateReference stateReference,
                                       StatusInformation statusInformation,
                                       BEROutputStream outgoingMessage)
                    throws IOException {
                return SnmpConstants.SNMP_MP_OK;
            }

            @Override
            public int prepareDataElements(MessageDispatcher messageDispatcher,
                                    Address transportAddress,
                                    BERInputStream wholeMsg,
                                    TransportStateReference tmStateReference,
                                    Integer32 messageProcessingModel,
                                    Integer32 securityModel,
                                    OctetString securityName,
                                    Integer32 securityLevel,
                                    MutablePDU pdu,
                                    PduHandle sendPduHandle,
                                    Integer32 maxSizeResponseScopedPDU,
                                    StatusInformation statusInformation,
                                    MutableStateReference mutableStateReference)
                    throws IOException {
                return SnmpConstants.SNMP_MP_OK;
            }

            @Override
            public boolean isProtocolVersionSupported(int snmpProtocolVersion) {
                return snmpProtocolVersion == getID();
            }

            @Override
            public void releaseStateReference(PduHandle pduHandle) {}
        }
}
