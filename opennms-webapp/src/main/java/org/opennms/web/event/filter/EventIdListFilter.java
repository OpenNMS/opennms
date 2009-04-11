package org.opennms.web.event.filter;

import org.opennms.web.filter.InFilter;
import org.opennms.web.filter.SQLType;

public class EventIdListFilter extends InFilter<Integer> {
    public static final String TYPE = "eventIdList";
    
    private static Integer[] box(int[] values) {
        if (values == null) return null;
        
        Integer[] boxed = new Integer[values.length];
        for(int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        
        return boxed;
    }
    
    public EventIdListFilter(int[] eventIds) {
        super(TYPE, SQLType.INT, "EVENTID", "id", box(eventIds));
    }
    
    public String getTextDescription() {
        StringBuilder buf = new StringBuilder("eventId in ");
        buf.append("(");
        buf.append(getValueString());
        buf.append(")");
        return buf.toString();
    }
    
}
