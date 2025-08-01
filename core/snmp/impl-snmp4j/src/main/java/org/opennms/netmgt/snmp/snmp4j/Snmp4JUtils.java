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
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DummyTransport;
import org.snmp4j.transport.TransportListener;

public class Snmp4JUtils {

	private static final transient Logger LOG = LoggerFactory.getLogger(Snmp4JUtils.class);

	/**
	 * @param address
	 * @param port
	 * @param community
	 * @param pdu
	 * 
	 * @return Byte array representing the {@link PDU} in either SNMPv1 or SNMPv2 
	 * format, depending on the type of the {@link PDU} object.
	 */
	public static byte[] convertPduToBytes(InetAddress address, int port, String community, PDU pdu) throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<byte[]> bytes = new AtomicReference<>();

		// IP address is optional when using the DummyTransport because
		// all requests are sent to the {@link DummyTransportResponder}
		final DummyTransport<IpAddress> transport = new DummyTransport<IpAddress>(null);

		final AbstractTransportMapping<IpAddress> responder = transport.getResponder(null);

		// Add a DummyTransportResponder listener that will receive the raw bytes of the PDU
		responder.addTransportListener(new TransportListener() {
			@Override
			public void processMessage(TransportMapping transport, Address address, ByteBuffer byteBuffer, TransportStateReference state) {
				byteBuffer.rewind();
				final byte[] byteArray = new byte[byteBuffer.remaining()];
				byteBuffer.get(byteArray);
				bytes.set(byteArray);
				byteBuffer.rewind();

				latch.countDown();
			}
		});

		// Create our own MessageDispatcher since we don't need to do all
		// of the crypto operations necessary to initialize SNMPv3 which is slow
		MessageDispatcher dispatcher = new MessageDispatcherImpl();
		dispatcher.addMessageProcessingModel(new MPv1());
		dispatcher.addMessageProcessingModel(new MPv2c());

		final Snmp snmp = new Snmp(dispatcher, responder);
		Snmp4JStrategy.trackSession(snmp);
		try {
			snmp.listen();

			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			if (pdu instanceof PDUv1) {
				target.setVersion(SnmpConstants.version1);
			} else {
				target.setVersion(SnmpConstants.version2c);
			}
			target.setAddress(Snmp4JAgentConfig.convertAddress(address, port));

			snmp.send(pdu, target, transport);

			latch.await();

			return bytes.get();
		} finally {
		    try {
			snmp.close();
		    } catch (final IOException e) {
		        LOG.error("failed to close SNMP session", e);
		    } finally {
		        Snmp4JStrategy.reapSession(snmp);
		    }
		}
	}

}
