package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.ResourceGraphsWindow;

public class ResourceGraphsOperation implements Operation {

	public boolean display(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return false;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		Node node = (Node)targets.get(0);
		try {
			operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(node));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
