package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>EventDisplayFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EventDisplayFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="eventDisplay"</code> */
    public static final String TYPE = "eventDisplay";
    
    /**
     * <p>Constructor for EventDisplayFilter.</p>
     *
     * @param displayType a {@link java.lang.String} object.
     */
    public EventDisplayFilter(String displayType){
        super(TYPE, SQLType.STRING, "EVENTDISPLAY", "eventDisplay", displayType);
    }

}
