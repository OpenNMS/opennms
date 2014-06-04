/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.snmp4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4jTrapReceiverTest extends MockSnmpAgentTestCase implements TrapProcessorFactory, CommandResponder {
    private static final Logger LOG = LoggerFactory.getLogger(Snmp4jTrapReceiverTest.class);

    private final Snmp4JStrategy m_strategy = new Snmp4JStrategy();

    private InetAddress m_addr = InetAddressUtils.getLocalHostAddress();

    private int m_trapCount;

    @Before
    public void resetTrapCount() {
        m_trapCount = 0;
    }

    /*
     * IMPORTANT:
     *
     * The sentence <code>snmp.getUSM().addUser(...)</code>, is the only requirement
     * in order to properly process SNMPv3 traps.
     * 
     * This is related with the credentials that should be created for Trapd in order
     * to properly authenticate and/or decode SNMPv3 traps in OpenNMS.
     * 
     * This is a user that should be configured (or should be used) by the external
     * devices to send SNMPv3 Traps to OpenNMS.
     * 
     * The SNMPv3 users should be configured in trapd-configuration.xml
     */
    @Test
    public void testTrapReceiverWithoutOpenNMS() throws Exception {
        assertEquals(0, m_trapCount);
        LOG.debug("SNMP4J: Register for Traps");
        DefaultUdpTransportMapping transportMapping = null;
        Snmp snmp = null;

        try {
            transportMapping = new DefaultUdpTransportMapping(new UdpAddress(9162));
            snmp = new Snmp(transportMapping);

            snmp.addCommandResponder(this);
            snmp.getUSM().addUser(
                new OctetString("opennmsUser"),
                new UsmUser(
                    new OctetString("opennmsUser"),
                    AuthMD5.ID,
                    new OctetString("0p3nNMSv3"),
                    PrivDES.ID,
                    new OctetString("0p3nNMSv3")
                )
            );

            long start = System.currentTimeMillis();
            snmp.listen();
            sendTraps();
            long waitUntil = System.currentTimeMillis() + 30000L;
            do {
                Thread.sleep(200);
                System.err.print(".");
                if (m_trapCount == 2) break;
            } while (System.currentTimeMillis() < waitUntil);
            System.err.println("");
            LOG.debug("waited for {} milliseconds", System.currentTimeMillis() - start);
        } finally {
            LOG.debug("SNMP4J: Unregister for Traps");
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (final IOException e) {
                    LOG.debug("Failed to close Snmp object: {}", snmp, e);
                }
            }
            if (transportMapping != null) {
                try {
                    transportMapping.close();
                } catch (final IOException e) {
                    LOG.debug("Failed to close transport mapping: {}", transportMapping, e);
                }
            }
        }

        LOG.debug("SNMP4J: Checking Trap status");
        assertEquals(2, m_trapCount);
    }

    @Test
    public void testTrapReceiverWithOpenNMS() {
        assertEquals(0, m_trapCount);
        LOG.debug("ONMS: Register for Traps");
        final TestTrapListener trapListener = new TestTrapListener();
        SnmpV3User user = new SnmpV3User("opennmsUser", "MD5", "0p3nNMSv3", "DES", "0p3nNMSv3");
        try {
            long start = System.currentTimeMillis();

            m_strategy.registerForTraps(trapListener, this, m_addr, 9162, Collections.singletonList(user));
            sendTraps();

            long waitUntil = System.currentTimeMillis() + 30000L;
            do {
                Thread.sleep(200);
                System.err.print(".");
                if (m_trapCount == 2) break;
            } while (System.currentTimeMillis() < waitUntil);
            System.err.println("");
            LOG.debug("waited for {} milliseconds", System.currentTimeMillis() - start);
        } catch (final IOException e) {
            LOG.debug("Failed to register for traps.", e);
        } catch (final Exception e) {
            LOG.debug("Failed to send traps.", e);
        } finally {
            LOG.debug("ONMS: Unregister for Traps");
            try {
                m_strategy.unregisterForTraps(trapListener, 9162);
            } catch (final IOException e) {
                LOG.debug("Failed to unregister for traps.", e);
            }
        }

        LOG.debug("ONMS: Checking Trap status");
        assertFalse(trapListener.hasError());
        assertEquals(2, trapListener.getReceivedTrapCount());
    }

    @Override
    public TrapProcessor createTrapProcessor() {
        return new TestTrapProcessor();
    }

    @Override
    protected boolean usingMockStrategy() {
        return false;
    }

    private void sendTraps() throws Exception {
        final String hostAddress = InetAddressUtils.str(m_addr);

        LOG.debug("Sending V2 Trap");
        SnmpObjId enterpriseId = SnmpObjId.get(".0.0");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        SnmpTrapBuilder pdu = m_strategy.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), m_strategy.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), m_strategy.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), m_strategy.getValueFactory().getObjectId(enterpriseId));
        pdu.send(hostAddress, 9162, "public");
        
        //Thread.sleep(10000);

        LOG.debug("Sending V3 Trap");
        SnmpV3TrapBuilder pduv3 = m_strategy.getV3TrapBuilder();
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), m_strategy.getValueFactory().getTimeTicks(0));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), m_strategy.getValueFactory().getObjectId(trapOID));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), m_strategy.getValueFactory().getObjectId(enterpriseId));
        pduv3.send(hostAddress, 9162, SnmpConfiguration.AUTH_PRIV, "opennmsUser", "0p3nNMSv3", SnmpConfiguration.DEFAULT_AUTH_PROTOCOL, "0p3nNMSv3", SnmpConfiguration.DEFAULT_PRIV_PROTOCOL);
    }

    @Override
    public synchronized void processPdu(final CommandResponderEvent cmdRespEvent) {
        final PDU pdu = cmdRespEvent.getPDU();
        LOG.debug("Received PDU... " + pdu);

        if (pdu != null) {
            LOG.debug(pdu.getClass().getName());
            LOG.debug("trapType = " + pdu.getType());
            LOG.debug("isPDUv1 = " + (pdu instanceof PDUv1));
            LOG.debug("isTrap = " + (pdu.getType() == PDU.TRAP));
            LOG.debug("isInform = " + (pdu.getType() == PDU.INFORM));
            LOG.debug("variableBindings = " + pdu.getVariableBindings());
            m_trapCount++;
        } else {
            LOG.debug("ERROR: Can't create PDU");
        }
    }

    private final class TestTrapListener implements TrapNotificationListener {
        private List<TrapNotification> m_traps = new ArrayList<TrapNotification>();
        private List<String> m_errors = new ArrayList<String>();

        @Override
        public void trapReceived(final TrapNotification trapNotification) {
            LOG.debug("Received Trap... {}", trapNotification);

            if (trapNotification != null) {
                LOG.debug(trapNotification.getClass().getName());
                TestTrapProcessor processor = (TestTrapProcessor)trapNotification.getTrapProcessor();
                LOG.debug("processor is {}", processor);
            }

            m_traps.add(trapNotification);
            m_trapCount++;
        }

        @Override
        public void trapError(final int error, final String msg) {
            LOG.debug("Received Trap Error... {}:{}", error, msg);
            m_errors.add(msg);
        }

        public boolean hasError() {
            return m_errors.size() > 0;
        }

        public int getReceivedTrapCount() {
            return m_traps.size();
        }
    }

    private final class TestTrapProcessor implements TrapProcessor {
        @Override
        public void setCommunity(String community) {}
        @Override
        public void setTimeStamp(long timeStamp) {}
        @Override
        public void setVersion(String version) { LOG.debug("Processed Trap with version: {}", version); }
        @Override
        public void setAgentAddress(InetAddress agentAddress) {}
        @Override
        public void setTrapAddress(InetAddress trapAddress) {}
        @Override
        public void processVarBind(SnmpObjId name, SnmpValue value) {}
        @Override
        public void setTrapIdentity(TrapIdentity trapIdentity) {}
    }
}
