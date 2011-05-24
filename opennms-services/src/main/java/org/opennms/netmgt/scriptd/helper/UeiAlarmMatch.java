package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class UeiAlarmMatch extends UeiEventMatch implements EventMatch {
	
	
	public UeiAlarmMatch() {
		super();
	}

	public UeiAlarmMatch(String ueimatch) {
		super(ueimatch);
	}

	@Override
	public boolean match(Event event) {
		return (event.getAlarmData() != null && super.match(event));
	}

}
