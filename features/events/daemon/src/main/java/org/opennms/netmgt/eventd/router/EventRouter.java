package org.opennms.netmgt.eventd.router;

import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRouter implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EventRouter.class);

    private final EventClassifier classifier;
    private final EventProcessor faultEventPublisher;
    private final EventProcessor ipcEventPublisher;
    private final EventIpcBroadcaster localBroadcaster;

    public EventRouter(EventClassifier classifier,
                       EventProcessor faultEventPublisher,
                       EventProcessor ipcEventPublisher,
                       EventIpcBroadcaster localBroadcaster) {
        this.classifier = classifier;
        this.faultEventPublisher = faultEventPublisher;
        this.ipcEventPublisher = ipcEventPublisher;
        this.localBroadcaster = localBroadcaster;
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            EventClassification classification = classifier.classify(event);
            LOG.debug("Event {} classified as {}", event.getUei(), classification);

            switch (classification) {
                case FAULT:
                    publishFaultEvent(eventLog, event, synchronous);
                    broadcastLocally(event, synchronous);
                    break;
                case IPC:
                    publishIpcEvent(eventLog, event, synchronous);
                    broadcastLocally(event, synchronous);
                    break;
                case DUAL:
                    publishFaultEvent(eventLog, event, synchronous);
                    publishIpcEvent(eventLog, event, synchronous);
                    broadcastLocally(event, synchronous);
                    break;
            }
        }
    }

    private void publishFaultEvent(Log originalLog, Event event, boolean synchronous)
            throws EventProcessorException {
        Log singleEventLog = new Log();
        Events events = new Events();
        events.addEvent(event);
        singleEventLog.setEvents(events);
        singleEventLog.setHeader(originalLog.getHeader());
        faultEventPublisher.process(singleEventLog, synchronous);
    }

    private void publishIpcEvent(Log originalLog, Event event, boolean synchronous)
            throws EventProcessorException {
        Log singleEventLog = new Log();
        Events events = new Events();
        events.addEvent(event);
        singleEventLog.setEvents(events);
        singleEventLog.setHeader(originalLog.getHeader());
        ipcEventPublisher.process(singleEventLog, synchronous);
    }

    private void broadcastLocally(Event event, boolean synchronous) {
        if (event.getLogmsg() != null && "suppress".equals(event.getLogmsg().getDest())) {
            LOG.debug("Suppressing local broadcast for event {}", event.getUei());
            return;
        }
        localBroadcaster.broadcastNow(event, synchronous);
    }
}
