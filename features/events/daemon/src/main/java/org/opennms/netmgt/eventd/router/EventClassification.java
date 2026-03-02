package org.opennms.netmgt.eventd.router;

public enum EventClassification {
    FAULT,      // -> Kafka + local broadcast
    IPC,        // -> JMS MessageBus + local broadcast
    DUAL        // -> both Kafka AND JMS (internal events with alarm-data)
}
