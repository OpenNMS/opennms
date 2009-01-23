package org.opennms.netmgt.scriptd;


import java.util.Date;

import org.opennms.netmgt.model.events.EventBuilder;


public class ScriptdTest {
    
    public void testEventBuilder() {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/external/scriptd/ExampleEvent", "scriptd");
        bldr.addParam("parm1", "abcXyz");
        bldr.setCreationTime(new Date());
        bldr.setDescription("Example Event");
        bldr.setInterface("10.1.1.1");
        
    }

}
