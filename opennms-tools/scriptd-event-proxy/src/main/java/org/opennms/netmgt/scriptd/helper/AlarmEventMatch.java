package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class AlarmEventMatch extends UeiEventMatch implements EventMatch {
	
	
	public AlarmEventMatch() {
		super();
	}

	public AlarmEventMatch(String ueimatch) {
		super(ueimatch);
	}

	@Override
	public boolean match(Event event) {
		return (event.getAlarmData() != null && super.match(event));
	}

}
