package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class AlarmEventFilter extends UeiEventFilter implements EventFilter {
	
	
	public AlarmEventFilter(boolean allow) {
		super(allow);
	}

	public AlarmEventFilter(String ueimatch, boolean allow) {
		super(ueimatch, allow);
	}

	@Override
	public boolean match(Event event) {
		return (event.getAlarmData() != null && super.match(event));
	}

}
