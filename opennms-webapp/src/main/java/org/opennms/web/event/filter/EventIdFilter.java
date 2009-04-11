package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

public class EventIdFilter extends EqualsFilter<Integer> {
    public static final String TYPE = "eventId";
    
    public EventIdFilter(int eventId){
        super(TYPE, SQLType.INT, "EVENTID", "id", eventId);
    }
    
}
