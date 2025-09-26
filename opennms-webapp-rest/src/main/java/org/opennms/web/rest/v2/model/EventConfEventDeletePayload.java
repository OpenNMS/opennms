package org.opennms.web.rest.v2.model;

import java.util.List;

public class EventConfEventDeletePayload {
    private List<Long> eventIds;

    public EventConfEventDeletePayload() {

    }

    public List<Long> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<Long> eventIds) {
        this.eventIds = eventIds;
    }

}
