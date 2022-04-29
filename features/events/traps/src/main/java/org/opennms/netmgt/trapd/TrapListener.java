/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;

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
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TrapListener implements TrapNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrapListener.class);

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

    public TrapListener(final TrapdConfig config) throws SocketException {
        Objects.requireNonNull(config, "Config cannot be null");
        m_config = config;
    }

    @Override
    public void trapReceived(TrapInformation trapInformation) {
        try {
            getMessageDispatcher().send(new TrapInformationWrapper(trapInformation))
                    .whenComplete((t,ex) -> {
                        if (ex != null) {
                            LOG.error("An error occured while forwarding trap {} for further processing. The trap will be dropped.", trapInformation, ex);
                            // This trap will never reach the sink consumer
                            TrapSinkConsumer.trapdInstrumentation.incErrorCount();
                        }
                    });
        } catch (final SnmpException | IllegalArgumentException ex) {
            LOG.error("Received trap {} is not valid and cannot be processed. The trap will be dropped.", trapInformation, ex);
            // This trap will never reach the sink consumer
            TrapSinkConsumer.trapdInstrumentation.incErrorCount();
        }
    }

    @Override
    public void trapError(int error, String msg) {
      LOG.warn("Error Processing Received Trap: error = {} {}", error, (msg != null ? ", ref = " + msg : ""));
    }

    public void start() {
        if (m_twinSubscriber != null) {
            subscribe();
        } else {
            open(new TrapListenerConfig());
        }
    }

    public void subscribe() {
        m_twinSubscription = m_twinSubscriber.subscribe(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class, (config) -> {
            try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Trapd.LOG4J_CATEGORY)) {
                LOG.info("Got listener config update - reloading");
                this.close();
                this.open(config);
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
                LOG.debug("stop: not attemping to closing SNMP trap session--it was never opened or already closed.");
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

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        m_messageDispatcherFactory = Objects.requireNonNull(messageDispatcherFactory);
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = Objects.requireNonNull(distPollerDao);
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
        }
        return m_dispatcher;
    }

    protected boolean hasConfigurationChanged(TrapdConfig newConfig) {
        if (newConfig.getSnmpTrapPort() != m_config.getSnmpTrapPort()) {
            LOG.info("SNMP trap port has been updated from trapd-confguration.xml.");
            return true;
        } else if (
                newConfig.getSnmpTrapAddress() != null
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase("*")
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase(m_config.getSnmpTrapAddress())) {
            LOG.info("SNMP trap address has been updated from trapd-confguration.xml.");
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
}
