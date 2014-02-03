package org.opennms.web.filter;



public enum NormalizedAcknowledgeType {
    ACKNOWLEDGED("ack"),
    UNACKNOWLEDGED("unack"),
    BOTH("both");

    private final String shortName;

    private NormalizedAcknowledgeType(final String shortName) {
        this.shortName = shortName;
    }

    public static NormalizedAcknowledgeType createFrom(org.opennms.web.event.AcknowledgeType eventAckType) {
        if (eventAckType == null) return null;
        return NormalizedAcknowledgeType.valueOf(eventAckType.name());
    }

    public static NormalizedAcknowledgeType createFrom(org.opennms.web.alarm.AcknowledgeType alarmAckType) {
        if (alarmAckType == null) return null;
        return NormalizedAcknowledgeType.valueOf(alarmAckType.name());
    }

    public String getShortName() {
        return shortName;
    }
}
