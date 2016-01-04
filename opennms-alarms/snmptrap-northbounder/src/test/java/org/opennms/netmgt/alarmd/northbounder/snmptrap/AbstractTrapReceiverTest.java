/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapNotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Test Class for SnmpTrapNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractTrapReceiverTest implements TrapNotificationListener {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTrapReceiverTest.class);

    /** The Constant TRAP_PORT. */
    protected static final int TRAP_PORT = 1162;

    /** The trap receiver address. */
    protected static final InetAddress TRAP_DESTINATION = InetAddressUtils.getLocalHostAddress();

    /** The traps received. */
    private int trapsReceived;

    /**
     * Sets up the test (initialize a trap listener).
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        System.setProperty("opennms.home", "src/test/resources");
        SnmpPeerFactory.init();
        trapsReceived = 0;
        Assert.assertEquals("Snmp4JStrategy", SnmpUtils.getStrategy().getClass().getSimpleName());

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(TRAP_DESTINATION);
        SnmpV3User user = new SnmpV3User(config.getSecurityName(), config.getAuthProtocol(), config.getAuthPassPhrase(), config.getPrivProtocol(), config.getPrivPassPhrase());
        SnmpUtils.registerForTraps(this, new NullTrapProcessorFactory(), TRAP_DESTINATION, TRAP_PORT, Collections.singletonList(user));
        LOG.info("Registered Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT);
    }

    /**
     * Tears down the test (shutdown the trap listener)
     */
    @After
    public void tearDown() {
        try {
            SnmpUtils.unregisterForTraps(this, TRAP_DESTINATION, TRAP_PORT);
            LOG.info("Unregistered Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT);
        } catch (IOException e) {
            Assert.fail();
            LOG.error("Can't unregister Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT, e);
        } finally {
            MockLogAppender.assertNoWarningsOrGreater();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TrapNotificationListener#trapReceived(org.opennms.netmgt.snmp.TrapNotification)
     */
    @Override
    public void trapReceived(TrapNotification trapNotification) {
        trapsReceived++;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TrapNotificationListener#trapError(int, java.lang.String)
     */
    @Override
    public void trapError(int error, String msg) {
        Assert.fail(msg);
    }

    /**
     * Gets the traps received count.
     *
     * @return the traps received count
     */
    protected int getTrapsReceivedCount() {
        return trapsReceived;
    }

    /**
     * Reset traps received.
     */
    protected void resetTrapsReceived() {
        trapsReceived = 0;
    }
}
