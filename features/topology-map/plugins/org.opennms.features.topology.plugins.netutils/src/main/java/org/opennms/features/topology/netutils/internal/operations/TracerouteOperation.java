package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.TracerouteWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class TracerouteOperation implements Operation {

	private String tracerouteURL;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";

		if (targets != null) {
			List<Object> selectedVertices = operationContext.getGraphContainer().getSelectedVertices();
			if (selectedVertices.size() > 0) return false;
			for(Object target : targets) {
				Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
				if (vertexItem != null) {
					Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
					ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
				}
			}
		}
		if ("".equals(ipAddr)) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";
		String label = "";
		int nodeID = -1;

		if (targets != null) {
			for(Object target : targets) {
				Property ipAddrProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr");
				ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
				Property labelProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label");
				label = labelProperty == null ? "" : (String) labelProperty.getValue();
				Property nodeIDProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID");
				nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
			}
		}
		Node node = new Node(nodeID, ipAddr, label);
		operationContext.getMainWindow().addWindow(new TracerouteWindow(node, getTracerouteURL()));
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "traceroute";
	}

	public void setTracerouteURL(String url) {
		tracerouteURL = url;
	}

	public String getTracerouteURL() {
		return tracerouteURL;
	}

}
