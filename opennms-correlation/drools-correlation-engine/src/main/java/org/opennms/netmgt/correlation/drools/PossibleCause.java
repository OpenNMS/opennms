package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.xml.event.Event;

public class PossibleCause extends Cause {
    
    public PossibleCause(Long cause, Event symptom) {
        super(Type.POSSIBLE, cause, symptom);
    }

}
