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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.snmp.BasicTrapProcessorFactory;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 * @fiddler joed
 */
public class TrapReceiverImpl implements TrapReceiver, TrapNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrapReceiverImpl.class);

    @Resource(name="snmpTrapAddress")
    private String m_snmpTrapAddress;

    @Resource(name="snmpTrapPort")
    private Integer m_snmpTrapPort;

    private final List<SnmpV3User> m_snmpV3Users = Collections.synchronizedList(new ArrayList<>());

    private boolean m_registeredForTraps;

    private List<TrapNotificationHandler> m_trapNotificationHandlers = new ArrayList<TrapNotificationHandler>();

    public void setTrapdConfig(TrapdConfiguration newTrapdConfig) {

        if (checkForTrapdConfigurationChange(newTrapdConfig)) {
            restartTrapReceiverAndRun(() -> {
                m_snmpTrapPort = newTrapdConfig.getSnmpTrapPort();
                m_snmpTrapAddress = newTrapdConfig.getSnmpTrapAddress();
                updateSnmpV3Users(newTrapdConfig);
            });
        }
    }

    public void setSnmpV3Users(final TrapdConfiguration newTrapdConfig) {
        if (checkForSnmpV3ConfigurationChange(newTrapdConfig)) {
            restartTrapReceiverAndRun(() -> {
                updateSnmpV3Users(newTrapdConfig);
            });
        }
    }

    private void updateSnmpV3Users(final TrapdConfiguration newTrapdConfig) {
        synchronized(m_snmpV3Users) {
            m_snmpV3Users.clear();
            if (newTrapdConfig.getSnmpv3UserCollection() != null) {
                m_snmpV3Users.addAll(newTrapdConfig.getSnmpv3UserCollection().stream().map(TrapReceiverImpl::toSnmpV3User).collect(Collectors.toList()));
            }
        }
    }

    private void restartTrapReceiverAndRun(final Runnable runnable) {
        LOG.info("Stopping TrapReceiver service to reload configuration...");
        stop();
        LOG.info("TrapReceiver service has been stopped.");

        runnable.run();

        LOG.info("Restarting the TrapReceiver service...");
        start();
        LOG.info("TrapReceiver service has been restarted.");
    }

    /**
     * @param existingSnmpV3UserMap
     * @param updatedSnmpV3Usermap
     */
    public static boolean isSnmpV3UsersMapUpdated(final Map<String, SnmpV3User> existingSnmpV3UserMap, final Map<String, SnmpV3User> updatedSnmpV3Usermap) {

        // If the new map is empty, do not update the configuration
        if (updatedSnmpV3Usermap.isEmpty()) {
            return false;
        } 

        return !existingSnmpV3UserMap.equals(updatedSnmpV3Usermap);
    }

    protected boolean checkForTrapdConfigurationChange(TrapdConfiguration trapdConfiguration) {
        if (trapdConfiguration.getSnmpTrapPort() != m_snmpTrapPort) {
            LOG.info("SNMP trap port has been updated from trapd-confguration.xml.");
            return true;
        } else if (
            trapdConfiguration.getSnmpTrapAddress() != null &&
            !trapdConfiguration.getSnmpTrapAddress().equalsIgnoreCase("*") &&
            !trapdConfiguration.getSnmpTrapAddress().equalsIgnoreCase(m_snmpTrapAddress)
        ) {
            LOG.info("SNMP trap address has been updated from trapd-confguration.xml.");
            return true;
        } else {
            return checkForSnmpV3ConfigurationChange(trapdConfiguration);
        }
    }

    protected boolean checkForSnmpV3ConfigurationChange(TrapdConfiguration trapdConfiguration) {
        final Map<String, SnmpV3User> newSnmpV3Users = getSnmpV3UserMap(trapdConfiguration);
        final Map<String, SnmpV3User> existingSnmpV3Users = m_snmpV3Users.stream().collect(Collectors.toMap(SnmpV3User::getSecurityName, Function.<SnmpV3User>identity(), (a,b) -> {
            LOG.warn("Multiple SNMPv3 user entries found for security name \"{}\", using entry {}", a.getSecurityName(), a);
            return a;
        }));

        if (isSnmpV3UsersMapUpdated(existingSnmpV3Users, newSnmpV3Users) ) {
            LOG.info("SNMPv3 user list has been updated from trapd-confguration.xml.");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Default constructor
     */
    public TrapReceiverImpl() {
    }

    /**
     * construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     */
    public TrapReceiverImpl(final TrapdConfig config) throws SocketException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        m_snmpTrapPort = config.getSnmpTrapPort();
        m_snmpTrapAddress = config.getSnmpTrapAddress();
        synchronized(m_snmpV3Users) {
            m_snmpV3Users.clear();
            m_snmpV3Users.addAll(config.getSnmpV3Users());
        }
    }

    public TrapNotificationHandler getTrapNotificationHandlers() {
        return m_trapNotificationHandlers.get(0);
    }

    public void setTrapNotificationHandlers(TrapNotificationHandler handler) {
        m_trapNotificationHandlers = Collections.singletonList(handler);
    }

    @Override
    public void trapReceived(TrapNotification trapNotification) {
        try {
            for (TrapNotificationHandler handler : m_trapNotificationHandlers) {
                handler.handleTrapNotification(trapNotification);
            }
        } catch (Throwable e) {
            LOG.error("Handler execution failed in {}", this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void trapError(int error, String msg) {
      LOG.warn("Error Processing Received Trap: error = {} {}", error, (msg != null ? ", ref = " + msg : ""));
    }

    @Override
    public void start(){
        try {
            InetAddress address = getInetAddress();
            LOG.info("Listening on {}:{}", address == null ? "[all interfaces]" : InetAddressUtils.str(address), m_snmpTrapPort);
            SnmpUtils.registerForTraps(this, new BasicTrapProcessorFactory(), address, m_snmpTrapPort, m_snmpV3Users); // Need to clarify
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

    @Override
    public void stop() {
        try {
            if (m_registeredForTraps) {
                LOG.debug("stop: Closing SNMP trap session.");
                SnmpUtils.unregisterForTraps(this, getInetAddress(), m_snmpTrapPort);
                LOG.debug("stop: SNMP trap session closed.");
            } else {
                LOG.debug("stop: not attemping to closing SNMP trap session--it was never opened");
            }

        } catch (final IOException e) {
            LOG.warn("stop: exception occurred closing session", e);
        } catch (final IllegalStateException e) {
            LOG.debug("stop: The SNMP session was already closed", e);
        }
    }

    private InetAddress getInetAddress() {
        if (m_snmpTrapAddress.equals("*")) {
            return null;
        }
        return InetAddressUtils.addr(m_snmpTrapAddress);
    }

    /**
     * This method converts a list of {@link Snmpv3User} objects into a
     * {@link Map} of {@link String} to {@link SnmpV3User}.
     * 
     * @param config
     * @return
     */
    private static Map<String, SnmpV3User> getSnmpV3UserMap(TrapdConfiguration config) {
        if(config.getSnmpv3UserCollection() != null) {
            return config.getSnmpv3UserCollection().stream().collect(Collectors.toMap(Snmpv3User::getSecurityName, TrapReceiverImpl::toSnmpV3User, (a,b) -> {
                LOG.warn("Multiple SNMPv3 user entries found for security name \"{}\", using entry {}", a.getSecurityName(), a);
                return a;
            }));
        } else {
            return Collections.emptyMap();
        }
    }

    public static SnmpV3User toSnmpV3User(Snmpv3User snmpv3User) {
        SnmpV3User snmpV3User = new SnmpV3User();
        snmpV3User.setAuthPassPhrase(snmpv3User.getAuthPassphrase());
        snmpV3User.setAuthProtocol(snmpv3User.getAuthProtocol());
        snmpV3User.setEngineId(snmpv3User.getEngineId());
        snmpV3User.setPrivPassPhrase(snmpv3User.getPrivacyPassphrase());
        snmpV3User.setPrivProtocol(snmpv3User.getPrivacyProtocol());
        snmpV3User.setSecurityName(snmpv3User.getSecurityName());
        return snmpV3User;
    }
}
