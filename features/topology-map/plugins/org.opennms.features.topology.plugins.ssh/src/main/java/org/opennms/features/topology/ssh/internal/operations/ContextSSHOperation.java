package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.AuthWindow;

public class ContextSSHOperation implements Operation {

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		//Default server info
		String ipAddr = "64.146.64.214";
		int port = 22;

		if (targets != null) {
			for(Object target : targets) {
			    ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
				port = 22;//(Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port").getValue();
			}
		}
		operationContext.getMainWindow().addWindow(new AuthWindow(ipAddr, port));
		return null;
	}

	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		String ipAddr = "";
		int port = -1;
		if (targets != null) {
			for(Object target : targets) {
			    ipAddr = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("ipAddr").getValue();
				port = 22;//(Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port").getValue();
			}
		}
		if ("".equals(ipAddr) || port < 0) return false;
		return true;
	}

	public String getId() {
		return "contextSSH";
	}

}
