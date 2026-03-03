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
package org.opennms.features.events.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ILogMsg;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.IValue;
import org.springframework.jdbc.core.JdbcTemplate;

public class EventArchiveWriterTest {

    private JdbcTemplate jdbcTemplate;
    private EventArchiveWriter writer;

    @Before
    public void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        writer = new EventArchiveWriter(jdbcTemplate);
    }

    @Test
    public void shouldReturnCorrectName() {
        assertThat(writer.getName()).isEqualTo("EventArchiveWriter");
    }

    @Test
    public void shouldInsertEventWithAllFields() throws UnknownHostException {
        IEvent event = mockEvent(
                42L,
                "uei.opennms.org/nodes/nodeDown",
                "pollerd",
                "Major",
                new Date(1709337600000L),
                7L,
                InetAddress.getByName("192.168.1.1"),
                "ICMP",
                "Node 7 is down",
                "The node has stopped responding to ICMP polls"
        );

        AtomicReference<Object[]> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            Object[] allArgs = invocation.getArguments();
            // First arg is the SQL string; remaining args are the query parameters
            Object[] queryParams = new Object[allArgs.length - 1];
            System.arraycopy(allArgs, 1, queryParams, 0, queryParams.length);
            captured.set(queryParams);
            return 1;
        }).when(jdbcTemplate).update(any(String.class), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());

        writer.onEvent(event);

        Object[] args = captured.get();
        assertThat(args).isNotNull();
        assertThat(args[0]).isEqualTo(42L); // tsid
        assertThat(args[1]).isEqualTo("uei.opennms.org/nodes/nodeDown"); // uei
        assertThat(args[2]).isEqualTo("pollerd"); // source
        assertThat(args[3]).isEqualTo(6); // severity (Major=6)
        assertThat(args[4]).isEqualTo(new Timestamp(1709337600000L)); // time
        assertThat(args[5]).isEqualTo(7L); // nodeId
        assertThat(args[6]).isEqualTo("192.168.1.1"); // ip
        assertThat(args[7]).isEqualTo("ICMP"); // service
        assertThat(args[8]).isEqualTo("Node 7 is down"); // log msg
        assertThat(args[9]).isEqualTo("The node has stopped responding to ICMP polls"); // descr
    }

    @Test
    public void shouldSkipEventWithoutTsid() {
        IEvent event = mock(IEvent.class);
        when(event.getDbid()).thenReturn(null);
        when(event.hasDbid()).thenReturn(false);
        when(event.getUei()).thenReturn("uei.opennms.org/test");

        writer.onEvent(event);

        verify(jdbcTemplate, never()).update(any(String.class), any(Object[].class));
    }

    @Test
    public void shouldSkipNullEvent() {
        writer.onEvent(null);
        verify(jdbcTemplate, never()).update(any(String.class), any(Object[].class));
    }

    @Test
    public void shouldParseSeverityNames() {
        assertThat(EventArchiveWriter.parseSeverity("Indeterminate")).isEqualTo(1);
        assertThat(EventArchiveWriter.parseSeverity("Cleared")).isEqualTo(2);
        assertThat(EventArchiveWriter.parseSeverity("Normal")).isEqualTo(3);
        assertThat(EventArchiveWriter.parseSeverity("Warning")).isEqualTo(4);
        assertThat(EventArchiveWriter.parseSeverity("Minor")).isEqualTo(5);
        assertThat(EventArchiveWriter.parseSeverity("Major")).isEqualTo(6);
        assertThat(EventArchiveWriter.parseSeverity("Critical")).isEqualTo(7);
    }

    @Test
    public void shouldParseSeverityNumbers() {
        assertThat(EventArchiveWriter.parseSeverity("3")).isEqualTo(3);
        assertThat(EventArchiveWriter.parseSeverity("7")).isEqualTo(7);
    }

    @Test
    public void shouldDefaultToIndeterminateForUnknownSeverity() {
        assertThat(EventArchiveWriter.parseSeverity(null)).isEqualTo(1);
        assertThat(EventArchiveWriter.parseSeverity("Unknown")).isEqualTo(1);
    }

    @Test
    public void shouldSerializeParameters() {
        IParm parm1 = mockParm("nodeLabel", "router-01");
        IParm parm2 = mockParm("reason", "timeout");
        String json = EventArchiveWriter.serializeParameters(List.of(parm1, parm2));
        assertThat(json).contains("\"nodeLabel\":\"router-01\"");
        assertThat(json).contains("\"reason\":\"timeout\"");
    }

    @Test
    public void shouldReturnNullForEmptyParameters() {
        assertThat(EventArchiveWriter.serializeParameters(null)).isNull();
        assertThat(EventArchiveWriter.serializeParameters(List.of())).isNull();
    }

    private static IEvent mockEvent(Long dbid, String uei, String source, String severity,
                                     Date time, Long nodeId, InetAddress iface,
                                     String service, String logMsg, String descr) {
        IEvent event = mock(IEvent.class);
        when(event.getDbid()).thenReturn(dbid);
        when(event.hasDbid()).thenReturn(dbid != null);
        when(event.getUei()).thenReturn(uei);
        when(event.getSource()).thenReturn(source);
        when(event.getSeverity()).thenReturn(severity);
        when(event.getTime()).thenReturn(time);
        when(event.getNodeid()).thenReturn(nodeId);
        when(event.getInterfaceAddress()).thenReturn(iface);
        when(event.getService()).thenReturn(service);
        when(event.getDescr()).thenReturn(descr);

        ILogMsg logMsgObj = mock(ILogMsg.class);
        when(logMsgObj.getContent()).thenReturn(logMsg);
        when(event.getLogmsg()).thenReturn(logMsgObj);
        when(event.getParmCollection()).thenReturn(List.of());

        return event;
    }

    private static IParm mockParm(String name, String value) {
        IParm parm = mock(IParm.class);
        when(parm.getParmName()).thenReturn(name);
        IValue iValue = mock(IValue.class);
        when(iValue.getContent()).thenReturn(value);
        when(parm.getValue()).thenReturn(iValue);
        return parm;
    }
}
