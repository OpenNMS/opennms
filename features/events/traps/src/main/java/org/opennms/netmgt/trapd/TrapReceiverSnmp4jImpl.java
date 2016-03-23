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
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 * @fiddler joed
 */
public class TrapReceiverSnmp4jImpl implements TrapReceiver, TrapNotificationListener, TrapProcessorFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TrapReceiverSnmp4jImpl.class);

    @Resource(name="snmpTrapAddress")
    private String m_snmpTrapAddress;

    @Resource(name="snmpTrapPort")
    private Integer m_snmpTrapPort;

    @Resource(name="snmpV3Users")
    private List<SnmpV3User> m_snmpV3Users;

    private boolean m_registeredForTraps;

    /**
     * Trapd IP manager.  Contains IP address -> node ID mapping.
     * 
     * TODO: Get rid of the requirement to have this here by creating a different
     * TrapProcessorFactory.
     */
    @Autowired
    private TrapdIpMgr m_trapdIpMgr;

    private List<TrapNotificationHandler> m_trapNotificationHandlers = Collections.emptyList();

    /**
     * construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     */
    public TrapReceiverSnmp4jImpl(final TrapdConfig config) throws SocketException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_snmpTrapPort = config.getSnmpTrapPort();
        m_snmpTrapAddress = config.getSnmpTrapAddress();
        m_snmpV3Users = config.getSnmpV3Users();
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
            SnmpUtils.registerForTraps(this, this, address, m_snmpTrapPort, m_snmpV3Users); // Need to clarify 
            m_registeredForTraps = true;
            
            LOG.debug("init: Creating the trap session");
        } catch (final IOException e) {
            if (e instanceof java.net.BindException) {
                Logging.withPrefix("OpenNMS.Manager", new Runnable() {
                    @Override
                    public void run() {
                        LOG.error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
                    }
                });
                LOG.error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
            } else {
                LOG.error("init: Failed to initialize SNMP trap socket", e);
            }
            throw new UndeclaredThrowableException(e);
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
     * TODO: Get rid of the requirement to have this here by creating a different
     * TrapProcessorFactory.
     */
    @Override
    public TrapProcessor createTrapProcessor() {
        return new EventCreator(m_trapdIpMgr);
    }

}
