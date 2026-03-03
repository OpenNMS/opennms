package org.opennms.netmgt.eventd.router;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class EventRouterTest {

    private EventProcessor mockFaultPublisher;
    private MessageBus mockMessageBus;
    private EventIpcBroadcaster mockBroadcaster;
    private EventRouter router;

    @Before
    public void setUp() {
        mockFaultPublisher = mock(EventProcessor.class);
        mockMessageBus = mock(MessageBus.class);
        mockBroadcaster = mock(EventIpcBroadcaster.class);
        router = new EventRouter(
                new EventClassifier(),
                mockFaultPublisher,
                mockMessageBus,
                mockBroadcaster,
                new IpcMessageConverter()
        );
    }

    @Test
    public void shouldRouteFaultEventToKafkaAndBroadcast() throws Exception {
        Event event = faultEvent("uei.opennms.org/nodes/nodeDown");
        Log log = createLog(event);

        router.process(log);

        verify(mockFaultPublisher, times(1)).process(any(Log.class), eq(false));
        verify(mockMessageBus, never()).publish(any(IpcMessage.class));
        verify(mockBroadcaster, times(1)).broadcastNow(eq(event), eq(false));
    }

    @Test
    public void shouldRouteIpcEventToMessageBusAndBroadcast() throws Exception {
        Event event = ipcEvent("uei.opennms.org/internal/reloadDaemonConfig");
        Log log = createLog(event);

        router.process(log);

        verify(mockFaultPublisher, never()).process(any(Log.class), any(boolean.class));
        verify(mockMessageBus, times(1)).publish(any(IpcMessage.class));
        verify(mockBroadcaster, times(1)).broadcastNow(eq(event), eq(false));
    }

    @Test
    public void shouldRouteDualEventToBothKafkaAndMessageBus() throws Exception {
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
        verify(mockMessageBus, times(1)).publish(any(IpcMessage.class));
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
