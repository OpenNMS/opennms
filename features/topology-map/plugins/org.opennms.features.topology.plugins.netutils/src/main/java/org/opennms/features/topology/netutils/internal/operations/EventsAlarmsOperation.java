package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;
import org.opennms.features.topology.netutils.internal.Node;

import com.vaadin.data.Item;

public class EventsAlarmsOperation implements Operation {

	private String eventsURL;
	private String eventsFilter;
	private String alarmsURL;
	private String alarmsFilter;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		int nodeID = 0;

		if (targets != null) {
			for(Object target : targets) {
				nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		int nodeID = 0;

		if (targets != null) {
			for(Object target : targets) {
			    Item item = operationContext.getGraphContainer().getVertexItem(target);
				nodeID = item.getItemProperty("nodeID").getValue() != null ? (Integer)item.getItemProperty("nodeID").getValue() : -1;
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String ipAddr = "";
		String label = "";
		int nodeID = -1;

		try {
			if (targets != null) {
				for(Object target : targets) {
					ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
					label = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label").getValue();
					nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
				}
			}
			if (nodeID < 0) {
				operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(null, getEventsURL(), getAlarmsURL()));
			} else {
				Node node = new Node(nodeID, ipAddr, label);
				operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(node, getEventsFilter(), getAlarmsFilter()));
			}
		} catch (Exception e) {e.printStackTrace();}
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

	public String getEventsFilter() {
		return eventsFilter;
	}

	public void setEventsFilter(String eventsFilter) {
		this.eventsFilter = eventsFilter;
	}

	public String getAlarmsFilter() {
		return alarmsFilter;
	}

	public void setAlarmsFilter(String alarmsFilter) {
		this.alarmsFilter = alarmsFilter;
	}

}
