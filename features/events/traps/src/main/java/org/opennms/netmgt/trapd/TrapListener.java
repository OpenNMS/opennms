/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.trapd;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.snmp.SnmpException;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TrapListener implements TrapNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrapListener.class);

    /**
     * Name of the dedicated audit logger that records one line for every trap
     * handed to this listener. It is shipped disabled; see trap-audit in
     * log4j2.xml (core) or org.ops4j.pax.logging.cfg (Minion) to enable it.
     */
    public static final String AUDIT_LOGGER_NAME = "trap-audit";

    private static final Logger AUDIT = LoggerFactory.getLogger(AUDIT_LOGGER_NAME);

    // True if trap configuration has been received from the core instance
    private boolean m_configured = false;
    private Object configuredLock = new Object();

    private long subscriberTimeoutMs = 60 * 1000;

    @Autowired
    private MessageDispatcherFactory m_messageDispatcherFactory;

    @Autowired
    private DistPollerDao m_distPollerDao;

    private boolean m_registeredForTraps;

    private TrapdConfig m_config;

    private AsyncDispatcher<TrapInformationWrapper> m_dispatcher;

    @Autowired
    private TwinSubscriber m_twinSubscriber;

    private Closeable m_twinSubscription;

    private TrapListenerMetrics m_listenerMetrics;

    public TrapListener(final TrapdConfig config) throws SocketException {
        Objects.requireNonNull(config, "Config cannot be null");
        m_config = config;
    }

    @Override
    public void trapReceived(TrapInformation trapInformation) {
        final InetAddress trapAddress = trapInformation.getTrapAddress();

        // Count raw trap at listener level before any processing
        if (m_listenerMetrics != null) {
            m_listenerMetrics.incRawTrapsReceivedCount(trapAddress);
        }
        TrapSinkConsumer.trapdInstrumentation.incRawTrapsReceivedCount();

        // The wrapper constructor validates the PDU, so a trap that gets past it is decoded and well formed
        final TrapInformationWrapper wrapper;
        try {
            wrapper = new TrapInformationWrapper(trapInformation);
        } catch (final SnmpException | IllegalArgumentException ex) {
            LOG.error("Received trap {} is not valid and cannot be processed. The trap will be dropped.", trapInformation, ex);
            audit("REJECTED", trapInformation, ex);
            // This trap will never reach the sink consumer
            if (m_listenerMetrics != null) {
                m_listenerMetrics.incErrorCount(trapAddress);
            }
            TrapSinkConsumer.trapdInstrumentation.incErrorCount();
            return;
        }

        // RECEIVED means decoded and valid; a hand-off failure is reported separately as FORWARD_FAILED
        audit("RECEIVED", trapInformation, null);

        try {
            getMessageDispatcher().send(wrapper)
                    .whenComplete((t,ex) -> {
                        if (ex != null) {
                            LOG.error("An error occurred while forwarding trap {} for further processing. The trap will be dropped.", trapInformation, ex);
                            audit("FORWARD_FAILED", trapInformation, ex);
                            // This trap will never reach the sink consumer
                            if (m_listenerMetrics != null) {
                                m_listenerMetrics.incErrorCount(trapAddress);
                            }
                            TrapSinkConsumer.trapdInstrumentation.incErrorCount();
                        }
                    });
        } catch (final RuntimeException ex) {
            // Dispatcher could not be created or used, for example because it is not yet bound
            LOG.error("An error occurred while forwarding trap {} for further processing. The trap will be dropped.", trapInformation, ex);
            audit("FORWARD_FAILED", trapInformation, ex);
            // This trap will never reach the sink consumer
            if (m_listenerMetrics != null) {
                m_listenerMetrics.incErrorCount(trapAddress);
            }
            TrapSinkConsumer.trapdInstrumentation.incErrorCount();
        }
    }

    /**
     * Writes one line to the trap audit log. This runs on the SNMP receive thread,
     * so it must be cheap when the logger is off and must never throw.
     */
    private void audit(final String outcome, final TrapInformation trapInformation, final Throwable cause) {
        if (!AUDIT.isInfoEnabled()) {
            return;
        }
        try {
            if (cause == null) {
                AUDIT.info("TRAP {} {}", outcome, describeForAudit(trapInformation));
            } else {
                AUDIT.info("TRAP {} {} reason={}", outcome, describeForAudit(trapInformation), auditReason(cause));
            }
        } catch (final RuntimeException e) {
            LOG.warn("Failed to write trap audit entry for trap from {}", trapInformation.getAgentAddress(), e);
        }
    }

    /**
     * Builds the key=value description shared by every audit line:
     * the UDP peer, the address the trap will be attributed to, the SNMP version,
     * the security name for v3, the trap OID and the number of varbinds.
     * Fields that cannot be derived from a malformed PDU are reported as "unknown".
     */
    private String describeForAudit(final TrapInformation trapInformation) {
        final StringBuilder sb = new StringBuilder(160);
        sb.append("src=").append(auditAddress(trapInformation.getAgentAddress()));
        sb.append(" agent=").append(auditAddress(auditEffectiveTrapAddress(trapInformation)));
        final String version = trapInformation.getVersion();
        sb.append(" version=").append(version);
        if ("v3".equals(version)) {
            sb.append(" user=").append(auditText(trapInformation.getCommunity()));
        }
        sb.append(" oid=").append(auditTrapOid(trapInformation));
        sb.append(" varbinds=").append(trapInformation.getPduLength());
        return sb.toString();
    }

    private InetAddress auditEffectiveTrapAddress(final TrapInformation trapInformation) {
        try {
            return TrapUtils.getEffectiveTrapAddress(trapInformation, m_config.shouldUseAddressFromVarbind());
        } catch (final RuntimeException e) {
            return trapInformation.getTrapAddress();
        }
    }

    /**
     * The trap OID as sent: the snmpTrapOID.0 value for v2c/v3, and for v1 the OID derived
     * from enterprise, generic and specific. {@link TrapIdentity#getTrapOID()} is deliberately
     * not used for v2c/v3 because it replaces the OID of standard traps with the
     * snmpTrapEnterprise.0 value when that varbind is present.
     */
    private static String auditTrapOid(final TrapInformation trapInformation) {
        try {
            final SnmpObjId received = trapInformation.getSnmpTrapOid();
            if (received != null) {
                return received.toString();
            }
            if ("v1".equals(trapInformation.getVersion())) {
                final String derived = trapInformation.getTrapIdentity().getTrapOID();
                return derived == null ? "unknown" : derived;
            }
        } catch (final RuntimeException e) {
            // Malformed PDUs (for example fewer than two varbinds) cannot yield an identity
        }
        return "unknown";
    }

    private static String auditAddress(final InetAddress address) {
        return address == null ? "unknown" : InetAddressUtils.str(address);
    }

    /**
     * Every audit entry must stay on one physical line. Validation messages from the
     * SNMP layer contain embedded newlines and tabs, so free text is collapsed to single spaces.
     */
    private static String auditText(final String text) {
        return text == null ? "unknown" : text.replaceAll("\\s+", " ").trim();
    }

    private static String auditReason(final Throwable cause) {
        final String message = cause.getMessage();
        return auditText(message == null || message.isEmpty() ? cause.getClass().getSimpleName() : message);
    }

    @Override
    public void trapError(int error, String msg) {
      LOG.warn("Error Processing Received Trap: error = {} {}", error, (msg != null ? ", ref = " + msg : ""));
    }

    public void start() {
        if (m_twinSubscriber != null) {
            subscribe();
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    delayedConfigurationCheck();
                }
            }, subscriberTimeoutMs); // wait 60 seconds for connection from core
        }
    }

    private void delayedConfigurationCheck() {
        synchronized (configuredLock) {
            if (!m_configured) {
                LOG.warn("No trap configuration received from core, using default settings");
                this.open(new TrapListenerConfig());
            }
        }
    }

    public void subscribe() {
        m_twinSubscription = m_twinSubscriber.subscribe(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class, (config) -> {
            try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Trapd.LOG4J_CATEGORY)) {
                LOG.info("Got listener config update - reloading");
                synchronized(configuredLock) {
                    m_configured = true;
                    this.close();
                    this.open(config);
                }
            }
        });
    }

    public void bind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = twinSubscriber;
        subscribe();
    }

    public void unbind(TwinSubscriber twinSubscriber) {
        m_twinSubscriber = null;
    }

    private void open(final TrapListenerConfig config) {
        final int m_snmpTrapPort = m_config.getSnmpTrapPort();
        final InetAddress address = getInetAddress();
        try {
            LOG.info("Listening on {}:{}", address == null ? "[all interfaces]" : InetAddressUtils.str(address), m_snmpTrapPort);
            SnmpUtils.registerForTraps(this, address, m_snmpTrapPort, config.getSnmpV3Users());
            m_registeredForTraps = true;
            
            LOG.debug("init: Creating the trap session");
        } catch (final IOException e) {
            if (e instanceof java.net.BindException) {
                Logging.withPrefix("OpenNMS.Manager", new Runnable() {
                    @Override
                    public void run() {
                        LOG.error("init: Failed to listen on SNMP trap port {}, perhaps something else is already listening?", m_snmpTrapPort, e);
                    }
                });
                LOG.error("init: Failed to listen on SNMP trap port {}, perhaps something else is already listening?", m_snmpTrapPort, e);
                throw new UndeclaredThrowableException(e, "Failed to listen on SNMP trap port " + m_snmpTrapPort + ", perhaps something else is already listening?");
            } else {
                LOG.error("init: Failed to initialize SNMP trap socket on port {}", m_snmpTrapPort, e);
                throw new UndeclaredThrowableException(e, "Failed to initialize SNMP trap socket on port " + m_snmpTrapPort);
            }
        }
    }

    public void stop() {
        try {
            m_twinSubscription.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.close();

        try {
            if (m_dispatcher != null) {
                // Clear the queue size supplier before closing
                if (m_listenerMetrics != null) {
                    m_listenerMetrics.setQueueSizeSupplier(null);
                }
                TrapSinkConsumer.trapdInstrumentation.setQueueSizeSupplier(null);
                m_dispatcher.close();
                m_dispatcher = null;
            }
        } catch (final Exception e) {
            LOG.warn("stop: exception occurred closing m_dispatcher", e);
        }
    }

    private void close() {
        try {
            if (m_registeredForTraps) {
                LOG.debug("stop: Closing SNMP trap session.");
                SnmpUtils.unregisterForTraps(this);
                m_registeredForTraps = false;
                LOG.debug("stop: SNMP trap session closed.");
            } else {
                LOG.debug("stop: not attempting to closing SNMP trap session--it was never opened or already closed.");
            }
        } catch (final IOException e) {
            LOG.warn("stop: exception occurred closing session", e);
        } catch (final IllegalStateException e) {
            LOG.debug("stop: The SNMP session was already closed", e);
        }
    }

    public void setTrapdConfig(TrapdConfiguration newTrapdConfig) {
        TrapdConfigBean newTrapdConfigBean = new TrapdConfigBean(newTrapdConfig);
        if (hasConfigurationChanged(newTrapdConfigBean)) {
            restartWithNewConfig(newTrapdConfigBean);
        }
    }

    public boolean isRegisteredForTraps() {
        return m_registeredForTraps;
    }

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        m_messageDispatcherFactory = Objects.requireNonNull(messageDispatcherFactory);
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = Objects.requireNonNull(distPollerDao);
    }

    public void setTrapListenerMetrics(TrapListenerMetrics listenerMetrics) {
        m_listenerMetrics = listenerMetrics;
    }

    private void restartWithNewConfig(final TrapdConfigBean newConfig) {
        // We stop, still using old config
        LOG.info("Stopping TrapListener service to reload configuration...");
        stop();
        LOG.info("TrapListener service has been stopped.");

        // Update config, instead of set it, to apply the changes to ALL config references
        m_config.update(newConfig);

        // We start with new config
        LOG.info("Restarting the TrapListener service...");
        start();
        LOG.info("TrapListener service has been restarted.");
    }

    public void reload() throws IOException {
        TrapdConfigFactory.reload();
        m_config = TrapdConfigFactory.getInstance();
    }

    private InetAddress getInetAddress() {
        if (m_config.getSnmpTrapAddress().equals("*")) {
            return null;
        }
        return InetAddressUtils.addr(m_config.getSnmpTrapAddress());
    }

    // We only want to create the messageDispatcher once
    private AsyncDispatcher<TrapInformationWrapper> getMessageDispatcher() {
        if (m_dispatcher == null) {
            Objects.requireNonNull(m_messageDispatcherFactory);
            m_dispatcher = m_messageDispatcherFactory.createAsyncDispatcher(new TrapSinkModule(m_config, m_distPollerDao.whoami()));

            // Register queue metrics with TrapListenerMetrics (for Minion JMX)
            if (m_listenerMetrics != null) {
                m_listenerMetrics.setMaxQueueSize(m_config.getQueueSize());
                m_listenerMetrics.setBatchSize(m_config.getBatchSize());
                m_listenerMetrics.setQueueSizeSupplier(m_dispatcher::getQueueSize);
            }
            // Register queue metrics with TrapdInstrumentation (for OpenNMS Core JMX)
            TrapSinkConsumer.trapdInstrumentation.setMaxQueueSize(m_config.getQueueSize());
            TrapSinkConsumer.trapdInstrumentation.setBatchSize(m_config.getBatchSize());
            TrapSinkConsumer.trapdInstrumentation.setQueueSizeSupplier(m_dispatcher::getQueueSize);
        }
        return m_dispatcher;
    }

    protected boolean hasConfigurationChanged(TrapdConfig newConfig) {
        if (newConfig.getSnmpTrapPort() != m_config.getSnmpTrapPort()) {
            LOG.info("SNMP trap port has been updated from trapd-configuration.xml.");
            return true;
        } else if (
                newConfig.getSnmpTrapAddress() != null
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase("*")
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase(m_config.getSnmpTrapAddress())) {
            LOG.info("SNMP trap address has been updated from trapd-configuration.xml.");
            return true;
        } else {
            return hasSnmpV3UsersChanged(newConfig);
        }
    }

    public boolean hasSnmpV3UsersChanged(TrapdConfig newConfig) {
        // If the new map is empty, do not update the configuration
        if (newConfig.getSnmpV3Users().isEmpty()) {
            return false;
        }
        return !newConfig.getSnmpV3Users().equals(m_config.getSnmpV3Users());
    }

    void setSubscriberTimeoutMs(long ms) {
        subscriberTimeoutMs = ms;
    }

    long getSubscriberTimeoutMs() {
        return subscriberTimeoutMs;
    }
}
