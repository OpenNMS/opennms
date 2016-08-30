/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DummyTransport;
import org.snmp4j.transport.TransportListener;

public class Snmp4JDummyTransportTest {

	private static final Logger LOG = LoggerFactory.getLogger(Snmp4JDummyTransportTest.class);

	static final long BASE = System.nanoTime();

	@Before
	public void setupSnmp4jLogging() {
		MockLogAppender.setupLogging(true, "DEBUG");
	}

	@Test
	public void testDummyTransport() throws Exception {

		PDU pdu = makePdu();

		LOG.debug("1 " + (System.nanoTime() - BASE));
		LOG.debug(SnmpUtils.getHexString(convertPduToBytes(InetAddressUtils.ONE_TWENTY_SEVEN, 162, "hello", pdu)));

	}

	/**
	 * TODO: Move conversion into this method
	 * 
	 * @param address
	 * @param port
	 * @param community
	 * @param pdu
	 * @return
	 */
	public static byte[] convertPduToBytes(InetAddress address, int port, String community, PDU pdu) throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<byte[]> bytes = new AtomicReference<>();

		LOG.debug("2 " + (System.nanoTime() - BASE));

		// IP address is optional when using the DummyTransport because
		// all requests are sent to the {@link DummyTransportResponder}
		final DummyTransport<IpAddress> transport = new DummyTransport<IpAddress>(null);

		final AbstractTransportMapping<IpAddress> responder = transport.getResponder(null);

		LOG.debug("3 " + (System.nanoTime() - BASE));

		responder.addTransportListener(new TransportListener() {
			@Override
			public void processMessage(TransportMapping transport, Address address, ByteBuffer byteBuffer, TransportStateReference state) {
				//LOG.debug(address == null ? "[null]" : address.toString());
				//LOG.debug(byteBuffer.toString());

				LOG.debug("2 " + (System.nanoTime() - BASE));

				byteBuffer.rewind();
				final byte[] byteArray = new byte[byteBuffer.remaining()];
				byteBuffer.get(byteArray);
				bytes.set(byteArray);
				byteBuffer.rewind();

				LOG.debug("3 " + (System.nanoTime() - BASE));

				latch.countDown();

				LOG.debug("4 " + (System.nanoTime() - BASE));
			}
		});

		LOG.debug("4 " + (System.nanoTime() - BASE));

		// Create our own MessageDispatcher since we don't need to do all
		// of the crypto operations necessary to initialize SNMPv3 which is slow
		MessageDispatcher dispatcher = new MessageDispatcherImpl();
		dispatcher.addMessageProcessingModel(new MPv1());
		dispatcher.addMessageProcessingModel(new MPv2c());

		LOG.debug("5 " + (System.nanoTime() - BASE));

		Snmp snmp = new Snmp(dispatcher, responder);

		LOG.debug("6 " + (System.nanoTime() - BASE));

		snmp.listen();

		LOG.debug("7 " + (System.nanoTime() - BASE));

		CommunityTarget target = new CommunityTarget();
		// TODO: Update with community of message
		target.setCommunity(new OctetString("helloworld"));
		if (pdu instanceof PDUv1) {
			target.setVersion(SnmpConstants.version1);
		} else {
			target.setVersion(SnmpConstants.version2c);
		}
		//target.setRetries(0);
		//target.setTimeout(1000);
		// TODO: Use sourceAddress, sourcePort of message
		target.setAddress(Snmp4JAgentConfig.convertAddress(address, port));

		LOG.debug("8 " + (System.nanoTime() - BASE));

		//snmp.trap(pdu, target);
		snmp.send(pdu, target, transport);

		LOG.debug("9 " + (System.nanoTime() - BASE));

		latch.await();

		LOG.debug("1 " + (System.nanoTime() - BASE));

		return bytes.get();
	}

	private static final PDU makePdu() {
		PDU snmp4JV2cTrapPdu = new PDUv1();

		OID oid = new OID(".1.3.6.1.2.1.1.3.0");

		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-2")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"), new OctetString("Trap v1 msg-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"), new OctetString("Trap v1 msg-2")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), new OctetString("Trap v1 msg-3")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.733.6.3.18.1.5.0"), new Integer32(1))); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"), new Null())); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"), new Null(128)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"), new Null(129)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"), new Null(130)));
		snmp4JV2cTrapPdu.setType(PDU.V1TRAP);

		return snmp4JV2cTrapPdu;
	}
}
