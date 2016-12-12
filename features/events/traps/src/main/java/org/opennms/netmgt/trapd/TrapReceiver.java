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
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.opennms.core.ipc.sink.api.MessageProducer;
import org.opennms.core.ipc.sink.api.MessageProducerFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.trapd.mapper.TrapInformation2TrapDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TrapReceiver implements TrapNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrapReceiver.class);

    // For test purposes only
    @Resource(name="snmpTrapAddress")
    private String m_snmpTrapAddress;

    // For test purposes only
    @Resource(name="snmpTrapPort")
    private Integer m_snmpTrapPort;

    @Autowired
    private MessageProducerFactory m_messageProducerFactory;

    @Autowired
    private DistPollerDao m_distPollerDao;

    private boolean m_registeredForTraps;

    private TrapdConfig config;

    private MessageProducer<TrapDTO> m_producer;

    public TrapReceiver(final TrapdConfig config) throws SocketException {
        Objects.requireNonNull(config, "Config cannot be null");
        this.config = config;
    }

    // This is for test purposes only and should be addressable with custom spring configuration.
    // This only works if we update to Spring 4.2. See https://jira.spring.io/browse/SPR-9567.
    @PostConstruct
    public void init() {
        TrapdConfigBean config = new TrapdConfigBean(this.config);
        config.setSnmpTrapAddress(m_snmpTrapAddress);
        config.setSnmpTrapPort(m_snmpTrapPort);
        this.config = config; // overwrite config
    }

    @Override
    public void trapReceived(TrapInformation trapInformation) {
        TrapDTO trapDTO = new TrapInformation2TrapDtoMapper(m_distPollerDao).object2dto(trapInformation);
        getMessageProducer().send(trapDTO);
    }

    @Override
    public void trapError(int error, String msg) {
      LOG.warn("Error Processing Received Trap: error = {} {}", error, (msg != null ? ", ref = " + msg : ""));
    }

    public void start() {
        final int m_snmpTrapPort = config.getSnmpTrapPort();
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
            if (m_registeredForTraps) {
                LOG.debug("stop: Closing SNMP trap session.");
                SnmpUtils.unregisterForTraps(this, getInetAddress(), config.getSnmpTrapPort());
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

    public void setTrapdConfig(TrapdConfiguration newTrapdConfig) {
        TrapdConfigBean newTrapdConfigBean = new TrapdConfigBean(newTrapdConfig);
        if (hasConfigurationChanged(newTrapdConfigBean)) {
            restartWithNewConfig(newTrapdConfigBean);
        }
    }

    public void setSnmpV3Users(final TrapdConfiguration newTrapdConfig) {
        TrapdConfigBean newTrapdConfigBean = new TrapdConfigBean(newTrapdConfig);
        if (hasSnmpV3UsersChanged(newTrapdConfigBean)) {
            TrapdConfigBean clone = new TrapdConfigBean(this.config);
            clone.setSnmpV3Users(newTrapdConfigBean.getSnmpV3Users());
            restartWithNewConfig(clone);
        }
    }

    public void setMessageProducerFactory(MessageProducerFactory messageProducerFactory) {
        m_messageProducerFactory = Objects.requireNonNull(messageProducerFactory);
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = Objects.requireNonNull(distPollerDao);
    }

    private void restartWithNewConfig(final TrapdConfigBean newConfig) {
        // We stop, still using old config
        LOG.info("Stopping TrapReceiver service to reload configuration...");
        stop();
        LOG.info("TrapReceiver service has been stopped.");

        // We set new config
        this.config = newConfig;

        // We start with new config
        LOG.info("Restarting the TrapReceiver service...");
        start();
        LOG.info("TrapReceiver service has been restarted.");
    }

    private InetAddress getInetAddress() {
        if (config.getSnmpTrapAddress().equals("*")) {
            return null;
        }
        return InetAddressUtils.addr(config.getSnmpTrapAddress());
    }

    private MessageProducer getMessageProducer() {
        if (m_producer == null) {
            Objects.requireNonNull(m_messageProducerFactory);
            m_producer = m_messageProducerFactory.getProducer(new TrapSinkModule(config));
        }
        return m_producer;
    }

    protected boolean hasConfigurationChanged(TrapdConfig newConfig) {
        if (newConfig.getSnmpTrapPort() != config.getSnmpTrapPort()) {
            LOG.info("SNMP trap port has been updated from trapd-confguration.xml.");
            return true;
        } else if (
                newConfig.getSnmpTrapAddress() != null
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase("*")
                        && !newConfig.getSnmpTrapAddress().equalsIgnoreCase(config.getSnmpTrapAddress())) {
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
        return !newConfig.getSnmpV3Users().equals(config.getSnmpV3Users());
    }
}
