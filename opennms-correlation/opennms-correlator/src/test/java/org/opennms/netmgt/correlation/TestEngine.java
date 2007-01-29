package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class TestEngine extends AbstractCorrelationEngine {

	public void correlate(Event e) {
		if ("testDown".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testDownReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
		else if ("testUp".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testUpReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
		else {
			throw new IllegalArgumentException("Unexpected event with uei = "+e.getUei());
		}
		
	}
    
	public List<String> getInterestingEvents() {
		List<String> ueis = new ArrayList<String>();
		ueis.add("testDown");
		ueis.add("testUp");
		return ueis;
	}

}
