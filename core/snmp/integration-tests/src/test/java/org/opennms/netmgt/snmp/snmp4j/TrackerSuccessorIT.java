/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * Used to validate NMS-10621
 *
 * Verifies that we don't get stuck in a loop if a bad
 * agent responds with VBs that come *before* or
 * are *equal to* the requested VBs.
 */
public class TrackerSuccessorIT {
    private static final Logger LOG = LoggerFactory.getLogger(TrackerSuccessorIT.class);

    private DefaultUdpTransportMapping transportMapping;

    @Before
    public void setUp() throws IOException {
        spawnAgent();
    }

    @After
    public void tearDown() throws IOException {
        if (transportMapping != null) {
            transportMapping.close();
        }
    }

    /**
     * Verify that invalid response to not trigger infinite loop in SNMP tracking code.
     *
     * Request should look like:
     *      User Datagram Protocol, Src Port: 40440, Dst Port: 161
     *      Simple Network Management Protocol
     *      version: v2c (1)
     *      community: funkymonkey
     *      data: getBulkRequest (5)
     *      getBulkRequest
     *      request-id: 1705470191
     *      non-repeaters: 0
     *      max-repetitions: 2
     *      variable-bindings: 10 items
     *      1.3.6.1.2.1.2.2.1.1.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.2.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.3.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.4.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.5.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.6.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.7.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.8.2: Value (Null)
     *      1.3.6.1.2.1.2.2.1.9.2: Value (Null)
     *      1.3.6.1.2.1.31.1.1.1.1.2: Value (Null)
     *
     * And the response should look like:
     *     User Datagram Protocol, Src Port: 161, Dst Port: 40440
     *      Simple Network Management Protocol
     *      version: v2c (1)
     *      community: funkymonkey
     *      data: get-response (2)
     *      get-response
     *      request-id: 1705470191
     *      error-status: noError (0)
     *      error-index: 0
     *      variable-bindings: 20 items
     *      1.3.6.1.2.1.2.2.1.1.2: 2
     *      1.3.6.1.2.1.2.2.1.2.2: <MISSING>
     *      1.3.6.1.2.1.2.2.1.3.2: 6
     *      1.3.6.1.2.1.2.2.1.4.2: 1500
     *      1.3.6.1.2.1.2.2.1.5.2: 100000000
     *      1.3.6.1.2.1.2.2.1.6.2: xxxxxxxxxxxx
     *      1.3.6.1.2.1.2.2.1.7.2: 1
     *      1.3.6.1.2.1.2.2.1.8.2: 1
     *      1.3.6.1.2.1.2.2.1.9.2: 0
     *      1.3.6.1.2.1.31.1.1.1.1.2: yyyyyyyy
     *      1.3.6.1.2.1.2.2.1.1.2: 2
     *      1.3.6.1.2.1.2.2.1.2.2: <MISSING>
     *      1.3.6.1.2.1.2.2.1.3.2: 6
     *      1.3.6.1.2.1.2.2.1.4.2: 1500
     *      1.3.6.1.2.1.2.2.1.5.2: 100000000
     *      1.3.6.1.2.1.2.2.1.6.2: xxxxxxxxxxxx
     *      1.3.6.1.2.1.2.2.1.7.2: 1
     *      1.3.6.1.2.1.2.2.1.8.2: 1
     *      1.3.6.1.2.1.2.2.1.9.2: 0
     *      1.3.6.1.2.1.31.1.1.1.1.2: yyyyyyyy
     */
    @Test(timeout=30000)
    public void canHandleInvalidResponses() throws IOException, InterruptedException {
        // Create our walker
        SnmpAgentConfig agent = new SnmpAgentConfig();
        agent.setAddress(InetAddress.getLocalHost());
        agent.setPort(transportMapping.getListenAddress().getPort());
        agent.setVersion(2);
        PhysInterfaceTableTracker tracker = new PhysInterfaceTableTracker();
        final SnmpWalker walker = SnmpUtils.createWalker(agent, "test", tracker);
        walker.start();

        // Wait and verify
        walker.waitFor();
        assertThat(walker.failed(), equalTo(false));

        // We don't care about the actual results of the tracker, only that it did finish successfully
    }

    /**
     * Leverage SNMP4J to create a UDP socket that allows us to receive
     * and send PDUs.
     *
     * @throws IOException on error
     */
    public void spawnAgent() throws IOException {
        transportMapping = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transportMapping);
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public void processPdu(CommandResponderEvent e) {
                LOG.debug("Got request PDU with: {}", e.getPDU());

                // Create the fixed response, copy over the request id
                PDU response = createFixedRepsonsePDU(e.getPDU().getRequestID());

                StatusInformation statusInformation = new StatusInformation();
                StateReference ref = e.getStateReference();
                try {
                    LOG.debug("Replying with: {}", response);
                    e.setProcessed(true);
                    e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(),
                            e.getSecurityModel(),
                            e.getSecurityName(),
                            e.getSecurityLevel(),
                            response,
                            e.getMaxSizeResponsePDU(),
                            ref,
                            statusInformation);
                }
                catch (MessageException ex) {
                    LOG.error("Error while sending response", ex);
                }
            }
        });
        transportMapping.listen();
    }

    private static PDU createFixedRepsonsePDU(Integer32 requestId) {
        final PDU pdu = new PDU();
        pdu.setType(PDU.RESPONSE);
        pdu.setRequestID(requestId);
        addVars(pdu);
        addVars(pdu);
        return pdu;
    }

    private static void addVars(PDU pdu) {
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.1.2"), new Integer32(2)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.2.2"), new Null()));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.3.2"), new Integer32(6)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.4.2"), new Integer32(1500)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.5.2"), new Integer32(100000000)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.6.2"), new OctetString("xxxxxxxxxxxx")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.7.2"), new Integer32(1)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.8.2"), new Integer32(1)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.9.2"), new Integer32(0)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.31.1.1.1.1.2"), new OctetString("yyyyyyyy")));
    }
}
