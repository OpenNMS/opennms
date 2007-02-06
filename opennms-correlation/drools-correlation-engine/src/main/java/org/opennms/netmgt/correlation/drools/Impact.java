package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.xml.event.Event;

public class Impact extends Cause {

    public Impact(Long cause, Event symptom) {
        super(Type.IMPACT, cause, symptom);
    }

}
