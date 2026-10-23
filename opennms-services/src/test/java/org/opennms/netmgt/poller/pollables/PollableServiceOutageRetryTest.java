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
package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.ScheduleTimer;
import org.opennms.netmgt.xml.event.Event;

/**
 * NMS-20426: outage writes that fail when a service changes state are retried
 * before the following polls.
 */
public class PollableServiceOutageRetryTest {

    private PollContext m_context;
    private PollableInterface m_iface;
    private PollEvent m_event;

    @Before
    public void setUp() throws Exception {
        m_context = mock(PollContext.class);
        // node-level events carry no address and no service name
        when(m_context.createEvent(anyString(), anyInt(), any(), any(), any(Date.class), any())).thenReturn(new Event());
        m_event = mock(PollEvent.class);
        when(m_event.getDate()).thenReturn(new Date());
        when(m_context.sendEvent(any())).thenReturn(m_event);
        when(m_context.resolveOutage(any(PollableService.class), any(PollEvent.class))).thenReturn(true);

        m_iface = new PollableNetwork(m_context)
                .createNode(1, "node", "Default")
                .createInterface(InetAddress.getLoopbackAddress());
    }

    private PollableService createService(String svcName, PollStatus... results) {
        final PollableService svc = m_iface.createService(svcName);
        final PollableServiceConfig config = mock(PollableServiceConfig.class);
        if (results.length > 0) {
            when(config.poll()).thenReturn(results[0], Arrays.copyOfRange(results, 1, results.length));
        }
        svc.setPollConfig(config);
        svc.setSchedule(new Schedule(svc, config, mock(ScheduleTimer.class)));
        return svc;
    }

    @Test
    public void retriesFailedOutageInsertWhileDown() {
        when(m_context.isNodeProcessingEnabled()).thenReturn(false);
        final PollableService svc = createService("ICMP", PollStatus.down("gone"));
        when(m_context.openOutage(eq(svc), eq(m_event))).thenReturn(false, true);

        svc.doRun();
        assertTrue("first insert failed", svc.isOutagePending());
        verify(m_context, times(1)).openOutage(svc, m_event);

        svc.doRun();
        assertFalse("retried with the original cause", svc.isOutagePending());
        verify(m_context, times(2)).openOutage(svc, m_event);

        svc.doRun();
        verify(m_context, times(2)).openOutage(svc, m_event);
    }

    @Test
    public void stopsRetryingOnceTheServiceIsUp() {
        when(m_context.isNodeProcessingEnabled()).thenReturn(false);
        final PollableService svc = createService("ICMP", PollStatus.down("gone"), PollStatus.up());
        when(m_context.openOutage(eq(svc), eq(m_event))).thenReturn(false);

        svc.doRun();
        assertTrue(svc.isOutagePending());

        // the retry runs before the poll that finds the service up
        svc.doRun();
        assertFalse(svc.isOutagePending());
        verify(m_context, times(2)).openOutage(svc, m_event);
        verify(m_context, times(1)).resolveOutage(eq(svc), any(PollEvent.class));

        svc.doRun();
        verify(m_context, times(2)).openOutage(svc, m_event);
    }

    @Test
    public void retriesFailedOutageResolution() {
        when(m_context.isNodeProcessingEnabled()).thenReturn(false);
        final PollableService svc = createService("ICMP", PollStatus.down("gone"), PollStatus.up());
        when(m_context.openOutage(eq(svc), eq(m_event))).thenReturn(true);
        when(m_context.resolveOutage(eq(svc), eq(m_event))).thenReturn(false, true);

        svc.doRun();
        svc.doRun();
        assertTrue("resolution failed", svc.isResolutionPending());
        verify(m_context, times(1)).resolveOutage(svc, m_event);

        svc.doRun();
        assertFalse(svc.isResolutionPending());
        verify(m_context, times(2)).resolveOutage(svc, m_event);
        verify(m_context, times(1)).openOutage(svc, m_event);
    }

    /**
     * A service that goes down again while its last resolution is still pending
     * must not open a new outage until the old one is closed, or the retried
     * resolution would close the new one.
     */
    @Test
    public void doesNotOpenWhileAResolutionIsPending() {
        when(m_context.isNodeProcessingEnabled()).thenReturn(false);
        final PollableService svc = createService("ICMP", PollStatus.down("gone"), PollStatus.up(), PollStatus.down("gone again"));
        when(m_context.openOutage(eq(svc), eq(m_event))).thenReturn(true);
        when(m_context.resolveOutage(eq(svc), eq(m_event))).thenReturn(false, false, false, true);

        svc.doRun();
        svc.doRun();
        assertTrue(svc.isResolutionPending());

        // down again: the resolve fails before the poll and again in createOutage, so nothing is opened yet
        svc.doRun();
        assertTrue(svc.isResolutionPending());
        assertTrue("open deferred behind the pending resolution", svc.isOutagePending());
        verify(m_context, times(1)).openOutage(svc, m_event);

        // resolve succeeds, then the deferred open runs
        svc.doRun();
        assertFalse(svc.isResolutionPending());
        assertFalse(svc.isOutagePending());
        verify(m_context, times(4)).resolveOutage(svc, m_event);
        verify(m_context, times(2)).openOutage(svc, m_event);
    }

    /**
     * With a critical service down the other services on the interface get the
     * interface's cause without ever being polled or marked down themselves.
     * The retry must still run for them.
     */
    @Test
    public void retriesInsertForServiceWithInheritedCause() {
        when(m_context.isNodeProcessingEnabled()).thenReturn(true);
        when(m_context.getCriticalServiceName()).thenReturn("ICMP");
        final PollableService icmp = createService("ICMP", PollStatus.down("gone"));
        final PollableService http = createService("HTTP");
        when(m_context.openOutage(eq(icmp), eq(m_event))).thenReturn(true);
        when(m_context.openOutage(eq(http), eq(m_event))).thenReturn(false, true);

        icmp.doRun();
        assertTrue("critical service brought the interface down", icmp.getStatus().isDown());
        assertTrue("HTTP keeps its own status", http.getStatus().isUp());
        assertTrue("HTTP has the inherited cause", http.getCause() != null);
        assertTrue(http.isOutagePending());
        verify(m_context, times(1)).openOutage(http, m_event);

        // HTTP's own schedule still fires; the interface answers for it without a poll
        http.doRun();
        assertFalse(http.isOutagePending());
        verify(m_context, times(2)).openOutage(http, m_event);
        verify(m_context, times(1)).openOutage(eq(icmp), any(PollEvent.class));
    }
}
