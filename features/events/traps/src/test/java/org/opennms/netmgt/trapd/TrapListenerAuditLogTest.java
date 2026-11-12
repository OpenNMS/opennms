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
package org.opennms.netmgt.trapd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.mock.MockDistPollerDao;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

/**
 * Verifies the lines written to the dedicated trap audit logger by {@link TrapListener}.
 */
public class TrapListenerAuditLogTest {

    private static final String TRAP_OID = ".1.3.6.1.4.1.9.9.41.2.0.1";

    private TrapListener listener;
    private AsyncDispatcher<TrapInformationWrapper> dispatcher;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        final TrapdConfigBean config = new TrapdConfigBean();
        config.setSnmpTrapPort(1162);
        config.setSnmpTrapAddress("localhost");

        dispatcher = mock(AsyncDispatcher.class);
        final MessageDispatcherFactory factory = mock(MessageDispatcherFactory.class);
        when(factory.createAsyncDispatcher(any())).thenReturn((AsyncDispatcher) dispatcher);

        listener = new TrapListener(config);
        listener.setMessageDispatcherFactory(factory);
        listener.setDistPollerDao(new MockDistPollerDao());
    }

    @Test
    public void logsReceivedLineForV2Trap() throws Exception {
        when(dispatcher.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        listener.trapReceived(v2Trap("public", 2));

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("TRAP RECEIVED src=127.0.0.1 agent=127.0.0.1 version=v2 oid=" + TRAP_OID + " varbinds=3"));
        assertThat(lines.get(0), not(containsString("user=")));
        assertThat(lines.get(0), not(containsString("public")));
    }

    @Test
    public void logsSecurityNameForV3Trap() throws Exception {
        when(dispatcher.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        listener.trapReceived(v2Trap("opennms-user", 3));

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("version=v3 user=opennms-user oid=" + TRAP_OID));
    }

    @Test
    public void logsReceivedOidNotEnterpriseForStandardV2TrapWithEnterpriseVarbind() throws Exception {
        when(dispatcher.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        // linkDown with a trailing snmpTrapEnterprise.0, as many agents send it. TrapIdentity reports the
        // enterprise OID for this shape, but the audit log must show the OID the agent actually sent.
        final String linkDown = ".1.3.6.1.6.3.1.1.5.3";
        final String enterprise = ".1.3.6.1.4.1.9";
        final PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(linkDown)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.1.3"), new org.snmp4j.smi.Integer32(3)));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapEnterprise, new OID(enterprise)));
        final TrapInformation trap = new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(InetAddressUtils.ONE_TWENTY_SEVEN, "public", pdu, 2);
        assertThat("precondition: TrapIdentity substitutes the enterprise", trap.getTrapIdentity().getTrapOID(), org.hamcrest.Matchers.is(enterprise));

        listener.trapReceived(trap);

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("version=v2 oid=" + linkDown + " varbinds=4"));
    }

    @Test
    public void logsDerivedOidForV1Trap() throws Exception {
        when(dispatcher.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        final PDUv1 pdu = new PDUv1();
        pdu.setType(PDU.V1TRAP);
        pdu.setEnterprise(new OID(".1.3.6.1.4.1.9.9.41.2"));
        pdu.setGenericTrap(6);
        pdu.setSpecificTrap(1);
        pdu.setAgentAddress(new IpAddress("10.0.0.5"));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"), new OctetString("hostname")));
        final TrapInformation trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(InetAddressUtils.ONE_TWENTY_SEVEN, "public", pdu);

        listener.trapReceived(trap);

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("TRAP RECEIVED src=127.0.0.1 agent=10.0.0.5 version=v1 oid=" + TRAP_OID + " varbinds=1"));
    }

    @Test
    public void logsRejectedLineForMalformedTrap() throws Exception {
        // A v2 trap needs at least two varbinds; this one has none
        final PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        final TrapInformation trap = new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(InetAddressUtils.ONE_TWENTY_SEVEN, "public", pdu, 2);

        listener.trapReceived(trap);

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("TRAP REJECTED src=127.0.0.1 agent=127.0.0.1 version=v2 oid=unknown varbinds=0 reason="));
    }

    @Test
    public void logsForwardFailedLineWhenDispatchFails() throws Exception {
        final CompletableFuture<AsyncDispatcher.DispatchStatus> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        when(dispatcher.send(any())).thenReturn(failed);

        listener.trapReceived(v2Trap("public", 2));

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(2));
        assertThat(lines.get(0), containsString("TRAP RECEIVED src=127.0.0.1"));
        assertThat(lines.get(1), containsString("TRAP FORWARD_FAILED src=127.0.0.1 agent=127.0.0.1 version=v2 oid=" + TRAP_OID + " varbinds=3 reason="));
        assertThat(lines.get(1), containsString("broker down"));
    }

    @Test
    public void logsForwardFailedLineWhenSendThrows() throws Exception {
        when(dispatcher.send(any())).thenThrow(new IllegalStateException("dispatcher closed"));

        listener.trapReceived(v2Trap("public", 2));

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(2));
        assertThat(lines.get(0), containsString("TRAP RECEIVED src=127.0.0.1"));
        assertThat(lines.get(1), containsString("TRAP FORWARD_FAILED src=127.0.0.1 agent=127.0.0.1 version=v2 oid=" + TRAP_OID + " varbinds=3 reason=dispatcher closed"));
    }

    @Test
    public void collapsesMultiLineReasonToOneLine() throws Exception {
        // Two varbinds, but neither is sysUpTime.0 / snmpTrapOID.0: validation fails with a multi-line message
        final PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"), new OctetString("a")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.6.0"), new OctetString("b")));
        final TrapInformation trap = new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(InetAddressUtils.ONE_TWENTY_SEVEN, "public", pdu, 2);

        listener.trapReceived(trap);

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), containsString("TRAP REJECTED src=127.0.0.1 agent=127.0.0.1 version=v2 oid=unknown varbinds=2 reason="));
        assertThat(lines.get(0), containsString("Varbinds received are"));
        assertThat(lines.get(0), not(containsString("\n")));
        assertThat(lines.get(0), not(containsString("\t")));
    }

    @Test
    public void usesExceptionTypeWhenMessageIsNull() throws Exception {
        when(dispatcher.send(any())).thenThrow(new IllegalStateException());

        listener.trapReceived(v2Trap("public", 2));

        final List<String> lines = auditLines();
        assertThat(lines, hasSize(2));
        assertThat(lines.get(1), containsString("reason=IllegalStateException"));
    }

    private static TrapInformation v2Trap(final String communityOrUser, final int version) throws Exception {
        final PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(TRAP_OID)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"), new OctetString("hostname")));
        return new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(InetAddress.getByName("127.0.0.1"), communityOrUser, pdu, version);
    }

    private static List<String> auditLines() {
        return Arrays.stream(MockLogAppender.getEvents())
                .filter(e -> TrapListener.AUDIT_LOGGER_NAME.equals(e.getLoggerName()))
                .map(LoggingEvent::getMessage)
                .collect(Collectors.toList());
    }
}
