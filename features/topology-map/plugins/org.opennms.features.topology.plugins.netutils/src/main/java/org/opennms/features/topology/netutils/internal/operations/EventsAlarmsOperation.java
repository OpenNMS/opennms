package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;
import org.opennms.features.topology.netutils.internal.Node;

public class EventsAlarmsOperation implements Operation {

	/*Test Data*/
	private Node testNode1 = new Node(9,"172.20.1.10","Cartman");
	private Node testNode2 = new Node(43, "172.20.1.14", "Butters");
	
	private String eventsURL;
	private String alarmsURL;
	
	public boolean display(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(testNode1, getEventsURL(), getAlarmsURL()));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "eventsAlarms";
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
