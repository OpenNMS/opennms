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
package org.opennms.netmgt.poller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

public class DefaultPollContextTest {

    @Test
    public void openOutageSkipsEventWhenNothingWasPersisted() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.openOutagePendingLostEventId(anyInt(), anyString(), anyString(), any())).thenReturn(null);
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        final PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(svc.getSvcName()).thenReturn("ICMP");
        final PollEvent lost = mock(PollEvent.class);
        when(lost.getDate()).thenReturn(new Date());
        when(lost.getEventId()).thenReturn(42L);

        assertFalse(context.openOutage(svc, lost));

        verify(queryManager, never()).updateOpenOutageWithEventId(anyInt(), anyLong());
        verify(eventManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void openOutageReportsPersistedOutage() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.openOutagePendingLostEventId(anyInt(), anyString(), anyString(), any())).thenReturn(7);
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        final PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(svc.getSvcName()).thenReturn("ICMP");
        final PollEvent lost = mock(PollEvent.class);
        when(lost.getDate()).thenReturn(new Date());
        when(lost.getEventId()).thenReturn(42L);

        assertTrue(context.openOutage(svc, lost));

        verify(queryManager).updateOpenOutageWithEventId(7, 42L);
        verify(eventManager).sendNow(any(Event.class));
    }

    @Test
    public void openOutageReportsFailedEventIdUpdate() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.openOutagePendingLostEventId(anyInt(), anyString(), anyString(), any())).thenReturn(7);
        doThrow(new RuntimeException("update failed")).when(queryManager).updateOpenOutageWithEventId(7, 42L);
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        final PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(svc.getSvcName()).thenReturn("ICMP");
        final PollEvent lost = mock(PollEvent.class);
        when(lost.getDate()).thenReturn(new Date());
        when(lost.getEventId()).thenReturn(42L);

        assertFalse(context.openOutage(svc, lost));

        verify(eventManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void resolveOutageReportsFailedEventSend() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.resolveOutagePendingRegainEventId(anyInt(), anyString(), anyString(), any())).thenReturn(7);
        final EventIpcManager eventManager = mock(EventIpcManager.class);
        doThrow(new RuntimeException("bus down")).when(eventManager).sendNow(any(Event.class));

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        final PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(svc.getSvcName()).thenReturn("ICMP");
        final PollEvent regained = mock(PollEvent.class);
        when(regained.getDate()).thenReturn(new Date());
        when(regained.getEventId()).thenReturn(43L);

        assertFalse(context.resolveOutage(svc, regained));

        verify(queryManager).updateResolvedOutageWithEventId(7, 43L);
    }

    @Test
    public void openOutageReportsFailedWrite() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.openOutagePendingLostEventId(anyInt(), anyString(), anyString(), any())).thenThrow(new RuntimeException("insert failed"));
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        assertFalse(context.openOutage(service(), event()));
        verify(eventManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void resolveOutageReportsFailedWrite() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.resolveOutagePendingRegainEventId(anyInt(), anyString(), anyString(), any())).thenThrow(new RuntimeException("update failed"));
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        assertFalse(context.resolveOutage(service(), event()));
        verify(eventManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void resolveOutageWithNothingOpenNeedsNoRetry() {
        final QueryManager queryManager = mock(QueryManager.class);
        when(queryManager.resolveOutagePendingRegainEventId(anyInt(), anyString(), anyString(), any())).thenReturn(null);
        final EventIpcManager eventManager = mock(EventIpcManager.class);

        final DefaultPollContext context = new DefaultPollContext();
        context.setQueryManager(queryManager);
        context.setEventManager(eventManager);

        assertTrue(context.resolveOutage(service(), event()));
        verify(queryManager, never()).updateResolvedOutageWithEventId(anyInt(), anyLong());
        verify(eventManager, never()).sendNow(any(Event.class));
    }

    private static PollableService service() {
        final PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(svc.getSvcName()).thenReturn("ICMP");
        return svc;
    }

    private static PollEvent event() {
        final PollEvent event = mock(PollEvent.class);
        when(event.getDate()).thenReturn(new Date());
        when(event.getEventId()).thenReturn(42L);
        return event;
    }
}
