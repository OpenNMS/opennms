package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>EventIdFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EventIdFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="eventId"</code> */
    public static final String TYPE = "eventId";
    
    /**
     * <p>Constructor for EventIdFilter.</p>
     *
     * @param eventId a int.
     */
    public EventIdFilter(int eventId){
        super(TYPE, SQLType.INT, "EVENTID", "id", eventId);
    }
    
}
