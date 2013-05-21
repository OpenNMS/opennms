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

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.junit.Test;
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
	
	@Override
	protected boolean usingMockStrategy() {
		return false;
	}

	private final Snmp4JStrategy m_strategy = new Snmp4JStrategy();

    private int trapCount = 0;

    private final class TestTrapListener implements TrapNotificationListener {
        private boolean m_error = false;
        private int m_receivedTrapCount = 0;

        @Override
        public void trapReceived(TrapNotification trapNotification) {
            m_receivedTrapCount++;
        }

        @Override
        public void trapError(int error, String msg) {
            m_error = true;
        }

        public boolean hasError() {
            return m_error;
        }

        public int getReceivedTrapCount() {
            return m_receivedTrapCount;
        }
    }

    private final class TestTrapProcessor implements TrapProcessor {
        @Override
        public void setCommunity(String community) {}
        @Override
        public void setTimeStamp(long timeStamp) {}
        @Override
        public void setVersion(String version) {}
        @Override
        public void setAgentAddress(InetAddress agentAddress) {}
        @Override
        public void setTrapAddress(InetAddress trapAddress) {}
        @Override
        public void processVarBind(SnmpObjId name, SnmpValue value) {}
        @Override
        public void setTrapIdentity(TrapIdentity trapIdentity) {}
    }

    /*
     * IMPORTANT:
     *
     * The sentence <code>snmp.getUSM().addUser(...)</code>, is the only requirement in order to properly process SNMPv3 traps.
     * This is related with the credentials that should be created for Trapd in order to properly authenticate and/or decode SNMPv3 traps in OpennMS.
     * This is a user that should be configured (or should be used) by the external  devices to send SNMPv3 Traps to OpenNMS.
     * The SNMPv3 users should be configured in trapd-configuration.xml
     *
     */
    @Test
    public void testTrapReceiverWithoutOpenNMS() throws Exception {
        System.out.println("SNMP4J: Register for Traps");
        trapCount = 0;
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping(new UdpAddress(9162)));
        snmp.addCommandResponder(this);
        snmp.getUSM().addUser(
                new OctetString("opennmsUser"),
                new UsmUser(new OctetString("opennmsUser"), AuthMD5.ID, new OctetString("0p3nNMSv3"), PrivDES.ID, new OctetString("0p3nNMSv3")));
        snmp.listen();

        sendTraps();

        System.out.println("SNMP4J: Unregister for Traps");
        snmp.close();

        System.out.println("SNMP4J: Checking Trap status");
        assertEquals(2, trapCount);
    }

    @Test
    public void testTrapReceiverWithOpenNMS() throws Exception {
        System.out.println("ONMS: Register for Traps");
        TestTrapListener trapListener = new TestTrapListener();
        SnmpV3User user = new SnmpV3User("agalue", "MD5", "0p3nNMSv3", "DES", "0p3nNMSv3");
        m_strategy.registerForTraps(trapListener, this, InetAddress.getLocalHost(), 9162, Collections.singletonList(user));

        sendTraps();

        System.out.println("ONMS: Unregister for Traps");
        m_strategy.unregisterForTraps(trapListener, 9162);

        System.out.println("ONMS: Checking Trap status");
        assertFalse(trapListener.hasError());
        assertEquals(2, trapListener.getReceivedTrapCount());
    }

    private void sendTraps() throws Exception, UnknownHostException, InterruptedException {
        System.out.println("Sending V2 Trap");
        SnmpObjId enterpriseId = SnmpObjId.get(".0.0");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        SnmpTrapBuilder pdu = m_strategy.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), m_strategy.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), m_strategy.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), m_strategy.getValueFactory().getObjectId(enterpriseId));
        pdu.send(InetAddress.getLocalHost().getHostAddress(), 9162, "public");
        Thread.sleep(1000);

        System.out.println("Sending V3 Trap");
        SnmpV3TrapBuilder pduv3 = m_strategy.getV3TrapBuilder();
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), m_strategy.getValueFactory().getTimeTicks(0));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), m_strategy.getValueFactory().getObjectId(trapOID));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), m_strategy.getValueFactory().getObjectId(enterpriseId));
        pduv3.send(InetAddress.getLocalHost().getHostAddress(), 9162, SnmpConfiguration.AUTH_PRIV, "opennmsUser", "0p3nNMSv3", SnmpConfiguration.DEFAULT_AUTH_PROTOCOL, "0p3nNMSv3", SnmpConfiguration.DEFAULT_PRIV_PROTOCOL);
        Thread.sleep(1000);
    }

        @Override
    public TrapProcessor createTrapProcessor() {
        return new TestTrapProcessor();
    }

        @Override
    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        PDU pdu = cmdRespEvent.getPDU();
        System.out.println("Received PDU... " + pdu);
        if (pdu != null) {
            System.out.println(pdu.getClass().getName());
            System.out.println("trapType = " + pdu.getType());
            System.out.println("isPDUv1 = " + (pdu instanceof PDUv1));
            System.out.println("isTrap = " + (pdu.getType() == PDU.TRAP));
            System.out.println("isInform = " + (pdu.getType() == PDU.INFORM));
            System.out.println("variableBindings = " + pdu.getVariableBindings());
            trapCount++;
        } else {
            System.err.println("ERROR: Can't create PDU");
        }
    }
}
