package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.TracerouteWindow;

public class TracerouteOperation implements Operation {

	private String tracerouteURL;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";

		if (targets != null) {
			for(Object target : targets) {
				ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
			}
		}
		if ("".equals(ipAddr)) return false;
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String ipAddr = "";

		if (targets != null) {
			for(Object target : targets) {
				ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
			}
		}
		if ("".equals(ipAddr)) return false;
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String ipAddr = "";
		String label = "";
		int nodeID = -1;

		if (targets != null) {
			for(Object target : targets) {
				ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
				label = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("label").getValue();
				nodeID = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("nodeID").getValue();
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
