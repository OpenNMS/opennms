package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.xml.event.Event;

public class RootCause extends Cause {

    public RootCause(Long cause, Event symptom) {
        super(Type.ROOT, cause, symptom);
    }

}
