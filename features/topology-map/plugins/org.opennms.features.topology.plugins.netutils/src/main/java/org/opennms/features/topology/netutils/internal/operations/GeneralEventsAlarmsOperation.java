package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;

public class GeneralEventsAlarmsOperation implements Operation {

	private String eventsURL;
	private String alarmsURL;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(null, getEventsURL(), getAlarmsURL()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		return "generalEventsAlarms";
	}

	public String getAlarmsURL() {
		return alarmsURL;
	}

	public void setAlarmsURL(String alarmsURL) {
		this.alarmsURL = alarmsURL;
	}

	public String getEventsURL() {
		return eventsURL;
	}

	public void setEventsURL(String eventsURL) {
		this.eventsURL = eventsURL;
	}

}