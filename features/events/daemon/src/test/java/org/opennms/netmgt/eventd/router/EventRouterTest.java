package org.opennms.netmgt.eventd.router;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class EventRouterTest {

    private EventProcessor mockFaultPublisher;
    private EventProcessor mockIpcPublisher;
    private EventIpcBroadcaster mockBroadcaster;
    private EventRouter router;

    @Before
    public void setUp() {
        mockFaultPublisher = mock(EventProcessor.class);
        mockIpcPublisher = mock(EventProcessor.class);
        mockBroadcaster = mock(EventIpcBroadcaster.class);
        router = new EventRouter(
                new EventClassifier(),
                mockFaultPublisher,
                mockIpcPublisher,
                mockBroadcaster
        );
    }

    @Test
    public void shouldRouteFaultEventToKafkaAndBroadcast() throws Exception {
        Event event = faultEvent("uei.opennms.org/nodes/nodeDown");
        Log log = createLog(event);

        router.process(log);

        verify(mockFaultPublisher, times(1)).process(any(Log.class), eq(false));
        verify(mockIpcPublisher, never()).process(any(Log.class), any(boolean.class));
        verify(mockBroadcaster, times(1)).broadcastNow(eq(event), eq(false));
    }

    @Test
    public void shouldRouteIpcEventToIpcPublisherAndBroadcast() throws Exception {
        Event event = ipcEvent("uei.opennms.org/internal/reloadDaemonConfig");
        Log log = createLog(event);

        router.process(log);

        verify(mockFaultPublisher, never()).process(any(Log.class), any(boolean.class));
        verify(mockIpcPublisher, times(1)).process(any(Log.class), eq(false));
        verify(mockBroadcaster, times(1)).broadcastNow(eq(event), eq(false));
    }

    @Test
    public void shouldRouteDualEventToBothPublishers() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/internal/reloadDaemonConfigFailed");
        event.setSource("webui");
        AlarmData ad = new AlarmData();
        ad.setReductionKey("key:1");
        ad.setAlarmType(1);
        event.setAlarmData(ad);
        Log log = createLog(event);

        router.process(log);

        verify(mockFaultPublisher, times(1)).process(any(Log.class), eq(false));
        verify(mockIpcPublisher, times(1)).process(any(Log.class), eq(false));
        verify(mockBroadcaster, times(1)).broadcastNow(eq(event), eq(false));
    }

    private Event faultEvent(String uei) {
        Event event = new Event();
        event.setUei(uei);
        event.setSource("test");
        AlarmData ad = new AlarmData();
        ad.setReductionKey(uei + ":1");
        ad.setAlarmType(1);
        event.setAlarmData(ad);
        return event;
    }

    private Event ipcEvent(String uei) {
        Event event = new Event();
        event.setUei(uei);
        event.setSource("test");
        return event;
    }

    private Log createLog(Event... events) {
        Events eventsContainer = new Events();
        for (Event e : events) {
            eventsContainer.addEvent(e);
        }
        Log log = new Log();
        log.setEvents(eventsContainer);
        return log;
    }
}
