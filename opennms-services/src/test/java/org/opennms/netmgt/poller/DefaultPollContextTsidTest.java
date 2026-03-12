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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

public class DefaultPollContextTsidTest {

    private DefaultPollContext pollContext;
    private EventIpcManager eventManager;
    private QueryManager queryManager;
    private TsidFactory tsidFactory;
    private PollerConfig pollerConfig;

    @Before
    public void setUp() throws Exception {
        eventManager = mock(EventIpcManager.class);
        queryManager = mock(QueryManager.class);
        tsidFactory = new TsidFactory(0);
        pollerConfig = mock(PollerConfig.class);

        when(pollerConfig.isPathOutageEnabled()).thenReturn(false);
        when(pollerConfig.getMaxConcurrentAsyncPolls()).thenReturn(10);

        pollContext = new DefaultPollContext();
        pollContext.setEventManager(eventManager);
        pollContext.setQueryManager(queryManager);
        pollContext.setTsidFactory(tsidFactory);
        pollContext.setPollerConfig(pollerConfig);
        pollContext.setName("TestPoller");
        pollContext.setLocalHostName("localhost");
        pollContext.afterPropertiesSet();
    }

    @Test
    public void sendEventShouldAssignTsidAndReturnDbPollEvent() {
        Event event = pollContext.createEvent(
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), "test reason");

        PollEvent result = pollContext.sendEvent(event);

        assertTrue("Expected DbPollEvent", result instanceof DbPollEvent);
        assertTrue("Event ID should be > 0", result.getEventId() > 0);
        assertEquals("Event dbid should match PollEvent eventId",
                (long) event.getDbid(), result.getEventId());
        verify(eventManager).sendNow(event);
    }

    @Test
    public void sendEventShouldGenerateUniqueTsids() {
        Event event1 = pollContext.createEvent(
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), "reason1");
        Event event2 = pollContext.createEvent(
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), "reason2");

        PollEvent result1 = pollContext.sendEvent(event1);
        PollEvent result2 = pollContext.sendEvent(event2);

        assertNotEquals("TSIDs should be unique", result1.getEventId(), result2.getEventId());
    }

    @Test
    public void openOutageShouldSetEventIdImmediately() {
        when(queryManager.openOutagePendingLostEventId(anyInt(), anyString(), anyString(), any(Date.class)))
                .thenReturn(42);

        PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("192.168.1.1");
        when(svc.getSvcName()).thenReturn("ICMP");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());

        Event event = pollContext.createEvent(
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), "test");
        PollEvent svcLostEvent = pollContext.sendEvent(event);
        long expectedEventId = svcLostEvent.getEventId();

        pollContext.openOutage(svc, svcLostEvent);

        verify(queryManager).openOutagePendingLostEventId(eq(1), eq("192.168.1.1"), eq("ICMP"), any(Date.class));
        verify(queryManager).updateOpenOutageWithEvent(eq(42), eq(expectedEventId), anyString());
    }

    @Test
    public void resolveOutageShouldSetEventIdImmediately() {
        when(queryManager.resolveOutagePendingRegainEventId(anyInt(), anyString(), anyString(), any(Date.class)))
                .thenReturn(42);

        PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("192.168.1.1");
        when(svc.getSvcName()).thenReturn("ICMP");
        when(svc.getAddress()).thenReturn(InetAddress.getLoopbackAddress());

        PollEvent svcRegainEvent = pollContext.sendEvent(pollContext.createEvent(
                EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), null));
        long expectedEventId = svcRegainEvent.getEventId();

        pollContext.resolveOutage(svc, svcRegainEvent);

        verify(queryManager).resolveOutagePendingRegainEventId(eq(1), eq("192.168.1.1"), eq("ICMP"), any(Date.class));
        verify(queryManager).updateResolvedOutageWithEvent(eq(42), eq(expectedEventId), anyString());
    }

    @Test
    public void resolveOutageShouldHandleMissingOutage() {
        when(queryManager.resolveOutagePendingRegainEventId(anyInt(), anyString(), anyString(), any(Date.class)))
                .thenReturn(null);

        PollableService svc = mock(PollableService.class);
        when(svc.getNodeId()).thenReturn(1);
        when(svc.getIpAddr()).thenReturn("192.168.1.1");
        when(svc.getSvcName()).thenReturn("ICMP");

        PollEvent svcRegainEvent = pollContext.sendEvent(pollContext.createEvent(
                EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI,
                1, InetAddress.getLoopbackAddress(), "ICMP",
                new Date(), null));

        pollContext.resolveOutage(svc, svcRegainEvent);

        verify(queryManager, never()).updateResolvedOutageWithEvent(anyInt(), anyLong(), anyString());
    }
}
