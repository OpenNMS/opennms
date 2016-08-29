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

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
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

	@BeforeClass
	public static void setupSnmp4jLogging() {
		MockLogAppender.setupLogging(true, "DEBUG");
	}

	//private static class DummyAddress extends IpAddress {}

	@Test
	public void testTrapReceiverWithoutOpenNMS() throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);

		//DummyAddress address = new DummyAddress();

		// IP address is optional when using the DummyTransport because
		// all requests are sent to the {@link DummyTransportResponder}
		final DummyTransport<IpAddress> transport = new DummyTransport<IpAddress>(null);

		/*
		transport.addTransportListener(new TransportListener() {
			@Override
			public void processMessage(TransportMapping arg0, Address arg1, ByteBuffer arg2, TransportStateReference arg3) {
				// TODO: Capture byte[]
				LOG.debug("GOT HERE 111");
				LOG.debug(arg2.toString());
				latch.countDown();
			}
		});
		 */

		final AbstractTransportMapping<IpAddress> responder = transport.getResponder(null);
		responder.addTransportListener(new TransportListener() {
			@Override
			public void processMessage(TransportMapping transport, Address address, ByteBuffer byteBuffer, TransportStateReference state) {
				LOG.debug(byteBuffer.toString());
				latch.countDown();
			}
		});

		Snmp snmp = new Snmp(responder);

		/*
		snmp.addCommandResponder(new CommandResponder() {
			@Override
			public void processPdu(CommandResponderEvent event) {
				LOG.debug("HELLOWE");
			}
		});
		//transport.listen();
		 */

		snmp.listen();

		PDU pdu = makePdu();

		CommunityTarget target = new CommunityTarget();
		// TODO: Update with community of message
		target.setCommunity(new OctetString("helloworld"));
		if (pdu instanceof PDUv1) {
			target.setVersion(SnmpConstants.version1);
		} else {
			target.setVersion(SnmpConstants.version2c);
		}
		target.setRetries(0);
		target.setTimeout(1000);
		// TODO: Use sourceAddress, sourcePort of message
		target.setAddress(Snmp4JAgentConfig.convertAddress(InetAddressUtils.ONE_TWENTY_SEVEN, 162));

		//snmp.trap(pdu, target);
		snmp.send(pdu, target, transport);

		latch.await();
	}

	private static final PDU makePdu() {
		PDU snmp4JV2cTrapPdu = new PDUv1();

		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-2")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("Trap v1 msg-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("Trap v1 msg-2")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), 
				new OctetString("Trap v1 msg-3")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.733.6.3.18.1.5.0"),
				new Integer32(1))); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new Null())); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"),
				new Null(128)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"),
				new Null(129)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"),
				new Null(130)));
		snmp4JV2cTrapPdu.setType(PDU.V1TRAP);

		return snmp4JV2cTrapPdu;
	}
}
