package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.PingWindow;

public class PingOperation implements Operation {

	private String pingURL;

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
		operationContext.getMainWindow().addWindow(new PingWindow(node, getPingURL()));
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "ping";
	}

	public void setPingURL(String url) {
		pingURL = url;
	}

	public String getPingURL() {
		return pingURL;
	}

}
