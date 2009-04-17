package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

public class EventDisplayFilter extends EqualsFilter<String> {
    public static final String TYPE = "eventDisplay";
    
    public EventDisplayFilter(String displayType){
        super(TYPE, SQLType.STRING, "EVENTDISPLAY", "eventDisplay", displayType);
    }

}
