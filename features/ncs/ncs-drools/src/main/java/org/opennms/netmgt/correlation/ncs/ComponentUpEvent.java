package org.opennms.netmgt.correlation.ncs;


import org.opennms.netmgt.xml.event.Event;

public class ComponentUpEvent extends ComponentEvent {
    
    public ComponentUpEvent(Component component, Event event) {
    	super(component, event);
    }


}
