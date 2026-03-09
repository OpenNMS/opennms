package org.opennms.netmgt.eventd.router;

import org.opennms.netmgt.xml.event.Event;

public class EventClassifier {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    /**
     * Internal events that must be routed to Kafka (DUAL) even without alarm-data,
     * because they trigger cross-container actions via KafkaEventSubscriptionService.
     *
     * Events here are classified as DUAL: published to both Kafka and MessageBus (AMQ).
     * This is required when a daemon container's @EventHandler uses
     * AnnotationBasedEventListenerAdapter (Kafka path) for an internal UEI that would
     * otherwise be IPC-only (MessageBus).
     */
    private static final String[] CROSS_CONTAINER_INTERNAL_UEIS = {
        "uei.opennms.org/internal/discovery/newSuspect",
        "uei.opennms.org/internal/capsd/forceRescan",
    };

    public EventClassification classify(Event event) {
        boolean hasAlarmData = event.getAlarmData() != null;
        boolean isInternal = event.getUei() != null
                && event.getUei().startsWith(INTERNAL_UEI_PREFIX);

        if (isInternal && (hasAlarmData || isCrossContainerEvent(event))) {
            return EventClassification.DUAL;
        }
        if (isInternal) {
            return EventClassification.IPC;
        }
        // Everything else is a fault event: traps, syslog, thresholds, node events, etc.
        return EventClassification.FAULT;
    }

    private boolean isCrossContainerEvent(Event event) {
        if (event.getUei() == null) {
            return false;
        }
        for (String uei : CROSS_CONTAINER_INTERNAL_UEIS) {
            if (uei.equals(event.getUei())) {
                return true;
            }
        }
        return false;
    }
}
