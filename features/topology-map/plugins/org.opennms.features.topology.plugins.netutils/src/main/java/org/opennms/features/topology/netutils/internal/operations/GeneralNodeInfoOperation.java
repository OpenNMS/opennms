package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.NodeInfoWindow;

public class GeneralNodeInfoOperation implements Operation {

	private String nodeInfoURL;

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new NodeInfoWindow(null, getNodeInfoURL()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	public String getId() {
		return "generalNodeInfo";
	}

	public String getNodeInfoURL() {
		return nodeInfoURL;
	}

	public void setNodeInfoURL(String nodeInfoURL) {
		this.nodeInfoURL = nodeInfoURL;
	}

}
