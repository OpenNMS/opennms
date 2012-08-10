package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;
import org.opennms.features.topology.netutils.internal.Node;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ContextEventsAlarmsOperation implements Operation {

	private String eventsFilter;
	private String alarmsFilter;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		int nodeID = -1;
		if (targets != null) {
			List<Object> selectedVertices = operationContext.getGraphContainer().getSelectedVertices();
			if (selectedVertices.size() > 0) return false;
			for(Object target : targets) {
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
				if (vertexItem != null) {
					Property nodeIDProperty = vertexItem.getItemProperty("nodeID");
					nodeID = nodeIDProperty == null ? -1 : (Integer)nodeIDProperty.getValue();
				}
			}
		}
		if (nodeID < 0) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		String label = "";
		int nodeID = -1;

		try {
			if (targets != null) {
				for(Object target : targets) {
					Property labelProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label");
					label = labelProperty == null ? "" : (String) labelProperty.getValue();
					Property nodeIDProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID");
					nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
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
