package org.opennms.netmgt.eventd.router;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
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
    private final MessageBus messageBus;
    private final EventIpcBroadcaster localBroadcaster;
    private final IpcMessageConverter ipcMessageConverter;

    public EventRouter(EventClassifier classifier,
                       EventProcessor faultEventPublisher,
                       MessageBus messageBus,
                       EventIpcBroadcaster localBroadcaster,
                       IpcMessageConverter ipcMessageConverter) {
        this.classifier = classifier;
        this.faultEventPublisher = faultEventPublisher;
        this.messageBus = messageBus;
        this.localBroadcaster = localBroadcaster;
        this.ipcMessageConverter = ipcMessageConverter;
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
                    publishIpcMessage(event);
                    broadcastLocally(event, synchronous);
                    break;
                case DUAL:
                    publishFaultEvent(eventLog, event, synchronous);
                    publishIpcMessage(event);
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

    private void publishIpcMessage(Event event) {
        IpcMessage message = ipcMessageConverter.convert(event);
        messageBus.publish(message);
    }

    private void broadcastLocally(Event event, boolean synchronous) {
        if (event.getLogmsg() != null && "suppress".equals(event.getLogmsg().getDest())) {
            LOG.debug("Suppressing local broadcast for event {}", event.getUei());
            return;
        }
        localBroadcaster.broadcastNow(event, synchronous);
    }
}
