package org.opennms.web.event.filter;

import java.util.Collection;

import org.opennms.web.filter.InFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>EventIdListFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EventIdListFilter extends InFilter<Integer> {
    /** Constant <code>TYPE="eventIdList"</code> */
    public static final String TYPE = "eventIdList";
    
    private static Integer[] box(int[] values) {
        if (values == null) {
            return null;
        }
        
        Integer[] boxed = new Integer[values.length];
        for(int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        
        return boxed;
    }
    
    /**
     * <p>Constructor for EventIdListFilter.</p>
     *
     * @param eventIds an array of int.
     */
    public EventIdListFilter(int[] eventIds) {
        super(TYPE, SQLType.INT, "EVENTID", "id", box(eventIds));
    }
    
    /**
     * <p>Constructor for EventIdListFilter.</p>
     *
     * @param eventIds a {@link java.util.Collection} object.
     */
    public EventIdListFilter(Collection<Integer> eventIds) {
        super(TYPE, SQLType.INT, "EVENTID", "id", eventIds.toArray(new Integer[0]));
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        StringBuilder buf = new StringBuilder("eventId in ");
        buf.append("(");
        buf.append(getValueString());
        buf.append(")");
        return buf.toString();
    }
    
}
