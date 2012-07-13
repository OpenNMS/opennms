package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.PingWindow;

public class PingOperation implements Operation {

	public boolean display(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		Node node = (Node)targets.get(0);
		try {
			operationContext.getMainWindow().addWindow(new PingWindow(node));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "ping";
	}

}
