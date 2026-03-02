package org.opennms.netmgt.eventd.router;

import org.opennms.netmgt.xml.event.Event;

public class EventClassifier {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    public EventClassification classify(Event event) {
        boolean hasAlarmData = event.getAlarmData() != null;
        boolean isInternal = event.getUei() != null
                && event.getUei().startsWith(INTERNAL_UEI_PREFIX);

        if (isInternal && hasAlarmData) {
            return EventClassification.DUAL;
        }
        if (isInternal) {
            return EventClassification.IPC;
        }
        // Everything else is a fault event: traps, syslog, thresholds, node events, etc.
        return EventClassification.FAULT;
    }
}
