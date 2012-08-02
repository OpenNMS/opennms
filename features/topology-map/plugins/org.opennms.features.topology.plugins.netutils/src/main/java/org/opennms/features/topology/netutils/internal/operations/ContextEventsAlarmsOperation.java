package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;
import org.opennms.features.topology.netutils.internal.Node;

public class ContextEventsAlarmsOperation implements Operation {

	private String eventsFilter;
	private String alarmsFilter;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		int nodeID = -1;
		if (targets != null) {
			for(Object target : targets) {
				nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		String label = "";
		int nodeID = -1;

		try {
			if (targets != null) {
				for(Object target : targets) {
					label = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label").getValue();
					nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
				}
			}
			Node node = new Node(nodeID, null, label);
			operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(node, getEventsFilter(), getAlarmsFilter()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		return "contextEventsAlarms";
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
