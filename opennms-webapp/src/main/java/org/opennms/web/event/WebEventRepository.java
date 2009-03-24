package org.opennms.web.event;

import java.util.Date;

import org.opennms.web.event.filter.EventCriteria;

public interface WebEventRepository {
    
    public abstract int countMatchingEvents(EventCriteria criteria);
    
    public abstract int[] countMatchingEventsBySeverity(EventCriteria criteria);
    
    public abstract Event getEvent(int eventId);
    
    public abstract Event[] getMatchingEvents(EventCriteria criteria);
    
    public abstract void acknowledgeMacthingEvents(String user, Date timestamp, EventCriteria criteria);
    
    public abstract void acknowledgeAll(String user, Date timestamp);
    
    public abstract void unacknowledgeMatchingEvents(EventCriteria criteria);
    
    public abstract void unacknowledgeAll();
}
